#-*- coding: utf-8 -*-

__version__ = "0.63"

# Requirements
#   python3, kops, ssh-keygen, awscli, packaging, appdirs, gcloud, azure-cli, helm, kubectl, kubernetes.tar.gz

# References:
#   https://github.com/kubernetes-incubator/client-python/blob/master/kubernetes/README.md
#   https://github.com/kubernetes/kops/blob/master/docs/aws.md

from multiprocessing import Process, Pool
import warnings
import requests
import fire
import tarfile
import os
import sys
import kubernetes.client as kubeclient
from kubernetes.client.rest import ApiException
import kubernetes.config as kubeconfig
import pick
import yaml
import json
import dill as pickle
#from git import Repo
from pprint import pprint
import subprocess
from datetime import timedelta


class PioCli(object):
    _kube_deploy_registry = {'jupyter': (['jupyterhub.ml/jupyterhub-deploy.yaml'], []),
                            'jupyterhub': (['jupyterhub.ml/jupyterhub-deploy.yaml'], []),
                            'spark': (['apachespark.ml/master-deploy.yaml'], ['spark-worker', 'metastore']),
                            'spark-worker': (['apachespark.ml/worker-deploy.yaml'], []),
                            'metastore': (['metastore.ml/metastore-deploy.yaml'], ['mysql']),
                            'hdfs': (['hdfs.ml/namenode-deploy.yaml'], []),
                            'redis': (['keyvalue.ml/redis-master-deploy.yaml'], []),
                            'presto': (['presto.ml/master-deploy.yaml',
                                        'presto.ml/worker-deploy.yaml'], ['metastore']),
                            'presto-ui': (['presto.ml/ui-deploy.yaml'], ['presto']),
                            'airflow': (['scheduler.ml/airflow-deploy.yaml'], ['mysql', 'redis']),
                            'mysql': (['sql.ml/mysql-master-deploy.yaml'], []),
                            'web-home': (['web.ml/home-deploy.yaml'], []),
                            'zeppelin': (['zeppelin.ml/zeppelin-deploy.yaml'], []),
                            'zookeeper': (['zookeeper.ml/zookeeper-deploy.yaml'], []),
                            'elasticsearch': (['elasticsearch.ml/elasticsearch-2-3-0-deploy.yaml'], []),
                            'kibana': (['kibana.ml/kibana-4-5-0-deploy.yaml'], ['elasticsearch'], []), 
                            'kafka': (['stream.ml/kafka-0.10-deploy.yaml'], ['zookeeper']),
                            'cassandra': (['cassandra.ml/cassandra-deploy.yaml'], []),
                            'prediction-java': (['prediction.ml/java-deploy.yaml'], []),
                            'prediction-python3': (['prediction.ml/python3-deploy.yaml'], []),
                            'prediction-scikit': (['prediction.ml/scikit-deploy.yaml'], []),
                            'prediction-pmml': (['prediction.ml/pmml-deploy.yaml'], []),
                            'prediction-spark': (['prediction.ml/spark-deploy.yaml'], []),
                            'prediction-tensorflow': (['prediction.ml/tensorflow-deploy.yaml'], []),
                            'prediction-tensorflow-gpu': (['prediction.ml/tensorflow-gpu-deploy.yaml'], []),
                            'turbine': (['dashboard.ml/turbine-deploy.yaml'], []),
                            'hystrix': (['dashboard.ml/hystrix-deploy.yaml'], []),
                            'weave-scope-app': (['dashboard.ml/weavescope/scope-1.3.0.yaml'], []),
                            'kubernetes-dashboard': (['dashboard.ml/kubernetes-dashboard/v1.6.0.yaml'], []),
                            'heapster': (['metrics.ml/monitoring-standalone/v1.3.0.yaml'], []),
                            'route53-mapper': (['dashboard.ml/route53-mapper/v1.3.0.yml'], []), 
                            'kubernetes-logging': (['dashboard.ml/logging-elasticsearch/v1.5.0.yaml'], []),
                           }

    _kube_svc_registry = {'jupyter': (['jupyterhub.ml/jupyterhub-svc.yaml'], []),
                         'jupyterhub': (['jupyterhub.ml/jupyterhub-svc.yaml'], []),
                         'spark': (['apachespark.ml/master-svc.yaml'], ['spark-worker', 'metastore']), 
                         'spark-worker': (['apachespark.ml/worker-svc.yaml'], []),
                         'metastore': (['metastore.ml/metastore-svc.yaml'], ['mysql']),
                         'hdfs': (['hdfs.ml/namenode-svc.yaml'], []),
                         'redis': (['keyvalue.ml/redis-master-svc.yaml'], []),
                         'presto': (['presto.ml/master-svc.yaml',
                                     'presto.ml/worker-svc.yaml'], ['metastore']),
                         'presto-ui': (['presto.ml/ui-svc.yaml'], ['presto']),
                         'airflow': (['scheduler.ml/airflow-svc.yaml'], ['mysql', 'redis']),
                         'mysql': (['sql.ml/mysql-master-svc.yaml'], []),
                         'web-home': (['web.ml/home-svc.yaml'], []),
                         'zeppelin': (['zeppelin.ml/zeppelin-svc.yaml'], []),
                         'zookeeper': (['zookeeper.ml/zookeeper-svc.yaml'], []),
                         'elasticsearch': (['elasticsearch.ml/elasticsearch-2-3-0-svc.yaml'], []),
                         'kibana': (['kibana.ml/kibana-4-5-0-svc.yaml'], ['elasticsearch'], []),
                         'kafka': (['stream.ml/kafka-0.10-svc.yaml'], ['zookeeper']),
                         'cassandra': (['cassandra.ml/cassandra-svc.yaml'], []),
                         'prediction-java': (['prediction.ml/java-svc.yaml'], []),
                         'prediction-python3': (['prediction.ml/python3-svc.yaml'], []),
                         'prediction-scikit': (['prediction.ml/scikit-svc.yaml'], []),
                         'prediction-spark': (['prediction.ml/spark-svc.yaml'], []),
                         'prediction-pmml': (['prediction.ml/pmml-svc.yaml'], []),
                         'prediction-tensorflow': (['prediction.ml/tensorflow-svc.yaml'], []),
                         'prediction-tensorflow-gpu': (['prediction.ml/tensorflow-gpu-svc.yaml'], []),
                         'turbine': (['dashboard.ml/turbine-svc.yaml'], []),
                         'hystrix': (['dashboard.ml/hystrix-svc.yaml'], []),
                        }


    def _get_default_pio_api_version(self):
        return 'v1'


    def _get_default_pio_git_home(self):
        return 'https://github.com/fluxcapacitor/source.ml/'


    def _get_default_pio_git_version(self):
        return 'master'


    def _get_current_context_from_kube_config(self):
        kube_cmd = "kubectl config current-context" 
        process = subprocess.Popen(kube_cmd.split(), stdout=subprocess.PIPE) 
        (output, error) = process.communicate() 
        return output.rstrip().decode('utf-8')


    def config_get(self,
                   config_key):
        print("")
        pprint(self._get_full_config())
        print("")
        return self._get_full_config()[config_key]


    def config_set(self,
                   config_key,
                   config_value):
        print("config_key: '%s'" % config_key)
        self._merge_config_dict({config_key: config_value})
        print("config_value: '%s'" % self._get_full_config()[config_key])
        self._merge_config_dict({config_key: config_value})
        print("")
        pprint(self._get_full_config())
        print("")        


    def _merge_config_dict(self, 
                           config_dict):

        pio_api_version = self._get_full_config()['pio_api_version']
        config_file_base_path = "~/.pio/"
        config_file_base_path = os.path.expandvars(config_file_base_path)
        config_file_base_path = os.path.expanduser(config_file_base_path)
        config_file_base_path = os.path.abspath(config_file_base_path)
        config_file_path = os.path.join(config_file_base_path, 'config')
        print("")

        existing_config_dict = self._get_full_config()

        # >= Python3.5 
        # {**existing_config_dict, **config_dict}
        existing_config_dict.update(config_dict)

        new_config_yaml = yaml.dump(existing_config_dict, default_flow_style=False, explicit_start=True)

        with open(config_file_path, 'w') as fh:
            fh.write(new_config_yaml)
