# -*- coding: utf-8 -*-

__version__ = "0.27"

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
    kube_deploy_registry = {'jupyter': (['jupyterhub.ml/jupyterhub-deploy.yaml'], []),
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
                            'weavescope': (['dashboard.ml/weavescope.yaml'], []),
                            #'dashboard': (['dashboard.ml/kubernetes/kubernetes-dashboard.yaml'], []),
                            #'heapster': (['https://raw.githubusercontent.com/kubernetes/kops/master/addons/monitoring-standalone/v1.3.0.yaml']), []),
                            #'route53': (['https://raw.githubusercontent.com/kubernetes/kops/master/addons/route53-mapper/v1.3.0.yml']), []),
                           }

    kube_svc_registry = {'jupyter': (['jupyterhub.ml/jupyterhub-svc.yaml'], []),
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

    def pio_api_version(self):
        return 'v1'


    def config_get(self,
                   config_key):
        print(self.config_get_all()[config_key])
        print("\n")
        return self.config_get_all()[config_key]


    def config_set(self,
                   config_key,
                   config_value):
        self.config_merge_dict({config_key: config_value})


    def config_merge_dict(self, 
                          config_dict):

        pio_api_version = self.config_get_all()['pio_api_version']

        config_file_base_path = os.path.expanduser("~/.pio/")
        expanded_config_file_base_path = os.path.expandvars(config_file_base_path)
        expanded_config_file_base_path = os.path.expanduser(expanded_config_file_base_path)
        expanded_config_file_base_path = os.path.abspath(expanded_config_file_base_path)
        expanded_config_file_path = os.path.join(expanded_config_file_base_path, 'config')

        pprint("Merging dict '%s' with existing config '%s'..." % (config_dict, expanded_config_file_path))

        existing_config_dict = self.config_get_all()

        # >= Python3.5 
        # {**existing_config_dict, **config_dict}
        existing_config_dict.update(config_dict)

        new_config_yaml = yaml.dump(existing_config_dict, default_flow_style=False, explicit_start=True)

        with open(expanded_config_file_path, 'w') as fh:
            fh.write(new_config_yaml)
        print(new_config_yaml)
        print("...Done!")
        print("\n")


    def config_get_all(self):
        config_file_base_path = os.path.expanduser("~/.pio/")
        expanded_config_file_base_path = os.path.expandvars(config_file_base_path)
        expanded_config_file_base_path = os.path.expanduser(expanded_config_file_base_path)
        expanded_config_file_base_path = os.path.abspath(expanded_config_file_base_path)
        expanded_config_file_path = os.path.join(expanded_config_file_base_path, 'config')

        # >= Python3.5
        # os.makedirs(expanded_config_file_base_path, exist_ok=True)
        if not os.path.exists(expanded_config_file_path):
            if not os.path.exists(expanded_config_file_base_path):
                os.makedirs(expanded_config_file_base_path)
            pio_api_version = self.pio_api_version() 
            initial_config_dict = {'pio_api_version': pio_api_version}
            initial_config_yaml =  yaml.dump(initial_config_dict, default_flow_style=False, explicit_start=True)
            print("Creating config '%s'..." % expanded_config_file_path)
            with open(expanded_config_file_path, 'w') as fh:
                fh.write(initial_config_yaml)
            print("...Done!")

        # Update the YAML 
        with open(expanded_config_file_path, 'r') as fh:
            existing_config_dict = yaml.load(fh)
            pio_api_version = existing_config_dict['pio_api_version']
            return existing_config_dict
        print("\n")


    def config_view(self):
        pprint(self.config_get_all())
        print("\n")


    def cluster_top(self):
        subprocess.call("kubectl top node", shell=True)
        print("\n")
        print("Note:  Heapster must be deployed for this command ^^ to work.\n")
        print("\n")


    def app_top(self,
                app_name):

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()
        
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_namespaced_pod(namespace=kube_namespace, watch=False, pretty=True)
            for pod in response.items:
                if (app_name in pod.metadata.name):
                    subprocess.call("kubectl top %s" % app_name, shell=True)
        print("\n")
        print("Note:  Heapster must be deployed for this command ^^ to work.\n")
        print("\n")


    def cluster_init(self,
                     pio_home,
                     pio_runtime_version,
                     kube_cluster_context,
                     kube_namespace='default'):

        pio_api_version = self.config_get_all()['pio_api_version']

        if 'http:' in pio_home or 'https:' in pio_home:
            pio_home = os.path.expandvars(pio_home)
            pio_home = os.path.expanduser(pio_home)
            pio_home = os.path.abspath(pio_home)

        config_dict = {'pio_home': pio_home, 
                       'pio_runtime_version': pio_runtime_version, 
                       'kube_cluster_context': kube_cluster_context, 
                       'kube_namespace': kube_namespace}
        self.config_merge_dict(config_dict)
        print("\n")
        pprint(self.config_get_all())
        print("\n")


    def model_init(self,
                   model_server_url,
                   model_type,
                   model_namespace,
                   model_name,
                   model_input_mime_type='application/json',
                   model_output_mime_type='application/json'):

        pio_api_version = self.config_get_all()['pio_api_version']

        config_dict = {"model_server_url": model_server_url, 
                       "model_type": model_type,
                       "model_namespace": model_namespace,
                       "model_name": model_name,
                       "model_input_mime_type": model_input_mime_type,
                       "model_output_mime_type": model_output_mime_type,
        }

        self.config_merge_dict(config_dict)
        print("\n")
        pprint(self.config_get_all())
        print("\n")


    def model_deploy(self,
                     model_version, 
                     model_path,
                     request_timeout=600):

        pio_api_version = self.config_get_all()['pio_api_version']

        try:
            model_server_url = self.config_get_all()['model_server_url']
            model_type = self.config_get_all()['model_type']
            model_namespace = self.config_get_all()['model_namespace']
            model_name = self.config_get_all()['model_name']
        except:
            print("Model needs to be initialized.")
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

        print("\n")


    def model_predict(self, 
                      model_version, 
                      model_input_file_path,
                      request_timeout=30):

        pio_api_version = self.config_get_all()['pio_api_version']

        try:
            model_server_url = self.config_get_all()['model_server_url']
            model_type = self.config_get_all()['model_type']
            model_namespace = self.config_get_all()['model_namespace']
            model_name = self.config_get_all()['model_name']
            model_input_mime_type = self.config_get_all()['model_input_mime_type']
            model_output_mime_type = self.config_get_all()['model_output_mime_type']
        except:
            print("Model needs to be initialized.")
            return

        print('model_version: %s' % model_version)
        print('model_input_file_path: %s' % model_input_file_path)
        print('request_timeout: %s' % request_timeout)

        full_model_server_url = "%s/%s/model/predict/%s/%s/%s/%s" % (model_server_url, pio_api_version, model_type, model_namespace, model_name, model_version)

        print("Predicting file '%s' with model '%s/%s/%s/%s' at '%s'..." % (model_input_file_path, model_type, model_namespace, model_name, model_version, full_model_server_url))
        print("")
        with open(model_input_file_path, 'rb') as fh:
            model_input_binary = fh.read()

        headers = {'Content-type': model_input_mime_type, 'Accept': model_output_mime_type} 
        response = requests.post(url=full_model_server_url, 
                                 headers=headers, 
                                 data=model_input_binary, 
                                 timeout=request_timeout)
        pprint(response.text)
        print("\n")
        print("...Done!")
        print("\n")


    def cluster_view(self):
        pio_api_version = self.config_get_all()['pio_api_version']

        try:
            kube_cluster_context = self.config_get_all()['kube_cluster_context']
            kube_namespace = self.config_get_all()['kube_namespace']
        except:
            print("Cluster needs to be initialized.")
            return

        print("\n")
        print("Config")
        print("******")
        pprint(self.config_get_all())

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        print("\n")
        print("Apps")
        print("****")
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1_beta1.list_namespaced_deployment(namespace=kube_namespace, 
                                                                      watch=False, 
                                                                      pretty=True)
            for deploy in response.items:
                print("%s (%s of %s replicas available)" % (deploy.metadata.name, deploy.status.ready_replicas, deploy.status.replicas))

        print("\n")
        print("Internal DNS (Public DNS)")
        print("*************************")
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
                print("%s (%s)" % (svc.metadata.name, ingress))

        print("\n")
        print("Pods")
        print("****")
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_namespaced_pod(namespace=kube_namespace, watch=False, pretty=True)
            for pod in response.items:
                print("%s (%s)" % (pod.metadata.name, pod.status.phase))
        print("\n")


    def apps_available(self):
        pio_api_version = self.config_get_all()['pio_api_version']

        try:
            kube_cluster_context = self.config_get_all()['kube_cluster_context']
            kube_namespace = self.config_get_all()['kube_namespace']
        except:
            print("Cluster needs to be initialized.")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1_beta1.list_namespaced_deployment(namespace=kube_namespace,
                                                                  watch=False,
                                                                  pretty=True)
            for deploy in response.items:
                print("%s (%s of %s replicas available)" % (deploy.metadata.name, deploy.status.ready_replicas, deploy.status.replicas))
        print("\n")

    def apps_deployed(self):
        pio_api_version = self.config_get_all()['pio_api_version']

        try:
            kube_cluster_context = self.config_get_all()['kube_cluster_context']
            kube_namespace = self.config_get_all()['kube_namespace']
        except:
            print("Cluster needs to be initialized.")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        print("\n")
        print("Apps")
        print("****")
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1_beta1.list_namespaced_deployment(namespace=kube_namespace,
                                                                      watch=False,
                                                                      pretty=True)
            for deploy in response.items:
                print("%s (%s of %s replicas available)" % (deploy.metadata.name, deploy.status.ready_replicas, deploy.status.replicas))
        print("\n")
   
 
    def app_shell(self,
                  app_name):

        pio_api_version = self.config_get_all()['pio_api_version']

        try:
            kube_cluster_context = self.config_get_all()['kube_cluster_context']
            kube_namespace = self.config_get_all()['kube_namespace']
        except:
            print("Cluster needs to be initialized.")
            return

        pprint(self.config_get_all())

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
        print("\n")


    def app_logs(self,
                 app_name):

        pio_api_version = self.config_get_all()['pio_api_version']

        try:
            kube_cluster_context = self.config_get_all()['kube_cluster_context']
            kube_namespace = self.config_get_all()['kube_namespace']
        except:
            print("Cluster needs to be initialized.")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")

            response = kubeclient_v1.list_namespaced_pod(namespace=kube_namespace, watch=False, pretty=True)
            for pod in response.items:
                if app_name in pod.metadata.name:
                    break
            print("Tailing logs on '%s'" % pod.metadata.name)
            subprocess.call("kubectl logs -f %s" % pod.metadata.name, shell=True)

        print("\n")


    def app_scale(self,
                  app_name,
                  replicas):

        pio_api_version = self.config_get_all()['pio_api_version']

        try:
            kube_namespace = self.config_get_all()['kube_namespace']
        except:
            print("Cluster needs to be initialized.")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")

            response = kubeclient_v1_beta1.list_namespaced_deployment(namespace=kube_namespace, watch=False, pretty=True)
            for deploy in response.items:
                if app_name in deploy.metadata.name:
                    break
            print("Scaling '%s' to %s replicas..." % (deploy.metadata.name, replicas))
            subprocess.call("kubectl scale deploy %s --replicas=%s" % (deploy.metadata.name, replicas), shell=True)

        self.cluster_view()

        print("\n")
        print("Note:  There may be a delay in the status change above ^^.")
        print("\n") 


    def get_config_yamls(self, 
                         app_name):
        return [] 


    def get_secret_yamls(self, 
                         app_name):
        return []


    def get_deploy_yamls(self, 
                         app_name):
        (deploy_yamls, dependencies) = PioCli.kube_deploy_registry[app_name]
        if len(dependencies) > 0:
            for dependency in dependencies:
                deploy_yamls = deploy_yamls + self.get_deploy_yamls(dependency)
        return deploy_yamls 


    def get_svc_yamls(self, 
                      app_name):
        (svc_yamls, dependencies) = PioCli.kube_svc_registry[app_name]
        if len(dependencies) > 0:
            for dependency in dependencies:
                svc_yamls = svc_yamls + self.get_svc_yamls(dependency)
        return svc_yamls 


    def app_deploy(self,
                   app_name):

        pio_api_version = self.config_get_all()['pio_api_version']

        try: 
            kube_namespace = self.config_get_all()['kube_namespace']

            pio_home = self.config_get_all()['pio_home']

            if 'http:' in pio_home or 'https:' in pio_home:
                pio_home = os.path.expandvars(pio_home)
                pio_home = os.path.expanduser(pio_home)
                pio_home = os.path.abspath(pio_home)
        except:
            print("Cluster needs to be initialized.")
            return

        print("Deploying app '%s'..." % app_name)

        kubeconfig.load_kube_config()

        config_yaml_filenames = [] 
        secret_yaml_filenames = [] 
        deploy_yaml_filenames = []
        svc_yaml_filenames = [] 
       
        config_yaml_filenames = config_yaml_filenames + self.get_config_yamls(app_name)
        secret_yaml_filenames = secret_yaml_filenames + self.get_secret_yamls(app_name)
        deploy_yaml_filenames = deploy_yaml_filenames + self.get_deploy_yamls(app_name)
        svc_yaml_filenames = svc_yaml_filenames + self.get_svc_yamls(app_name)

        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        #for config_yaml_filename in config_yaml_filenames:
            # TODO
        #    return 

        #for secret_yaml_filename in secret_yaml_filenames:
            # TODO 
        #    return

        print(deploy_yaml_filenames)
        print(svc_yaml_filenames)

        for deploy_yaml_filename in deploy_yaml_filenames:
            try:
                # TODO: handle http: or https:
                if 'http:' in pio_home or 'https:' in pio_home:
                    # TODO: handle http: or https:
                    pass
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
                # TODO: handle http: or https:
                if 'http:' in pio_home or 'https:' in pio_home:
                    # TODO: handle http: or https: 
                    pass
                else:
                    with open(os.path.join(pio_home, svc_yaml_filename)) as fh:
                        svc_yaml = yaml.load(fh)
                        response = kubeclient_v1.create_namespaced_service(body=svc_yaml, 
                                                                           namespace=kube_namespace, 
                                                                           pretty=True)
                        pprint(response)
            except ApiException as e: 
                print("Service not created for '%s':\n%s\n" % (svc_yaml_filename, str(e)))

        self.cluster_view()

        print("\n")


    def app_undeploy(self,
                     app_name):

        pio_api_version = self.config_get_all()['pio_api_version']

        try:
            kube_namespace = self.config_get_all()['kube_namespace']
        except:
            print("Cluster needs to be initialized.")
            return

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1_beta1.list_namespaced_deployment(namespace=kube_namespace, watch=False, pretty=True)
            for deploy in response.items:
                if app_name in deploy.metadata.name:
                    break
            print("Deleting '%s'" % deploy.metadata.name)
            subprocess.call("kubectl delete deploy %s" % deploy.metadata.name, shell=True)

        self.cluster_view()

        print("\n")
        print("Note:  There may be a delay in the status change above ^^.")
        print("\n")

#    def git_init(self,
#                 git_repo_base_path,
#                 git_revision='HEAD'):

#        expanded_git_repo_base_path = os.path.expandvars(git_repo_base_path)
#        expanded_git_repo_base_path = os.path.expanduser(expanded_git_repo_base_path)
#        expanded_git_repo_base_path = os.path.abspath(expanded_git_repo_base_path)

#        pio_api_version = self.config_get_all()['pio_api_version']

#        print("git_repo_base_path: '%s'" % git_repo_base_path)
#        print("expanded_git_repo_base_path: '%s'" % expanded_git_repo_base_path)
#        print("git_revision: '%s'" % git_revision)

#        git_repo = Repo(expanded_git_repo_base_path, search_parent_directories=True)
 
#        config_dict = {'git_repo_base_path': git_repo.working_tree_dir , 'git_revision': git_revision}

#        self.config_merge_dict(config_dict)
#        pprint(self.config_get_all())
#        print("\n")

#    def git_view(self):
#        pio_api_version = self.config_get_all()['pio_api_version']
#        try: 
#            git_repo_base_path = self.config_get_all()['git_repo_base_path']

#            expanded_git_repo_base_path = os.path.expandvars(git_repo_base_path)
#            expanded_git_repo_base_path = os.path.expanduser(expanded_git_repo_base_path)
#            expanded_git_repo_base_path = os.path.abspath(expanded_git_repo_base_path)

#            git_revision = self.config_get_all()['git_revision']
#        except:
#            print("Git needs to be initialized.")
#            return

#        pprint(self.config_get_all())

#        git_repo = Repo(expanded_git_repo_base_path, search_parent_directories=False)
#        ch = git_repo.commit(git_revision)

#        print("Git repo base path: '%s'" % git_repo_base_path)
#        print("Git revision: '%s'" % git_revision)
#        print("Git commit message: '%s'" % ch.message)
#        print("Git commit hash: '%s'" % ch.hexsha)
#        print("\n")


def main():
    fire.Fire(PioCli)


if __name__ == '__main__':
    main()
