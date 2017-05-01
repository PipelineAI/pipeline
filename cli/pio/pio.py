#-*- coding: utf-8 -*-

__version__ = "0.30"

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
from git import Repo
from pprint import pprint
import subprocess

# TODO: enums
#   model_input_mime_type = ['application/xml', 'application/json']
#   model_output_mime_type = ['application/json', 'text/plain']

# References:
#   https://github.com/kubernetes-incubator/client-python/blob/master/kubernetes/README.md

class PioCli(object):
    _kube_deploy_registry = {'jupyter': (['jupyterhub.ml/jupyterhub-deploy.yaml'], []),
                            'spark': (['apachespark.ml/master-deploy.yaml'], ['spark-worker', 'metastore']),
                            'spark-worker': (['apachespark.ml/worker-deploy.yaml'], []),
                            'metastore': (['metastore.ml/metastore-deploy.yaml'], ['mysql']),
                            'hdfs': (['hdfs.ml/namenode-deploy.yaml'], []),
                            'redis': (['keyvalue.ml/redis-master-deploy.yaml'], []),
                            'presto': (['presto.ml/presto-master-deploy.yaml',
                                        'presto.ml/presto-worker-deploy.yaml'], ['metastore']),
                            'presto-ui': (['presto.ml/presto-ui-deploy.yaml'], ['presto']),
                            'airflow': (['scheduler.ml/airflow-deploy.yaml'], ['mysql', 'redis']),
                            'mysql': (['sql.ml/mysql-master-deploy.yaml'], []),
                            'www': (['web.ml/home-deploy.yaml'], []),
                            'zeppelin': (['zeppelin.ml/zeppelin-deploy.yaml'], []),
                            'zookeeper': (['zookeeper.ml/zookeeper-deploy.yaml'], []),
                            'kafka': (['stream.ml/kafka-0.10-deploy.yaml'], ['zookeeper']),
                            'cassandra': (['cassandra.ml/cassandra-deploy.yaml'], []),
                            'prediction-jvm': (['prediction.ml/jvm-deploy.yaml'], []),
                            'prediction-python3': (['prediction.ml/python3-deploy.yaml'], []),
                            'prediction-tensorflow': (['prediction.ml/tensorflow-deploy.yaml'], []),
                            'turbine': (['dashboard.ml/turbine-deploy.yaml'], []),
                            'hystrix': (['dashboard.ml/hystrix-deploy.yaml'], []),
                            'weavescope': (['dashboard.ml/weavescope/weavescope.yaml'], []),
                            'dashboard': (['https://raw.githubusercontent.com/kubernetes/kops/master/addons/kubernetes-dashboard/v1.6.0.yaml'], []),
                            'heapster': (['https://raw.githubusercontent.com/kubernetes/kops/master/addons/monitoring-standalone/v1.3.0.yaml'], []),
                            'route53': (['https://raw.githubusercontent.com/kubernetes/kops/master/addons/route53-mapper/v1.3.0.yml'], []),
                           }

    _kube_svc_registry = {'jupyter': (['jupyterhub.ml/jupyterhub-svc.yaml'], []),
                         'spark': (['apachespark.ml/master-svc.yaml'], ['spark-worker', 'metastore']), 
                         'spark-worker': (['apachespark.ml/worker-svc.yaml'], []),
                         'metastore': (['metastore.ml/metastore-svc.yaml'], ['mysql']),
                         'hdfs': (['hdfs.ml/namenode-svc.yaml'], []),
                         'redis': (['keyvalue.ml/redis-master-svc.yaml'], []),
                         'presto': (['presto.ml/presto-master-svc.yaml',
                                     'presto.ml/presto-worker-svc.yaml'], ['metastore']),
                         'presto-ui': (['presto.ml/presto-ui-svc.yaml'], ['presto']),
                         'airflow': (['scheduler.ml/airflow-svc.yaml'], ['mysql', 'redis']),
                         'mysql': (['sql.ml/mysql-master-svc.yaml'], []),
                         'www': (['web.ml/home-svc.yaml'], []),
                         'zeppelin': (['zeppelin.ml/zeppelin-svc.yaml'], []),
                         'zookeeper': (['zookeeper.ml/zookeeper-svc.yaml'], []),
                         'kafka': (['stream.ml/kafka-0.10-svc.yaml'], ['zookeeper']),
                         'cassandra': (['cassandra.ml/cassandra-svc.yaml'], []),
                         'prediction-jvm': (['prediction.ml/jvm-svc.yaml'], []),
                         'prediction-python3': (['prediction.ml/python3-svc.yaml'], []),
                         'prediction-tensorflow': (['prediction.ml/tensorflow-svc.yaml'], []),
                        }

    def _pio_api_version(self):
        return 'v1'


    def get_config_value(self,
                         config_key):
        print("")
        pprint(self._get_full_config())
        print("")
        return self._get_full_config()[config_key]


    def set_config_value(self,
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

        config_file_base_path = os.path.expanduser("~/.pio/")
        expanded_config_file_base_path = os.path.expandvars(config_file_base_path)
        expanded_config_file_base_path = os.path.expanduser(expanded_config_file_base_path)
        expanded_config_file_base_path = os.path.abspath(expanded_config_file_base_path)
        expanded_config_file_path = os.path.join(expanded_config_file_base_path, 'config')

        pprint("Merging dict '%s' with existing config '%s'..." % (config_dict, expanded_config_file_path))

        existing_config_dict = self._get_full_config()

        # >= Python3.5 
        # {**existing_config_dict, **config_dict}
        existing_config_dict.update(config_dict)

        new_config_yaml = yaml.dump(existing_config_dict, default_flow_style=False, explicit_start=True)

        with open(expanded_config_file_path, 'w') as fh:
            fh.write(new_config_yaml)
        print(new_config_yaml)
        print("...Done!")
        print("")


    def _get_full_config(self):
        config_file_base_path = os.path.expanduser("~/.pio/")
        config_file_base_path = os.path.expandvars(config_file_base_path)
        config_file_base_path = os.path.expanduser(config_file_base_path)
        config_file_base_path = os.path.abspath(config_file_base_path)
        config_file_filename = os.path.join(config_file_base_path, 'config')

        if not os.path.exists(config_file_filename):
            if not os.path.exists(config_file_base_path):
                os.makedirs(config_file_base_path)
            pio_api_version = self._pio_api_version() 
            initial_config_dict = {'pio_api_version': pio_api_version}
            initial_config_yaml =  yaml.dump(initial_config_dict, default_flow_style=False, explicit_start=True)
            print("Creating config '%s'..." % config_file_filename)
            with open(config_file_filename, 'w') as fh:
                fh.write(initial_config_yaml)
            print("...Done!")
        print("")

        # Update the YAML 
        with open(config_file_filename, 'r') as fh:
            existing_config_dict = yaml.load(fh)
            return existing_config_dict


    def view_config(self):
        pprint(self._get_full_config())
        print("")


    def top_cluster(self):
        subprocess.call("kubectl top node", shell=True)
        print("")
        print("Note:  Heapster must be deployed to support this command ^^.")
        print("")


    def top_app(self,
                app_name):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("Cluster needs to be configured.")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()
        
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_namespaced_pod(namespace=kube_namespace, watch=False, pretty=True)
            for pod in response.items:
                if (app_name in pod.metadata.name):
                    subprocess.call("kubectl top pod %s" % pod.metadata.name, shell=True)
        print("")


    def configure_app(self,
                      pio_home,
                      pio_runtime_version):

        pio_api_version = self._get_full_config()['pio_api_version']

        if 'http:' not in pio_home and 'https:' not in pio_home:
            pio_home = os.path.expandvars(pio_home)
            pio_home = os.path.expanduser(pio_home)
            pio_home = os.path.abspath(pio_home)

        config_dict = {'pio_home': pio_home,
                       'pio_runtime_version': pio_runtime_version}

        self._merge_config_dict(config_dict)
        print("")
        pprint(self._get_full_config())
        print("")


    def configure_cluster(self,
                          kube_cluster_context,
                          kube_namespace='default'):

        pio_api_version = self._get_full_config()['pio_api_version']

        config_dict = {'kube_cluster_context': kube_cluster_context, 
                       'kube_namespace': kube_namespace}
        self._merge_config_dict(config_dict)
        print("")
        pprint(self._get_full_config())
        print("")


    def configure_model(self,
                        model_server_url,
                        model_type,
                        model_namespace,
                        model_name,
                        model_input_mime_type='application/json',
                        model_output_mime_type='application/json'):

        pio_api_version = self._get_full_config()['pio_api_version']

        config_dict = {"model_server_url": model_server_url, 
                       "model_type": model_type,
                       "model_namespace": model_namespace,
                       "model_name": model_name,
                       "model_input_mime_type": model_input_mime_type,
                       "model_output_mime_type": model_output_mime_type,
        }

        self._merge_config_dict(config_dict)
        print("")
        pprint(self._get_full_config())
        print("")


    def deploy_model(self,
                     model_version, 
                     model_path,
                     request_timeout=600):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            model_server_url = self._get_full_config()['model_server_url']
            model_type = self._get_full_config()['model_type']
            model_namespace = self._get_full_config()['model_namespace']
            model_name = self._get_full_config()['model_name']
        except:
            print("Model needs to be configured.")
            return

        model_path = os.path.expandvars(model_path)
        model_path = os.path.expanduser(model_path)
        model_path = os.path.abspath(model_path)

        print('model_version: %s' % model_version)
        print('model_path: %s' % model_path)
        print('request_timeout: %s' % request_timeout)

        if (os.path.isdir(model_path)):
            compressed_model_bundle_filename = 'bundle-%s-%s-%s-%s.tar.gz' % (model_type, model_namespace, model_name, model_version)

            print("Compressing model '%s' into '%s'..." % (model_path, compressed_model_bundle_filename))  
            print("")
            with tarfile.open(compressed_model_bundle_filename, 'w:gz') as tar:
                tar.add(model_path, arcname='.')
            print("...Done!")
            model_file = compressed_model_bundle_filename
            upload_key = 'bundle'
            upload_value = compressed_model_bundle_filename
        else:
            model_file = model_path
            upload_key = 'file'
            upload_value = os.path.split(model_path)

        with open(model_file, 'rb') as fh:
            files = [(upload_key, (upload_value, fh))]

            full_model_server_url = "%s/%s/model/deploy/%s/%s/%s/%s" % (model_server_url, pio_api_version, model_type, model_namespace, model_name, model_version)
            print("Deploying model '%s' to '%s'..." % (model_file, full_model_server_url))
            print("")
            headers = {'Accept': 'application/json'}
            response = requests.post(url=full_model_server_url, 
                                     headers=headers, 
                                     files=files, 
                                     timeout=request_timeout)
            pprint(response.text)
            print("...Done!")

        if (os.path.isdir(model_path)):
            print("Cleaning up compressed model '%s'..." % model_file)
            os.remove(model_file)
            print("...Done!")
        print("")


    def predict_model(self, 
                      model_version, 
                      model_input_filename,
                      request_timeout=30):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            model_server_url = self._get_full_config()['model_server_url']
            model_type = self._get_full_config()['model_type']
            model_namespace = self._get_full_config()['model_namespace']
            model_name = self._get_full_config()['model_name']
            model_input_mime_type = self._get_full_config()['model_input_mime_type']
            model_output_mime_type = self._get_full_config()['model_output_mime_type']
        except:
            print("Model needs to be configured.")
            return

        print('model_version: %s' % model_version)
        print('model_input_filename: %s' % model_input_filename)
        print('request_timeout: %s' % request_timeout)

        full_model_server_url = "%s/%s/model/predict/%s/%s/%s/%s" % (model_server_url, pio_api_version, model_type, model_namespace, model_name, model_version)

        print("Predicting file '%s' with model '%s/%s/%s/%s' at '%s'..." % (model_input_filename, model_type, model_namespace, model_name, model_version, full_model_server_url))
        print("")
        with open(model_input_filename, 'rb') as fh:
            model_input_binary = fh.read()

        headers = {'Content-type': model_input_mime_type, 'Accept': model_output_mime_type} 
        response = requests.post(url=full_model_server_url, 
                                 headers=headers, 
                                 data=model_input_binary, 
                                 timeout=request_timeout)
        pprint(response.text)
        print("")
        print("...Done!")
        print("")


    def view_cluster(self):
        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("Cluster needs to be configured.")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        self.view_apps()

        print("DNS Internal :: DNS Public")
        print("**************************")
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_namespaced_service(namespace=kube_namespace, watch=False, pretty=True)
            for svc in response.items:
                ingress = 'Not public' 
                if svc.status.load_balancer.ingress and len(svc.status.load_balancer.ingress) > 0:
                    if (svc.status.load_balancer.ingress[0].hostname):
                        ingress = svc.status.load_balancer.ingress[0].hostname
                    if (svc.status.load_balancer.ingress[0].ip):
                        ingress = svc.status.load_balancer.ingress[0].ip               
                print("%s :: %s" % (svc.metadata.name, ingress))

        print("")
        print("Running Pods")
        print("************")
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_namespaced_pod(namespace=kube_namespace, watch=False, pretty=True)
            for pod in response.items:
                print("%s (%s)" % (pod.metadata.name, pod.status.phase))
        print("")

        print("Config")
        print("******")
        pprint(self._get_full_config())
        print("")


    def view_available_apps(self):
        print("")
        print("Available Apps")
        print("**************")
        pio_api_version = self._get_full_config()['pio_api_version']
        available_apps = PioCli._kube_deploy_registry.keys()
        for app in available_apps:
            print(app)
        self.view_apps()


    def view_apps(self):
        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("Cluster needs to be configured.")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        print("")
        print("Running Apps")
        print("************")
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1_beta1.list_namespaced_deployment(namespace=kube_namespace,
                                                                      watch=False,
                                                                      pretty=True)
            for deploy in response.items:
                print("%s (%s of %s replicas available)" % (deploy.metadata.name, deploy.status.ready_replicas, deploy.status.replicas))
        print("")
   
 
    def shell_app(self,
                  app_name):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("Cluster needs to be configured.")
            return

        pprint(self._get_full_config())

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_namespaced_pod(namespace=kube_namespace, watch=False, pretty=True)
            for pod in response.items:
                if app_name in pod.metadata.name:
                    break
            print("Shelling into '%s'" % pod.metadata.name)      
            subprocess.call("kubectl exec -it %s bash" % pod.metadata.name, shell=True)
        print("")


    def tail_app(self,
                 app_name):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_cluster_context = self._get_full_config()['kube_cluster_context']
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("Cluster needs to be configured.")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_namespaced_pod(namespace=kube_namespace, watch=False, pretty=True)
            found = False
            for pod in response.items:
                if app_name in pod.metadata.name:
                    found = True
                    break
            if found:
                print("Tailing logs on '%s'" % pod.metadata.name)
                subprocess.call("kubectl logs -f %s" % pod.metadata.name, shell=True)
            else:
                print("App '%s' is not running." % app_name)
        print("")


    def scale_app(self,
                  app_name,
                  replicas):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("Cluster needs to be configured.")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1_beta1.list_namespaced_deployment(namespace=kube_namespace, watch=False, pretty=True)
            found = False
            for deploy in response.items:
                if app_name in deploy.metadata.name:
                    found = True
                    break
            if found:
                print("Scaling '%s' to '%s' replicas..." % (deploy.metadata.name, replicas))
                subprocess.call("kubectl scale deploy %s --replicas=%s" % (deploy.metadata.name, replicas), shell=True)
                print("...Done!")
                self.view_cluster()
                print("Note:  There may be a delay in the status change above ^^.")
            else:
                print("App '%s' is not running." % app_name)
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


    def deploy_app(self,
                   app_name):

        pio_api_version = self._get_full_config()['pio_api_version']

        try: 
            kube_namespace = self._get_full_config()['kube_namespace']

            pio_home = self._get_full_config()['pio_home']

            if 'http:' in pio_home or 'https:' in pio_home:
                pass
            else:
                pio_home = os.path.expandvars(pio_home)
                pio_home = os.path.expanduser(pio_home)
                pio_home = os.path.abspath(pio_home)

            pio_runtime_version = self._get_full_config()['pio_runtime_version']
        except:
            print("Cluster needs to be configured.")
            return

        config_yaml_filenames = [] 
        secret_yaml_filenames = [] 
        deploy_yaml_filenames = []
        svc_yaml_filenames = [] 
       
        config_yaml_filenames = config_yaml_filenames + self._get_config_yamls(app_name)
        secret_yaml_filenames = secret_yaml_filenames + self._get_secret_yamls(app_name)
        deploy_yaml_filenames = deploy_yaml_filenames + self._get_deploy_yamls(app_name)
        svc_yaml_filenames = svc_yaml_filenames + self._get_svc_yamls(app_name)

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        #for config_yaml_filename in config_yaml_filenames:
            # TODO
        #    return 

        #for secret_yaml_filename in secret_yaml_filenames:
            # TODO 
        #    return

        print("Deploying app '%s'..." % app_name)
        for deploy_yaml_filename in deploy_yaml_filenames:
            try:
                if 'http:' in deploy_yaml_filename or 'https:' in deploy_yaml_filename:
                    subprocess.call("kubectl create -f %s" % deploy_yaml_filename, shell=True)
                else:
                    if 'http:' in pio_home or 'https:' in pio_home:
                        subprocess.call("kubectl create -f %s/%s/%s" % (pio_home.rstrip('/'), pio_runtime_version, deploy_yaml_filename), shell=True)
                    else:
                        with open(os.path.join(pio_home, deploy_yaml_filename)) as fh:
                            deploy_yaml = yaml.load(fh)
                            with warnings.catch_warnings():
                                warnings.simplefilter("ignore")
                                response = kubeclient_v1_beta1.create_namespaced_deployment(body=deploy_yaml, 
                                                                                            namespace=kube_namespace, 
                                                                                            pretty=True)
                                pprint(response) 
            except ApiException as e: 
                print("App not deployed for '%s':\n%s\n" % (deploy_yaml_filename, str(e)))

        for svc_yaml_filename in svc_yaml_filenames:
            try:
                if 'http:' in svc_yaml_filename or 'https:' in svc_yaml_filename:
                    subprocess.call("kubectl create -f %s" % svc_yaml_filename, shell=True)
                else:
                    if 'http:' in pio_home or 'https:' in pio_home:
                        subprocess.call("kubectl create -f %s/%s/%s" % (pio_home, pio_runtime_version, svc_yaml_filename), shell=True)
                    else:
                        with open(os.path.join(pio_home, svc_yaml_filename)) as fh:
                            svc_yaml = yaml.load(fh)
                            with warnings.catch_warnings():
                                warnings.simplefilter("ignore")
                                response = kubeclient_v1.create_namespaced_service(body=svc_yaml, 
                                                                                   namespace=kube_namespace, 
                                                                                   pretty=True)
                                pprint(response)
            except ApiException as e: 
                print("Service not created for '%s':\n%s\n" % (svc_yaml_filename, str(e)))

        print("...Done!")
        self.view_cluster()
        print("Note:  There may be a delay in the status change above ^^.")
        print("")


    def undeploy_app(self,
                     app_name):

        pio_api_version = self._get_full_config()['pio_api_version']

        try:
            kube_namespace = self._get_full_config()['kube_namespace']
        except:
            print("Cluster needs to be configured.")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1_beta1.list_namespaced_deployment(namespace=kube_namespace, watch=False, pretty=True)
            found = False
            for deploy in response.items:
                if app_name in deploy.metadata.name:
                    found = True
                    break
            if found:
                print("Undeploying '%s'..." % deploy.metadata.name)
                subprocess.call("kubectl delete deploy %s" % deploy.metadata.name, shell=True)
                print("...Done!")
                self.view_cluster()
                print("Note:  There may be a delay in the status change above ^^.")
            else:
                print("App '%s' is not running." % app_name)
        print("")


#    def configure_git(self,
#                      git_repo_base_path,
#                      git_revision='HEAD'):

#        expanded_git_repo_base_path = os.path.expandvars(git_repo_base_path)
#        expanded_git_repo_base_path = os.path.expanduser(expanded_git_repo_base_path)
#        expanded_git_repo_base_path = os.path.abspath(expanded_git_repo_base_path)

#        pio_api_version = self._get_full_config()['pio_api_version']

#        print("git_repo_base_path: '%s'" % git_repo_base_path)
#        print("expanded_git_repo_base_path: '%s'" % expanded_git_repo_base_path)
#        print("git_revision: '%s'" % git_revision)

#        git_repo = Repo(expanded_git_repo_base_path, search_parent_directories=True)
 
#        config_dict = {'git_repo_base_path': git_repo.working_tree_dir , 'git_revision': git_revision}

#        self._merge_config_dict(config_dict)
#        pprint(self._get_full_config())
#        print("")

#    def view_git(self):
#        pio_api_version = self._get_full_config()['pio_api_version']
#        try: 
#            git_repo_base_path = self._get_full_config()['git_repo_base_path']

#            expanded_git_repo_base_path = os.path.expandvars(git_repo_base_path)
#            expanded_git_repo_base_path = os.path.expanduser(expanded_git_repo_base_path)
#            expanded_git_repo_base_path = os.path.abspath(expanded_git_repo_base_path)

#            git_revision = self._get_full_config()['git_revision']
#        except:
#            print("Git needs to be configured.")
#            return

#        pprint(self._get_full_config())

#        git_repo = Repo(expanded_git_repo_base_path, search_parent_directories=False)
#        ch = git_repo.commit(git_revision)

#        print("Git repo base path: '%s'" % git_repo_base_path)
#        print("Git revision: '%s'" % git_revision)
#        print("Git commit message: '%s'" % ch.message)
#        print("Git commit hash: '%s'" % ch.hexsha)
#        print("")


def main():
    fire.Fire(PioCli)


if __name__ == '__main__':
    main()