#        print(new_config_yaml)
#        print("")


    def _get_full_config(self):
        config_file_base_path = "~/.pio/"
        config_file_base_path = os.path.expandvars(config_file_base_path)
        config_file_base_path = os.path.expanduser(config_file_base_path)
        config_file_base_path = os.path.abspath(config_file_base_path)
        config_file_filename = os.path.join(config_file_base_path, 'config')

        if not os.path.exists(config_file_filename):
            if not os.path.exists(config_file_base_path):
                os.makedirs(config_file_base_path)
            initial_config_dict = {'pio_api_version': self._get_default_pio_api_version(),
                                   'pio_git_home': self._get_default_pio_git_home(),
                                   'pio_git_version': self._get_default_pio_git_version()}
            initial_config_yaml =  yaml.dump(initial_config_dict, default_flow_style=False, explicit_start=True)
            print("")
            print("Default config created at '%s'.  Override with 'pio init-pio'" % config_file_filename)
            print("")
            with open(config_file_filename, 'w') as fh:
                fh.write(initial_config_yaml)
                pprint(initial_config_dict)

        # Load the YAML 
        with open(config_file_filename, 'r') as fh:
            existing_config_dict = yaml.load(fh)
            return existing_config_dict


    def config(self):
        pprint(self._get_full_config())
        print("")


    def proxy(self,
              app_name,
              local_port=None,
              app_port=None):

        self.tunnel(app_name, local_port, app_port)


    def tunnel(self,
               app_name,
               local_port=None,
               app_port=None):

        pio_api_version = self._get_full_config()['pio_api_version']
        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        pod = self._get_pod_by_app_name(app_name)
        if not pod:
            print("")
            print("App '%s' is not running." % app_name)
            print("")
            return
        if not app_port:
            svc = self._get_svc_by_app_name(app_name)
            if not svc:
                print("")
                print("App '%s' proxy port cannot be found." % app_name)
                print("")
                return
            app_port = svc.spec.ports[0].target_port

        if not local_port:
            print("")
            print("Proxying local port '<randomly-chosen>' to app '%s' port '%s' using pod '%s'." % (app_port, app_name, pod.metadata.name))
            print("")
            print("Use 'http://127.0.0.1:<randomly-chosen>' to access app '%s' on port '%s'." % (app_name, app_port))
            print("")
            print("If you break out of this terminal, your proxy session will end.")
            print("")
            subprocess.call('kubectl port-forward %s :%s' % (pod.metadata.name, app_port), shell=True)
            print("")
        else:
            print("")
            print("Proxying local port '%s' to app '%s' port '%s' using pod '%s'." % (local_port, app_port, app_name, pod.metadata.name))
            print("")
            print("Use 'http://127.0.0.1:%s' to access app '%s' on port '%s'." % (local_port, app_name, app_port))
            print("")
            print("If you break out of this terminal, your proxy session will end.")
            print("")
            subprocess.call('kubectl port-forward %s %s:%s' % (pod.metadata.name, local_port, app_port), shell=True)
            print("")


    # TODO:  Start an airflow job
    def flow(self,
             flow_name):
        print("")
        print("Submit airflow coming soon!")


    # TODO:  Submit a spark job
    def submit(self,
               replicas):
        print("Submit spark job coming soon!")


    def top(self,
            app_name=None):

        self.system(app_name)


    def system(self,
               app_name=None):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured 'pio init-cluster'.")
            print("")
            return

        if (app_name):
