import collections
import datetime
import logging
import math
import time

import botocore
import boto3
import botocore.exceptions
import datadog
import pykube

import autoscaler.autoscaling_groups as autoscaling_groups
import autoscaler.capacity as capacity
from autoscaler.kube import KubePod, KubeNode, KubeResource, KubePodStatus
from autoscaler.notification import notify_scale, notify_failed_to_scale
import autoscaler.utils as utils

pykube.Pod.objects.namespace = None  # we are interested in all pods, incl. system ones

# HACK: https://github.com/kelproject/pykube/issues/29#issuecomment-230026930
import backports.ssl_match_hostname
# Monkey-patch match_hostname with backports's match_hostname, allowing for IP addresses
# XXX: the exception that this might raise is backports.ssl_match_hostname.CertificateError
pykube.http.requests.packages.urllib3.connection.match_hostname = backports.ssl_match_hostname.match_hostname

logger = logging.getLogger(__name__)


class ClusterNodeState(object):
    INSTANCE_TERMINATED = 'instance-terminated'
    ASG_MIN_SIZE = 'asg-min-size'
    POD_PENDING = 'pod-pending'
    GRACE_PERIOD = 'grace-period'
    TYPE_GRACE_PERIOD = 'type-grace-period'
    IDLE_SCHEDULABLE = 'idle-schedulable'
    IDLE_UNSCHEDULABLE = 'idle-unschedulable'
    BUSY_UNSCHEDULABLE = 'busy-unschedulable'
    BUSY = 'busy'
    UNDER_UTILIZED_DRAINABLE = 'under-utilized-drainable'
    UNDER_UTILIZED_UNDRAINABLE = 'under-utilized-undrainable'


