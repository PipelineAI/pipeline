"""
module to handle capacity of resources
"""
import json

from autoscaler.config import Config
from autoscaler.kube import KubeResource

# RESOURCE_SPEC should denote the amount of resouces that are available
# to workload pods on a new, clean node, i.e. resouces used by system pods
# have to be accounted for
with open(Config.CAPACITY_DATA, 'r') as f:
    data = json.loads(f.read())
    RESOURCE_SPEC = {}
    for key, instance_map in data.items():
        RESOURCE_SPEC[key] = {}
        for instance_type, resource_spec in instance_map.items():
            resource_spec['cpu'] -= Config.CAPACITY_CPU_RESERVE
            resource = KubeResource(**resource_spec)
            RESOURCE_SPEC[key][instance_type] = resource

DEFAULT_TYPE_SELECTOR_KEY = 'aws/type'
DEFAULT_CLASS_SELECTOR_KEY = 'aws/class'
COMPUTING_SELECTOR_KEY = 'openai/computing'


def is_possible(pod):
    """
    returns whether the pod is possible under the maximum allowable capacity
    """
    computing = pod.selectors.get(COMPUTING_SELECTOR_KEY, 'false')
    selector = pod.selectors.get(DEFAULT_TYPE_SELECTOR_KEY)
    class_ = pod.selectors.get(DEFAULT_CLASS_SELECTOR_KEY)

    unit_caps = RESOURCE_SPEC[computing]

    # if an instance type was specified
    if selector in unit_caps:
        return (unit_caps[selector] - pod.resources).possible

    # if an instance class was specified, see if pod fits in any type in
    # the class
    for type_, resource in unit_caps.items():
        if (not class_ or type_.startswith(class_)) and (resource - pod.resources).possible:
            return True

    return False


def get_unit_capacity(group):
    """
    returns the KubeResource provided by one unit in the
    AutoScalingGroup or KubeNode
    """
    computing = group.selectors.get(COMPUTING_SELECTOR_KEY, 'false')
    instance_type = group.launch_config['InstanceType']
    unit_caps = RESOURCE_SPEC[computing]
    return unit_caps[instance_type]