#            print("")
#            print("Retrieving system metrics for app '%s'." % app_name)
#            print("")
            self._get_app_resources(app_name)
#            print("")
#            print("Retrieving system metrics for cluster.")
            print("")
            self._get_cluster_resources()
        else:
            print("")
            print("Retrieving only system resources for cluster.  Use '--app-name' for app-level, as well.")
            print("")
            self._get_cluster_resources()

        print("")
        print("If you see an error above, you need to start Heapster with 'pio start heapster'.")
        print("")


    def _get_cluster_resources(self):
        subprocess.call("kubectl top node", shell=True)
        print("")

    def _get_app_resources(self,
                           app_name):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured 'pio init-cluster'.")
            print("")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()
        
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_pod_for_all_namespaces(watch=False, 
                                                                 pretty=True)
            pods = response.items
            for pod in pods: 
                if (app_name in pod.metadata.name):
                    subprocess.call('kubectl top pod %s' % pod.metadata.name, shell=True)
        print("")


    def join(self,
             federation):
        print("")
        print("Federation joining coming soon!")
        print("")


    def up(self,
           provider='aws',
           ssh_public_key='~/.ssh/id_rsa.pub',
           initial_worker_count='1',
#           min_worker_count='1',
#           max_worker_count='1',
           worker_zones='us-west-2a,us-west-2b',
           worker_type='r2.2xlarge',
           master_zones='us-west-2c',
           master_type='t2.medium',
#           dns_zone='',
#           vpc='',
           kubernetes_version='1.6.2',
           kubernetes_image='kope.io/k8s-1.5-debian-jessie-amd64-hvm-ebs-2017-01-09'):
        try:
            kops_cluster_name = self._get_full_config()['kops_cluster_name']
            kops_state_store = self._get_full_config()['kops_state_store']

            if not kops_ssh_public_key:
                subprocess.call("ssh-keygen -t rsa", shell=True)
                ssh_public_key = '~/.ssh/id_rsa.pub'

            subprocess.call("aws configure", shell=True)
            subprocess.call("aws s3 mb %s" % kops_state_store, shell=True)

            subprocess.call("kops create cluster --ssh-public-key %s --node-count %s --zones %s --master-zones %s --node-size %s --master-size %s --kubernetes-version %s --image %s --state %s --name %s" % (ssh_public_key, initial_worker_count, worker_zones, master_zones, worker_type, master_type, kubernetes_version, kubernetes_image, kops_state_store, kops_cluster_name), shell=True)
            subprocess.call("kops update --state %s cluster %s --yes" % (kops_state_store, kops_cluster_name), shell=True)
            subprocess.call("kubectl config set-cluster %s --insecure-skip-tls-verify=true" % kops_cluster_name)
            print("")
            print("Cluster is being created.  This may take a few mins.")
            print("")
            print("Once the cluster is up, run 'kubectl cluster-info' for the Kubernetes dashboard url.")
            print("Username is 'admin'.")
            print("Password can be retrieved with 'kops get secrets kube --type secret -oplaintext --state %s'" % kops_state_store)
        except:
            print("")
            print("Kops needs to be configured with 'pio init-kops'.")
            print("")
            return
           

    def init_kops(self,
                  kops_cluster_name,
                  kops_state_store):
        config_dict = {'kops_cluster_name': kops_cluster_name,
                       'kops_state_store': kops_state_store}
        self._merge_config_dict(config_dict)
        print("")
        pprint(self._get_full_config())
        print("")

       
    def instancegroups(self):
        try:
            kops_cluster_name = self._get_full_config()['kops_cluster_name']
            kops_state_store = self._get_full_config()['kops_state_store']
            
            subprocess.call("kops --state %s --name %s get instancegroups" % (kops_state_store, kops_cluster_name), shell=True)
            print("")
        except:
            print("")
            print("Kops needs to be configured with 'pio init-kops'.")
            print("")
            return


    def clusters(self):
        try:
            kops_cluster_name = self._get_full_config()['kops_cluster_name']
            kops_state_store = self._get_full_config()['kops_state_store']

            subprocess.call("kops --state %s --name %s get clusters" % (kops_state_store, kops_cluster_name), shell=True)
            print("")
        except:
            print("")
            print("Kops needs to be configured with 'pio init-kops'.")
            print("")
            return
 

    def federations(self):
        try:
            kops_cluster_name = self._get_full_config()['kops_cluster_name']
            kops_state_store = self._get_full_config()['kops_state_store']

            subprocess.call("kops --state %s --name %s get federations" % (kops_state_store, kops_cluster_name), shell=True)
            print("")
        except:
            print("")
            print("Kops needs to be configured with 'pio init-kops'.")
            print("")
            return


    def secrets(self):
        try:
            kops_cluster_name = self._get_full_config()['kops_cluster_name']
            kops_state_store = self._get_full_config()['kops_state_store']

            subprocess.call("kops --state %s --name %s get secrets" % (kops_state_store, kops_cluster_name), shell=True)
            print("")
        except:
            print("")
            print("Kops needs to be configured with 'pio init-kops'.")
            print("")
            return


    def init_pio(self,
                 pio_api_version,
                 pio_git_home,
                 pio_git_version):
        config_dict = {'pio_api_version': pio_api_version,
                       'pio_git_home': pio_git_home,
                       'pio_git_version': pio_git_version}
        self._merge_config_dict(config_dict)
        print("")
        pprint(self._get_full_config())
        print("")


    def init_cluster(self,
                     kube_cluster_context=None,
                     kube_namespace=None):
        pio_api_version = self._get_full_config()['pio_api_version']

        if not kube_cluster_context:
            kube_cluster_context = self._get_current_context_from_kube_config()

        if not kube_namespace:
            kube_namespace = 'default'

        config_dict = {'kube_cluster_context': kube_cluster_context, 
                       'kube_namespace': kube_namespace}
        self._merge_config_dict(config_dict)
        print("")
        pprint(self._get_full_config())
        print("")


    def init_model(self,
                   model_server_url,
                   model_type,
                   model_namespace,
                   model_name,
                   model_version,
                   model_path,
                   model_test_request_path=None,
                   model_input_mime_type='application/json',
                   model_output_mime_type='application/json'):

        pio_api_version = self._get_full_config()['pio_api_version']

        model_path = os.path.expandvars(model_path)
        model_path = os.path.expanduser(model_path)
        model_path = os.path.abspath(model_path)
        
        if model_test_request_path:
            model_test_request_path = os.path.expandvars(model_test_request_path)
            model_test_request_path = os.path.expanduser(model_test_request_path)
            model_test_request_path = os.path.abspath(model_test_request_path)

        config_dict = {"model_server_url": model_server_url.rstrip('/'), 
                       "model_type": model_type,
                       "model_namespace": model_namespace,
                       "model_name": model_name,
                       "model_version": model_version,
                       "model_path": model_path, 
                       "model_test_request_path": model_test_request_path,
                       "model_input_mime_type": model_input_mime_type,
                       "model_output_mime_type": model_output_mime_type,
        }

        self._merge_config_dict(config_dict)
        print("")
        pprint(self._get_full_config())
        print("")


    def upgrade(self,
                app_name,
                docker_image,
                docker_tag):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1_beta1.list_deployment_for_all_namespaces(watch=False,
                                                                              pretty=True)
            found = False
            deployments = response.items
            for deploy in deployments:
                if app_name in deploy.metadata.name:
                    found = True
                    break
            if found:
                print("")
                print("Upgrading app '%s' using Docker image '%s:%s'." % (deploy.metadata.name, docker_image, docker_tag))
                print("")
                kube_cmd = "kubectl set image deploy %s %s=%s:%s" % (deploy.metadata.name, deploy.metadata.name, docker_image, docker_tag)
                print("Running '%s'." % kube_cmd)
                print("")
                subprocess.call(kube_cmd, shell=True)
                print("")
                kube_cmd = "kubectl rollout status deploy %s" % deploy.metadata.name
                print("Running '%s'." % kube_cmd)
                print("")
                subprocess.call(kube_cmd, shell=True)
                print("")
                kube_cmd = "kubectl rollout history deploy %s" % deploy.metadata.name
                print("Running '%s'." % kube_cmd)
                print("")
                subprocess.call(kube_cmd, shell=True)
                print("")
                print("Check status with 'pio cluster'.")
                print("")
            else:
                print("")
                print("App '%s' is not running." % app_name)
                print("")


    def rollback(self,
                app_name,
                to_revision=None):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1_beta1.list_deployment_for_all_namespaces(watch=False,
                                                                              pretty=True)
            found = False
            deployments = response.items
            for deploy in deployments:
                if app_name in deploy.metadata.name:
                    found = True
                    break
            if found:
                print("")
                if to_revision:
                    print("Rolling back app '%s' to revision '%s'." % deploy.metadata.name, revision)
                    kube_cmd = "kubectl rollout undo deploy %s --to-revision=%s" % (deploy.metadata.name, to_revision)
                else:
                    print("Rolling back app '%s'." % deploy.metadata.name)
                    kube_cmd = "kubectl rollout undo deploy %s" % deploy.metadata.name
                print("")
                print("Running '%s'." % kube_cmd)
                print("")
                subprocess.call(kube_cmd, shell=True)
                print("")
                kube_cmd = "kubectl rollout status deploy %s" % deploy.metadata.name
                print("Running '%s'." % kube_cmd)
                print("")
                subprocess.call(kube_cmd, shell=True)
                print("")
                kube_cmd = "kubectl rollout history deploy %s" % deploy.metadata.name
                print("Running '%s'." % kube_cmd)
                print("")
                subprocess.call(kube_cmd, shell=True)
                print("")
                print("Check status with 'pio cluster'.")
                print("")
            else:
                print("")
                print("App '%s' is not running." % app_name)
                print("")


    def deploy_from_git(self,
                        git_path,
                        model_server_url=None,
                        model_type=None,
                        model_namespace=None,
                        model_name=None,
                        model_version=None):
        print("")
        print("Coming soon!")
        print("")


    def deploy(self,
               model_server_url=None,
               model_type=None,
               model_namespace=None,
               model_name=None,
               model_version=None, 
               model_path=None):

        pio_api_version = self._get_full_config()['pio_api_version']

        if not model_server_url:
            try:
                model_server_url = self._get_full_config()['model_server_url']
            except:
                print("")
                print("Model needs to be configured with 'pio init-model'.")
                print("")
                return

        if not model_type:
            try:
                model_type = self._get_full_config()['model_type']
            except:
                print("")
                print("Model needs to be configured with 'pio init-model'.")
                print("")
                return

        if not model_namespace:
            try:
                model_namespace = self._get_full_config()['model_namespace']
            except:
                print("")
                print("Model needs to be configured with 'pio init-model'.")
                print("")
                return

        if not model_name:
            try:
                model_name = self._get_full_config()['model_name']
            except:
                print("")
                print("Model needs to be configured with 'pio init-model'.")
                print("")
                return

        if not model_version:
            try:
                model_version = self._get_full_config()['model_version']
            except:
                print("")
                print("Model needs to be configured with 'pio init-model'.")
                print("")
                return

        if not model_path:
            try:
                model_path = self._get_full_config()['model_path']
            except:
                print("")
                print("Model needs to be configured with 'pio init-model'.")
                print("")
                return

        model_path = os.path.expandvars(model_path)
        model_path = os.path.expanduser(model_path)
        model_path = os.path.abspath(model_path)

        print('model_server_url: %s' % model_server_url)
        print('model_type: %s' % model_type)
        print('model_namespace: %s' % model_namespace)
        print('model_name: %s' % model_name)
        print('model_version: %s' % model_version)
        print('model_path: %s' % model_path)

        if (os.path.isdir(model_path)):
            compressed_model_bundle_filename = 'bundle-%s-%s-%s-%s.tar.gz' % (model_type, model_namespace, model_name, model_version)

            print("")
            print("Compressing model bundle '%s' into '%s'." % (model_path, compressed_model_bundle_filename))  
            print("")
            with tarfile.open(compressed_model_bundle_filename, 'w:gz') as tar:
                tar.add(model_path, arcname='.')
            model_file = compressed_model_bundle_filename
            upload_key = 'file'
            upload_value = compressed_model_bundle_filename
        else:
            print("")
            print("Model path must be a directory.  Note:  You can use .pioignore within the directory to keep files from being uploaded.")
            print("")
            return

        full_model_url = "%s/api/%s/model/deploy/%s/%s/%s/%s" % (model_server_url.rstrip('/'), pio_api_version, model_type, model_namespace, model_name, model_version)

        with open(model_file, 'rb') as fh:
            files = [(upload_key, (upload_value, fh))]
            print("")
            print("Deploying model '%s' to '%s'." % (model_file, full_model_url))
            print("")
            headers = {'Accept': 'application/json'}
            try:
                response = requests.post(url=full_model_url, 
                                         headers=headers, 
                                         files=files, 
                                         timeout=600)
                if response.text:
                    pprint(response.text)
                print("")
                print("Success!")
                print("")
                print("Predict with 'pio predict' or POST to '%s'" % full_model_url.replace('/deploy/','/predict/'))
                print("")
            except IOError as e:
                print("Error while deploying model.\nError: '%s'" % str(e))
                print("")
 
        if (os.path.isdir(model_path)):
            print("")
            print("Cleaning up compressed model bundle '%s'..." % model_file)
            print("")
            os.remove(model_file)


    def predict(self,
                model_server_url=None,
                model_type=None,
                model_namespace=None,
                model_name=None,
                model_version=None,
                model_test_request_path=None,
                model_input_mime_type=None,
                model_output_mime_type=None):

        pio_api_version = self._get_full_config()['pio_api_version']

        if not model_server_url:
            try:
                model_server_url = self._get_full_config()['model_server_url']
            except:
                print("")
                print("Model needs to be configured with 'pio init-model'.")
                print("")
                return

        if not model_type:
            try:
                model_type = self._get_full_config()['model_type']
            except:
                print("")
                print("Model needs to be configured with 'pio init-model'.")
                print("")
                return

        if not model_namespace:
            try:
                model_namespace = self._get_full_config()['model_namespace']
            except:
                print("")
                print("Model needs to be configured with 'pio init-model'.")
                print("")
                return

        if not model_name:
            try:
                model_name = self._get_full_config()['model_name']
            except:
                print("")
                print("Model needs to be configured with 'pio init-model'.")
                print("")
                return

        if not model_version:
            try:
                model_version = self._get_full_config()['model_version']
            except:
                print("")
                print("Model needs to be configured with 'pio init-model'.")
                print("")
                return

        if not model_test_request_path:
            try:
                model_test_request_path = self._get_full_config()['model_test_request_path']
            except:
                print("")
                print("Model needs to be configured with 'pio init-model'.")
                print("")
                return

        if model_test_request_path:
            model_test_request_path = os.path.expandvars(model_test_request_path)
            model_test_request_path = os.path.expanduser(model_test_request_path)
            model_test_request_path = os.path.abspath(model_test_request_path)

        if not model_input_mime_type:
            try:
                model_input_mime_type = self._get_full_config()['model_input_mime_type']
            except:
                print("")
                print("Model needs to be configured with 'pio init-model'.")
                print("")
                return

        if not model_output_mime_type:
            try:
                model_output_mime_type = self._get_full_config()['model_output_mime_type']
            except:
                print("")
                print("Model needs to be configured with 'pio init-model'.")
                print("")
                return

        print('model_server_url: %s' % model_server_url)
        print('model_type: %s' % model_type)
        print('model_namespace: %s' % model_namespace)
        print('model_name: %s' % model_name)
        print('model_version: %s' % model_version)
        print('model_test_request_path: %s' % model_test_request_path)
        print('model_input_mime_type: %s' % model_input_mime_type)
        print('model_output_mime_type: %s' % model_output_mime_type)

        full_model_url = "%s/api/%s/model/predict/%s/%s/%s/%s" % (model_server_url.rstrip('/'), pio_api_version, model_type, model_namespace, model_name, model_version)
        print("")
        print("Predicting with file '%s' using '%s'" % (model_test_request_path, full_model_url))
        print("")
        with open(model_test_request_path, 'rb') as fh:
            model_input_binary = fh.read()

        headers = {'Content-type': model_input_mime_type, 'Accept': model_output_mime_type} 
        from datetime import datetime 

        begin_time = datetime.now()
        response = requests.post(url=full_model_url, 
                                 headers=headers, 
                                 data=model_input_binary, 
                                 timeout=30)
        end_time = datetime.now()
        pprint(response.text)
        print("")
        total_time = end_time - begin_time
        print("Total time: %s milliseconds" % (total_time.microseconds / 1000))
        print("")


    def predict_many(self,
                     num_iterations,
                     model_server_url=None,
                     model_type=None,
                     model_namespace=None,
                     model_name=None,
                     model_version=None,
                     model_test_request_path=None,
                     model_input_mime_type=None,
                     model_output_mime_type=None):

        p = Pool(num_iterations)
        for _ in range(num_iterations):        
            p.apply(self.predict, (model_server_url,
                                   model_type,
                                   model_namespace,
                                   model_name,
                                   model_version,
                                   model_test_request_path,
                                   model_input_mime_type,
                                   model_output_mime_type),
               )
        print("")
        print("This currently only works with Python 3.")
        print("")


    def cluster(self):
        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        self.apps()

        print("DNS Internal (Public)")
        print("*********************")
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_service_for_all_namespaces(watch=False, 
                                                                     pretty=True)
            services = response.items
            for svc in services:
                ingress = 'Not public' 
                if svc.status.load_balancer.ingress and len(svc.status.load_balancer.ingress) > 0:
                    if (svc.status.load_balancer.ingress[0].hostname):
                        ingress = svc.status.load_balancer.ingress[0].hostname
                    if (svc.status.load_balancer.ingress[0].ip):
                        ingress = svc.status.load_balancer.ingress[0].ip               
                print("%s (%s)" % (svc.metadata.name, ingress))

        print("")
        print("Containers (Pods)")
        print("****************")
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_pod_for_all_namespaces(watch=False, 
                                                                 pretty=True)
            pods = response.items
            for pod in pods:
                print("%s (%s)" % (pod.metadata.name, pod.status.phase))

        print("")
        print("Nodes")
        print("*****")
        self._get_all_nodes()
        
        print("")
        print("Config")
        print("******")
        pprint(self._get_full_config())
        print("")


    def _get_pod_by_app_name(self,
                             app_name):

        pio_api_version = self._get_full_config()['pio_api_version']
        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        found = False 
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_pod_for_all_namespaces(watch=False, pretty=True)
            pods = response.items
            for pod in pods:
                if app_name in pod.metadata.name:
                    found = True
                    break
        if found:
            return pod
        else:
            return None


    def _get_svc_by_app_name(self,
                             app_name):

        pio_api_version = self._get_full_config()['pio_api_version']
        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        found = False
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_service_for_all_namespaces(watch=False, 
                                                                     pretty=True)
            services = response.items
            for svc in services:
                if app_name in svc.metadata.name:
                    found = True
                    break
        if found:
            return svc 
        else:
            return None


    def _get_all_available_apps(self):
        pio_api_version = self._get_full_config()['pio_api_version']

        available_apps = list(PioCli._kube_deploy_registry.keys())
        available_apps.sort()
        for app in available_apps:
            print(app)


    def nodes(self):
        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        print("")
        print("Nodes")
        print("*****")
        self._get_all_nodes()
        print("")


    def _get_all_nodes(self):
        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_node(watch=False, pretty=True)
            nodes = response.items
            for node in nodes:
                print("%s" % node.metadata.labels['kubernetes.io/hostname'])


    def apps(self):
        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        print("")
        print("Available Apps")
        print("**************")
        self._get_all_available_apps()

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        print("")
        print("Running Apps")
        print("************")
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1_beta1.list_deployment_for_all_namespaces(watch=False,
                                                                              pretty=True)
            deployments = response.items
            for deploy in deployments:
                print("%s (%s of %s replicas are running)" % (deploy.metadata.name, deploy.status.ready_replicas, deploy.status.replicas))
        print("")
   

    def shell(self,
              app_name):

        self.connect(app_name)

 
    def connect(self,
                app_name):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_pod_for_all_namespaces(watch=False, 
                                                                 pretty=True)
            pods = response.items
            for pod in pods:
                if app_name in pod.metadata.name:
                    break
            print("")
            print("Connecting to '%s'" % pod.metadata.name)      
            print("")
            subprocess.call("kubectl exec -it %s bash" % pod.metadata.name, shell=True)
        print("")


    def logs(self,
             app_name):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_pod_for_all_namespaces(watch=False, 
                                                                 pretty=True)
            found = False
            pods = response.items
            for pod in pods:
                if app_name in pod.metadata.name:
                    found = True
                    break
            if found:
                print("")
                print("Tailing logs on '%s'." % pod.metadata.name)
                print("")
                subprocess.call("kubectl logs -f %s" % pod.metadata.name, shell=True)
                print("")
            else:
                print("")
                print("App '%s' is not running." % app_name)
                print("")


    def scale(self,
              app_name,
              replicas):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1_beta1.list_deployment_for_all_namespaces(watch=False, 
                                                                              pretty=True)
            found = False
            deployments = response.items
            for deploy in deployments:
                if app_name in deploy.metadata.name:
                    found = True
                    break
            if found:
                print("")
                print("Scaling app '%s' to '%s' replicas." % (deploy.metadata.name, replicas))
                print("")
                kube_cmd = "kubectl scale deploy %s --replicas=%s" % (deploy.metadata.name, replicas)
                print("Running '%s'." % kube_cmd)
                print("")
                subprocess.call(kube_cmd, shell=True)
                print("")
                print("Check status with 'pio cluster'.")
                print("")
            else:
                print("")
                print("App '%s' is not running." % app_name)
                print("") 


    def volumes(self):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        print("")
        print("Volumes")
        print("*******")
        self._get_all_volumes()

        print("")
        print("Volume Claims")
        print("*************")
        self._get_all_volume_claims()
        print("")


    def _get_all_volumes(self):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_persistent_volume(watch=False,
                                                            pretty=True)
            claims = response.items
            for claim in claims:
                print("%s" % (claim.metadata.name))
        print("")


    def _get_all_volume_claims(self):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_persistent_volume_claim_for_all_namespaces(watch=False,
                                                                                     pretty=True)
            claims = response.items
            for claim in claims:
                print("%s" % (claim.metadata.name))
        print("")


    def _get_config_yamls(self, 
                         app_name):
        return [] 


    def _get_secret_yamls(self, 
                         app_name):
        return []


    def _get_deploy_yamls(self, 
                         app_name):
        try:
            (deploy_yamls, dependencies) = PioCli._kube_deploy_registry[app_name]
        except:
            dependencies = []
            deploy_yamls = []

        if len(dependencies) > 0:
            for dependency in dependencies:
                deploy_yamls = deploy_yamls + self._get_deploy_yamls(dependency)
        return deploy_yamls 


    def _get_svc_yamls(self, 
                      app_name):
        try:
            (svc_yamls, dependencies) = PioCli._kube_svc_registry[app_name]
        except:
            dependencies = []
            svc_yamls = []
       
        if len(dependencies) > 0:
            for dependency in dependencies:
                svc_yamls = svc_yamls + self._get_svc_yamls(dependency)
        return svc_yamls


    def start(self,
              app_name):

        pio_api_version = self._get_full_config()['pio_api_version']

        try: 
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        try:
            pio_git_home = self._get_full_config()['pio_git_home']

            if 'http:' in pio_git_home or 'https:' in pio_git_home:
                pass
            else:
                pio_git_home = os.path.expandvars(pio_git_home)
                pio_git_home = os.path.expanduser(pio_git_home)
                pio_git_home = os.path.abspath(pio_git_home)

            pio_git_version = self._get_full_config()['pio_git_version']
        except:
            print("")
            print("PipelineIO needs to be configured with 'pio init-pio'.")
            print("")
            return

        config_yaml_filenames = [] 
        secret_yaml_filenames = [] 
        deploy_yaml_filenames = []
        svc_yaml_filenames = [] 
       
        config_yaml_filenames = config_yaml_filenames + self._get_config_yamls(app_name)
        secret_yaml_filenames = secret_yaml_filenames + self._get_secret_yamls(app_name)
        deploy_yaml_filenames = deploy_yaml_filenames + self._get_deploy_yamls(app_name)
        #print(deploy_yaml_filenames)
 
        svc_yaml_filenames = svc_yaml_filenames + self._get_svc_yamls(app_name)
        #print(svc_yaml_filenames)

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        #for config_yaml_filename in config_yaml_filenames:
            # TODO
        #    return 

        #for secret_yaml_filename in secret_yaml_filenames:
            # TODO 
        #    return
        print("")
        print("Starting app '%s'." % app_name)
        print("")
        print("Kubernetes Deployments:")
        print("")
        for deploy_yaml_filename in deploy_yaml_filenames:
            try:
                if 'http:' in deploy_yaml_filename or 'https:' in deploy_yaml_filename:
                    deploy_yaml_filename = deploy_yaml_filename.replace('github.com', 'raw.githubusercontent.com')
                    kube_cmd = "kubectl create -f %s --record" % deploy_yaml_filename
                    print("Running '%s'." % kube_cmd)
                    print("")
                    subprocess.call(kube_cmd, shell=True)
                    print("")
                else:
                    if 'http:' in pio_git_home or 'https:' in pio_git_home:
                        pio_git_home = pio_git_home.replace('github.com', 'raw.githubusercontent.com')
                        kube_cmd = "kubectl create -f %s/%s/%s --record" % (pio_git_home.rstrip('/'), pio_git_version, deploy_yaml_filename)
                        print("Running '%s'." % kube_cmd)
                        print("")
                        subprocess.call(kube_cmd, shell=True)
                        print("")
                    else:
                        with open(os.path.join(pio_git_home, deploy_yaml_filename)) as fh:
                            deploy_yaml = yaml.load(fh)
                            with warnings.catch_warnings():
                                warnings.simplefilter("ignore")
                                response = kubeclient_v1_beta1.create_namespaced_deployment(body=deploy_yaml, 
                                                                                            namespace=kube_namespace, 
                                                                                            pretty=True)
                                pprint(response) 
            except ApiException as e: 
                print("")
                print("App '%s' did not start properly.\n%s" % (deploy_yaml_filename, str(e)))
                print("")

        print("")
        print("Kubernetes Services:")
        print("")
        for svc_yaml_filename in svc_yaml_filenames:
            try:
                if 'http:' in svc_yaml_filename or 'https:' in svc_yaml_filename:
                    svc_yaml_filename = svc_yaml_filename.replace('github.com', 'raw.githubusercontent.com')
                    kube_cmd = "kubectl create -f %s --record" % svc_yaml_filename
                    print("Running '%s'." % kube_cmd)
                    print("")
                    subprocess.call(kube_cmd, shell=True)
                    print("")
                else:
                    if 'http:' in pio_git_home or 'https:' in pio_git_home:
                        pio_git_home = pio_git_home.replace('github.com', 'raw.githubusercontent.com')
                        kube_cmd = "kubectl create -f %s/%s/%s --record" % (pio_git_home.rstrip('/'), pio_git_version, svc_yaml_filename)
                        print("Running '%s'." % kube_cmd)
                        print("")
                        subprocess.call(kube_cmd, shell=True)
                        print("")
                    else:
                        with open(os.path.join(pio_git_home, svc_yaml_filename)) as fh:
                            svc_yaml = yaml.load(fh)
                            with warnings.catch_warnings():
                                warnings.simplefilter("ignore")
                                response = kubeclient_v1.create_namespaced_service(body=svc_yaml, 
                                                                                   namespace=kube_namespace, 
                                                                                   pretty=True)
                                pprint(response)
            except ApiException as e: 
                print("")
                print("App '%s' did not start properly.\n%s" % (svc_yaml_filename, str(e)))
                print("")

        print("")
        print("Ignore any 'Already Exists' errors.  These are OK.")
        print("")
        print("Check app status with 'pio apps' or 'pio cluster'.")
        print("")


    def stop(self,
             app_name):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("")
            print("Cluster needs to be configured with 'pio init-cluster'.")
            print("")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1_beta1.list_deployment_for_all_namespaces(watch=False, pretty=True)
            found = False
            deployments = response.items
            for deploy in deployments:
                if app_name in deploy.metadata.name:
                    found = True
                    break
            if found:
                print("")
                print("Stopping app '%s'." % deploy.metadata.name)
                print("")
                kube_cmd = "kubectl delete deploy %s" % deploy.metadata.name
                print("Running '%s'." % kube_cmd)
                print("")
                subprocess.call(kube_cmd, shell=True)
                print("")
                print("Check app status with 'pio apps' or 'pio cluster'.")
                print("")
            else:
                print("")
                print("App '%s' is not running." % app_name)
                print("")