class Cluster(object):

    # the number of instances per type that is allowed to be idle
    # this is for keeping some spare capacity around for faster
    # pod scheduling, instead of keeping the cluster at capacity
    # and having to spin up nodes for every job submission
    TYPE_IDLE_COUNT = 5

    # the utilization threshold under which to consider a node
    # under utilized and drainable
    UTIL_THRESHOLD = 0.3

    # HACK: before we're ready to favor bigger instances in all cases
    # just prioritize the ones that we're confident about
    _GROUP_DEFAULT_PRIORITY = 10
    _GROUP_PRIORITIES = {
        'g2.8xlarge': 2,
        'm4.xlarge': 0,
        'm4.2xlarge': 0,
        'm4.4xlarge': 0,
        'm4.10xlarge': 0
    }

    def __init__(self, regions, aws_access_key, aws_secret_key,
                 kubeconfig, idle_threshold, type_idle_threshold,
                 instance_init_time, cluster_name,
                 over_provision=5,
                 scale_up=True, maintainance=True,
                 datadog_api_key=None, slack_hook=None, dry_run=False):
        if kubeconfig:
            # for using locally
            logger.debug('Using kubeconfig %s', kubeconfig)
            self.api = pykube.HTTPClient(pykube.KubeConfig.from_file(kubeconfig))
        else:
            # for using on kube
            logger.debug('Using kube service account')
            self.api = pykube.HTTPClient(pykube.KubeConfig.from_service_account())

        self._drained = {}
        self.session = boto3.session.Session(
            aws_access_key_id=aws_access_key,
            aws_secret_access_key=aws_secret_key,
            region_name=regions[0])  # provide a default region
        self.autoscaling_groups = autoscaling_groups.AutoScalingGroups(
            session=self.session, regions=regions,
            cluster_name=cluster_name)

        # config
        self.regions = regions
        self.idle_threshold = idle_threshold
        self.instance_init_time = instance_init_time
        self.type_idle_threshold = type_idle_threshold
        self.over_provision = over_provision

        self.scale_up = scale_up
        self.maintainance = maintainance

        self.slack_hook = slack_hook

        if datadog_api_key:
            datadog.initialize(api_key=datadog_api_key)
            logger.info('Datadog initialized')
        self.stats = datadog.ThreadStats()
        self.stats.start()

        self.dry_run = dry_run

    def scale_loop(self):
        """
        runs one loop of scaling to current needs.
        returns True if successfully scaled.
        """
        logger.info("++++++++++++++ Running Scaling Loop ++++++++++++++++")
        try:
            all_nodes = map(KubeNode, pykube.Node.objects(self.api))
            managed_nodes = [node for node in all_nodes if node.is_managed()]

            running_insts_map = self.get_running_instances_map(managed_nodes)

            pods = map(KubePod, pykube.Pod.objects(self.api))

            running_or_pending_assigned_pods = [
                p for p in pods if (p.status == KubePodStatus.RUNNING or p.status == KubePodStatus.CONTAINER_CREATING) or (
                    p.status == KubePodStatus.PENDING and p.node_name
                )
            ]

            for node in all_nodes:
                for pod in running_or_pending_assigned_pods:
                    if pod.node_name == node.name:
                        node.count_pod(pod)

            asgs = self.autoscaling_groups.get_all_groups(all_nodes)

            pods_to_schedule = self.get_pods_to_schedule(pods)

            if self.scale_up:
                logger.info("++++++++++++++ Scaling Up Begins ++++++++++++++++")
                self.scale(
                    pods_to_schedule, all_nodes, asgs, running_insts_map)
                logger.info("++++++++++++++ Scaling Up Ends ++++++++++++++++")
            if self.maintainance:
                logger.info("++++++++++++++ Maintenance Begins ++++++++++++++++")
                self.maintain(
                    managed_nodes, running_insts_map,
                    pods_to_schedule, running_or_pending_assigned_pods, asgs)
                logger.info("++++++++++++++ Maintenance Ends ++++++++++++++++")

            return True
        except botocore.exceptions.ClientError as e:
            logger.warn(e)
            return False

    def fulfill_pending(self, asgs, selectors_hash, pods):
        """
        selectors_hash - string repr of selectors
        pods - list of KubePods that are pending
        """
        logger.info(
            "========= Scaling for %s ========", selectors_hash)
        logger.debug("pending: %s", pods[:5])

        accounted_pods = dict((p, False) for p in pods)
        num_unaccounted = len(pods)

        groups = utils.get_groups_for_hash(asgs, selectors_hash)

        groups = self._prioritize_groups(groups)

        for group in groups:
            logger.debug("group: %s", group)

            unit_capacity = capacity.get_unit_capacity(group)
            new_instance_resources = []
            assigned_pods = []
            for pod, acc in accounted_pods.items():
                if acc or not (unit_capacity - pod.resources).possible:
                    continue

                found_fit = False
                for i, instance in enumerate(new_instance_resources):
                    if (instance - pod.resources).possible:
                        new_instance_resources[i] = instance - pod.resources
                        assigned_pods[i].append(pod)
                        found_fit = True
                        break
                if not found_fit:
                    new_instance_resources.append(unit_capacity - pod.resources)
                    assigned_pods.append([pod])

            # new desired # machines = # running nodes + # machines required to fit jobs that don't
            #   fit on running nodes. This scaling is conservative but won't create starving
            units_needed = len(new_instance_resources)
            units_needed += self.over_provision

            if self.autoscaling_groups.is_timed_out(group):
                # if a machine is timed out, it cannot be scaled further
                # just account for its current capacity (it may have more
                # being launched, but we're being conservative)
                unavailable_units = max(
                    0, units_needed - (group.desired_capacity - group.actual_capacity))
            else:
                unavailable_units = max(
                    0, units_needed - (group.max_size - group.actual_capacity))
            units_requested = units_needed - unavailable_units

            logger.debug("units_needed: %s", units_needed)
            logger.debug("units_requested: %s", units_requested)

            new_capacity = group.actual_capacity + units_requested
            if not self.dry_run:
                scaled = group.scale(new_capacity)

                if scaled:
                    notify_scale(group, units_requested,
                                 pods,
                                 self.slack_hook)
            else:
                logger.info('[Dry run] Would have scaled up to %s', new_capacity)

            for i in range(min(len(assigned_pods), units_requested)):
                for pod in assigned_pods[i]:
                    accounted_pods[pod] = True
                    num_unaccounted -= 1

            logger.debug("remining pending: %s", num_unaccounted)

            if not num_unaccounted:
                break

        if num_unaccounted:
            logger.warn('Failed to scale sufficiently.')
            notify_failed_to_scale(selectors_hash, pods, hook=self.slack_hook)

    def get_running_instances_in_region(self, region, instance_ids):
        """
        a generator for getting ec2.Instance objects given a list of
        instance IDs.
        """
        yielded_ids = set()
        try:
            running_insts = (self.session
                             .resource('ec2', region_name=region)
                             .instances
                             .filter(
                                 InstanceIds=instance_ids,
                                 Filters=[{
                                     'Name': "instance-state-name",
                                     'Values': ["running"]}]
                             ))
            # we have to go through each instance to make sure
            # they actually exist and handle errors otherwise
            # boto collections do not always call DescribeInstance
            # when returning from filter, so it could error during
            # iteration
            for inst in running_insts:
                yield inst
                yielded_ids.add(inst.id)
        except botocore.exceptions.ClientError as e:
            logger.debug('Caught %s', e)
            if str(e).find("InvalidInstanceID.NotFound") == -1:
                raise e
            elif len(instance_ids) == 1:
                return
            else:
                # this should hopefully happen rarely so we resort to slow methods to
                # handle this case
                for instance_id in instance_ids:
                    if instance_id in yielded_ids:
                        continue
                    for inst in self.get_running_instances_in_region(region, [instance_id]):
                        yield inst

    def get_running_instances_map(self, nodes):
        """
        given a list of KubeNode's, return a map of
        instance_id -> ec2.Instance object
        """
        instance_id_by_region = {}
        for node in nodes:
            instance_id_by_region.setdefault(node.region, []).append(node.instance_id)

        instance_map = {}
        for region, instance_ids in instance_id_by_region.items():
            # note that this assumes that all instances have a valid region
            # the regions referenced by the nodes may also be outside of the
            # list of regions provided by the user
            # this should be ok because they will just end up being nodes
            # unmanaged by autoscaling groups we know about
            region_instances = self.get_running_instances_in_region(
                region, instance_ids)
            instance_map.update((inst.id, inst) for inst in region_instances)

        return instance_map

    def _get_required_capacity(self, requested, group):
        """
        returns the number of nodes within an autoscaling group that should
        be provisioned to fit the requested amount of KubeResource.

        TODO: bin packing would probably be better?

        requested - KubeResource
        group - AutoScalingGroup
        """
        unit_capacity = capacity.get_unit_capacity(group)
        return max(
            # (peter) should 0.8 be configurable?
            int(math.ceil(requested.get(field, 0.0) / unit_capacity.get(field, 1.0)))
            for field in ('cpu', 'memory', 'pods')
        )

    def _prioritize_groups(self, groups):
        """
        returns the groups sorted in order of where we should try to schedule
        things first
        """
        def sort_key(group):
            region = self._GROUP_DEFAULT_PRIORITY
            try:
                region = self.regions.index(group.region)
            except ValueError:
                pass
            priority = self._GROUP_PRIORITIES.get(group.selectors.get('aws/type'), self._GROUP_DEFAULT_PRIORITY)
            return (region, priority, group.name)
        return sorted(groups, key=sort_key)

    def get_node_state(self, node, asg, node_pods, pods_to_schedule,
                       running_insts_map, idle_selector_hash):
        """
        returns the ClusterNodeState for the given node

        params:
        node - KubeNode object
        asg - AutoScalingGroup object that this node belongs in. can be None.
        node_pods - list of KubePods assigned to this node
        pods_to_schedule - list of all pending pods
        running_inst_map - map of all (instance_id -> ec2.Instance object)
        idle_selector_hash - current map of idle nodes by type. may be modified.
        """
        pending_list = []
        for pods in pods_to_schedule.values():
            for pod in pods:
                if node.is_match(pod):
                    pending_list.append(pod)
        # we consider a node to be busy if it's running any non-DaemonSet pods
        # TODO: we can be a bit more aggressive in killing pods that are
        # replicated
        busy_list = [p for p in node_pods if not p.is_mirrored()]
        undrainable_list = [p for p in node_pods if not p.is_drainable()]
        utilization = sum((p.resources for p in busy_list), KubeResource())
        under_utilized = (self.UTIL_THRESHOLD * node.capacity - utilization).possible
        drainable = not undrainable_list

        maybe_inst = running_insts_map.get(node.instance_id)
        instance_type = utils.selectors_to_hash(asg.selectors) if asg else None

        if maybe_inst:
            age = (datetime.datetime.now(maybe_inst.launch_time.tzinfo)
                   - maybe_inst.launch_time).seconds
        else:
            age = None

        instance_type = utils.selectors_to_hash(asg.selectors) if asg else node.instance_type

        if maybe_inst is None:
            state = ClusterNodeState.INSTANCE_TERMINATED
        elif asg and len(asg.nodes) <= asg.min_size:
            state = ClusterNodeState.ASG_MIN_SIZE
        elif busy_list and not under_utilized:
            if node.unschedulable:
                state = ClusterNodeState.BUSY_UNSCHEDULABLE
            else:
                state = ClusterNodeState.BUSY
        elif pending_list and not node.unschedulable:
            state = ClusterNodeState.POD_PENDING
        elif ((not self.type_idle_threshold or idle_selector_hash[instance_type] >= self.TYPE_IDLE_COUNT)
              and age <= self.idle_threshold) and not node.unschedulable:
            # there is already an instance of this type sitting idle
            # so we use the regular idle threshold for the grace period
            state = ClusterNodeState.GRACE_PERIOD
        elif (instance_type and idle_selector_hash[instance_type] < self.TYPE_IDLE_COUNT
              and age <= self.type_idle_threshold) and not node.unschedulable:
            # we don't have an instance of this type yet!
            # use the type idle threshold for the grace period
            # and mark the type as seen
            idle_selector_hash[instance_type] += 1
            state = ClusterNodeState.TYPE_GRACE_PERIOD
        elif under_utilized and not node.unschedulable:
            if drainable:
                state = ClusterNodeState.UNDER_UTILIZED_DRAINABLE
            else:
                state = ClusterNodeState.UNDER_UTILIZED_UNDRAINABLE
        else:
            if node.unschedulable:
                state = ClusterNodeState.IDLE_UNSCHEDULABLE
            else:
                state = ClusterNodeState.IDLE_SCHEDULABLE

        return state

    def get_pods_to_schedule(self, pods):
        """
        given a list of KubePod objects,
        return a map of (selectors hash -> pods) to be scheduled
        """
        pending_unassigned_pods = [
            p for p in pods
            if p.status == KubePodStatus.PENDING and (not p.node_name)
        ]

        # we only consider a pod to be schedulable if it's pending and unassigned and feasible
        pods_to_schedule = {}
        for pod in pending_unassigned_pods:
            if capacity.is_possible(pod):
                pods_to_schedule.setdefault(utils.selectors_to_hash(pod.selectors), []).append(pod)
            else:
                logger.warn(
                    "Pending pod %s cannot fit %s. "
                    "Please check that requested resource amount is "
                    "consistent with node selectors. Scheduling skipped." % (pod.name, pod.selectors))
        return pods_to_schedule

    def scale(self, pods_to_schedule, all_nodes, asgs, running_insts_map):
        """
        scale up logic
        """
        for asg in asgs:
            self.autoscaling_groups.reconcile_limits(asg)

        cached_live_nodes = []
        for node in all_nodes:
            # either we know the physical node behind it and know it's alive
            # or we don't know it and assume it's alive
            if (node.instance_id and node.instance_id in running_insts_map) \
                    or (not node.is_managed()):
                cached_live_nodes.append(node)

        # asg name -> pending KubePods
        pending_pods = {}

        # for each pending & unassigned job, try to fit them on current machines or count requested
        #   resources towards future machines
        for selectors_hash, pods in pods_to_schedule.items():
            for pod in pods:
                fitting = None
                for node in cached_live_nodes:
                    if node.can_fit(pod.resources):
                        fitting = node
                        break
                if fitting is None:
                    # because a pod may be able to fit in multiple groups
                    # pick a group now
                    pending_pods.setdefault(selectors_hash, []).append(pod)
                    logger.info(
                        "{pod} is pending ({selectors_hash})".format(
                            pod=pod, selectors_hash=selectors_hash))
                else:
                    fitting.count_pod(pod)
                    logger.info("{pod} fits on {node}".format(pod=pod,
                                                              node=fitting))

        # scale each node type to reach the new capacity
        for selectors_hash, pending in pending_pods.items():
            self.fulfill_pending(asgs, selectors_hash, pending)

    def maintain(self, cached_managed_nodes, running_insts_map,
                 pods_to_schedule, running_or_pending_assigned_pods, asgs):
        """
        maintains running instances:
        - determines if idle nodes should be drained and terminated
        - determines if there are bad nodes in ASGs (did not spin up under
          `instance_init_time` seconds)
        """
        logger.info("++++++++++++++ Maintaining Managed Nodes ++++++++++++++++")

        # for each type of instance, we keep one around for longer
        # in order to speed up job start up time
        idle_selector_hash = collections.Counter()

        pods_by_node = {}
        for p in running_or_pending_assigned_pods:
            pods_by_node.setdefault(p.node_name, []).append(p)

        stats_time = time.time()

        for node in cached_managed_nodes:
            asg = utils.get_group_for_node(asgs, node)
            state = self.get_node_state(
                node, asg, pods_by_node.get(node.name, []), pods_to_schedule,
                running_insts_map, idle_selector_hash)

            logger.info("node: %-*s state: %s" % (75, node, state))
            self.stats.increment(
                'kubernetes.custom.node.state.{}'.format(state.replace('-', '_')),
                timestamp=stats_time)

            # state machine & why doesnt python have case?
            if state in (ClusterNodeState.POD_PENDING, ClusterNodeState.BUSY,
                         ClusterNodeState.GRACE_PERIOD,
                         ClusterNodeState.TYPE_GRACE_PERIOD,
                         ClusterNodeState.ASG_MIN_SIZE):
                # do nothing
                pass
            elif state == ClusterNodeState.UNDER_UTILIZED_DRAINABLE:
                if not self.dry_run:
                    if not asg:
                        logger.warn('Cannot find ASG for node %s. Not cordoned.', node)
                    else:
                        node.cordon()
                        node.drain(pods_by_node.get(node.name, []))
                else:
                    logger.info('[Dry run] Would have drained and cordoned %s', node)
            elif state == ClusterNodeState.IDLE_SCHEDULABLE:
                if not self.dry_run:
                    if not asg:
                        logger.warn('Cannot find ASG for node %s. Not cordoned.', node)
                    else:
                        node.cordon()
                else:
                    logger.info('[Dry run] Would have cordoned %s', node)
            elif state == ClusterNodeState.BUSY_UNSCHEDULABLE:
                # this is duplicated in original scale logic
                if not self.dry_run:
                    node.uncordon()
                else:
                    logger.info('[Dry run] Would have uncordoned %s', node)
            elif state == ClusterNodeState.IDLE_UNSCHEDULABLE:
                # remove it from asg
                if not self.dry_run:
                    if not asg:
                        logger.warn('Cannot find ASG for node %s. Not terminated.', node)
                    else:
                        asg.scale_node_in(node)
                else:
                    logger.info('[Dry run] Would have scaled in %s', node)
            elif state == ClusterNodeState.INSTANCE_TERMINATED:
                if not self.dry_run:
                    node.delete()
                else:
                    logger.info('[Dry run] Would have deleted %s', node)
            elif state == ClusterNodeState.UNDER_UTILIZED_UNDRAINABLE:
                # noop for now
                pass
            else:
                raise Exception("Unhandled state: {}".format(state))

        logger.info("++++++++++++++ Maintaining Unmanaged Instances ++++++++++++++++")
        # these are instances that have been running for a while but it's not properly managed
        #   i.e. not having registered to kube or not having proper meta data set
        managed_instance_ids = set(node.instance_id for node in cached_managed_nodes)
        for asg in asgs:
            unmanaged_instance_ids = list(asg.instance_ids - managed_instance_ids)
            if len(unmanaged_instance_ids) != 0:
                unmanaged_running_insts = self.get_running_instances_in_region(
                    asg.region, unmanaged_instance_ids)
                for inst in unmanaged_running_insts:
                    if (datetime.datetime.now(inst.launch_time.tzinfo)
                            - inst.launch_time).seconds >= self.instance_init_time:
                        if not self.dry_run:
                            asg.client.terminate_instance_in_auto_scaling_group(
                                InstanceId=inst.id, ShouldDecrementDesiredCapacity=False)
                            logger.info("terminating unmanaged %s" % inst)
                            self.stats.increment(
                                'kubernetes.custom.node.state.unmanaged',
                                timestamp=stats_time)
                            # TODO: try to delete node from kube as well
                            # in the case where kubelet may have registered but node
                            # labels have not been applied yet, so it appears unmanaged
                        else:
                            logger.info('[Dry run] Would have terminated unmanaged %s', inst)