def main():
    fire.Fire(PioCli)



### `PioModelInitializer` Class
# Must implement the `initialize_model()` method.
class PioModelInitializer(object):
    def __init__(self, 
                 *args,
                 **kwargs):        

        pass

    
    def initialize_model(self,
                        *args,
                        **kwargs):

        return


### `PioRequestTransformer` Class
#Must implement the `transform_request()` method.
class PioRequestTransformer(object):
    def __init__(self, 
                 *args,
                 **kwargs):        
        pass
    
    
    def transform_request(self,
                          request,
                          *args,
                          **kwargs):
        return request


### `PioResponseTransformer` Class
# Must implement the `transform_response()` method.
class PioResponseTransformer(object):
    def __init__(self, 
                 *args,
                 **kwargs):        
        pass
    
    
    def transform_response(self,
                           response,
                           *args,
                           **kwargs):
        return response


### `PioModel` Class
# Must implement the `predict()` method.
class PioModel(object):

    def __init__(self, 
                 request_transformer, 
                 response_transformer,
                 model_initializer,
                 *args,
                 **kwargs):

        self.request_transformer = request_transformer
        self.response_transformer = response_transformer

        self.model_initializer = model_initializer
        self.model = self.model_initializer.initialize_model(args,
                                                             kwargs)

        
    def predict(self, 
                request,
                *args,
                **kwargs):

        return




if __name__ == '__main__':
    main()
