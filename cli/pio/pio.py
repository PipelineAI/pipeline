# -*- coding: utf-8 -*-

__version__ = "0.9"

import requests
import fire
import tarfile
import os
import kubernetes.client as kubeclient
import kubernetes.config as kubeconfig
import pick
import yaml
import json
import dill as pickle
from git import Repo

# TODO: enums
#   model_input_mime_type = ['application/xml', 'application/json']
#   model_output_mime_type = ['application/json', 'text/plain']

# References:
#   https://github.com/kubernetes-incubator/client-python/blob/master/kubernetes/README.md

class PioCli(object):

    def pio_api_version(self):
        return 'v1'


    def config_set(self, 
                   config_dict):
        config_file_base_path = os.path.expanduser("~/.pio/")
        os.makedirs(config_file_base_path, exist_ok=True)
        config_file_path = os.path.join(config_file_base_path, 'config')
        config_file_path_exists = os.path.exists(config_file_path)
        pio_api_version = self.pio_api_version()

        print("config_dict: '%s'" % str(config_dict))
        print("config_file_base_path: '%s'" % config_file_base_path)
        print("config_file_path: '%s'" % config_file_path)
        print("config_file_path_exists: '%s'" % config_file_path_exists)
        print("pio_api_version: %s" % pio_api_version)

        print("Merging new config_dict '%s' with old config file '%s'..." % (config_dict, config_file_path))

        existing_config_dict = self.config_get()

        merged_config_dict = {**existing_config_dict, **config_dict}
        config_yaml = yaml.dump(merged_config_dict, default_flow_style=False, explicit_start=True)

        with open(config_file_path, 'w') as fh:
            fh.write(config_yaml)
        print(config_yaml)
        print("...Done!")


    def config_get(self):
        config_file_base_path = os.path.expanduser("~/.pio/")
        os.makedirs(config_file_base_path, exist_ok=True)
        config_file_path = os.path.join(config_file_base_path, 'config')
        config_file_path_exists = os.path.exists(config_file_path)
        pio_api_version = self.pio_api_version()

        print("config_file_base_path: '%s'" % config_file_base_path)
        print("config_file_path: '%s'" % config_file_path)
        print("config_file_path_exists: '%s'" % config_file_path_exists)
        print("pio_api_version: %s" % pio_api_version)

        print("Retrieving config file '%s'..." % config_file_path)

        # TODO:  Update the YAML instead of overwritting it
        # TODO:  Check if config_file_path exists, first
        if (config_file_path_exists):
            with open(config_file_path, 'r') as fh:
                existing_config_dict = yaml.load(fh)
        else:
            existing_config_dict = {}

        print(existing_config_dict)
        print("...Done!")
        return existing_config_dict

    def config_view(self):
        return self.config_get()

    def cluster_init(self,
                     kube_yaml_base_path,
                     kube_cluster_context,
                     kube_namespace='default'):

        pio_api_version = self.pio_api_version()
        expanded_kube_yaml_base_path = os.path.expanduser(kube_yaml_base_path)

        print('kube_cluster_context: %s' % kube_cluster_context)
        print('kube_namespace: %s' % kube_namespace)
        print("pio_api_version: %s" % pio_api_version)
        print("kube_yaml_base_path: %s" % kube_yaml_base_path)
        print("expanded_kube_yaml_base_path: %s" % expanded_kube_yaml_base_path)

        config_dict = {'kube_yaml_base_path': expanded_kube_yaml_base_path, 'kube_cluster_context': kube_cluster_context, 'kube_namespace': kube_namespace}
        self.config_set(config_dict)


    def model_init(self,
                   model_server_url,
                   model_namespace,
                   model_name,
                   model_input_mime_type='application/json',
                   model_output_mime_type='application/json'):

        pio_api_version = self.pio_api_version()

        print("model_server_url: '%s'" % model_server_url)
        print("model_namespace: '%s'" % model_namespace)
        print("model_name: '%s'" % model_name)
        print("model_input_mime_type: '%s'" % model_input_mime_type)
        print("model_output_mime_type: '%s'" % model_output_mime_type)
        print("pio_api_version: %s" % pio_api_version)

        config_dict = {"model_server_url": model_server_url, 
                       "model_namespace": model_namespace,
                       "model_name": model_name,
                       "model_input_mime_type": model_input_mime_type,
                       "model_output_mime_type": model_output_mime_type,
        }
        self.config_set(config_dict)


    def model_deploy(self,
                     model_version, 
                     model_bundle_path,
                     request_timeout=600):

        model_server_url = self.config_get()['model_server_url']
        model_namespace = self.config_get()['model_namespace']
        model_name = self.config_get()['model_name']
        pio_api_version = self.pio_api_version()

        print('model_server_url: %s' % model_server_url)
        print('model_namespace: %s' % model_namespace)
        print('model_name: %s' % model_name)
        print('model_version: %s' % model_version)
        print('model_bundle_path: %s' % model_bundle_path)
        print('request_timeout: %s' % request_timeout)
        print("pio_api_version: %s" % pio_api_version)
 
        compressed_model_bundle_filename = model_bundle_path.rstrip(os.sep)
        compressed_model_bundle_filename = '%s.tar.gz' % compressed_model_bundle_filename

        print("Compressing '%s' into '%s'..." % (model_bundle_path, compressed_model_bundle_filename))  
        print("")
        with tarfile.open(compressed_model_bundle_filename, 'w:gz') as tar:
            tar.add(model_bundle_path, arcname='.')
        print("...Done!")

        with open(compressed_model_bundle_filename, 'rb') as fh:
            files = [('bundle', (compressed_model_bundle_filename, fh))]

        full_model_server_url = "%s:81/%s/%s/%s/%s" % (model_server_url, pio_api_version, model_namespace, model_name, model_version)
        print("Deploying model bundle '%s' to '%s...'" % (compressed_model_bundle_filename, full_model_server_url))
        print("")
        headers = {'Accept': 'application/json'}
        response = requests.post(url=full_model_server_url, headers=headers, files=files, timeout=request_timeout)
        print(response.text)
        print("...Done!")

    def model_predict(self, 
                      model_version, 
                      model_input_file_path,
                      request_timeout=30):

        model_server_url = self.config_get()['model_server_url']
        model_namespace = self.config_get()['model_namespace']
        model_name = self.config_get()['model_name']
        model_input_mime_type = self.config_get()['model_input_mime_type']
        model_output_mime_type = self.config_get()['model_output_mime_type']

        pio_api_version = self.pio_api_version()

        print('model_server_url: %s' % model_server_url)
        print('model_namespace: %s' % model_namespace)
        print('model_name: %s' % model_name)
        print('model_version: %s' % model_version)
        print('model_input_file_path: %s' % model_input_file_path)
        print('model_input_mime_type: %s' % model_input_mime_type)
        print('model_output_mime_type: %s' % model_output_mime_type)
        print('request_timeout: %s' % request_timeout)
        print("pio_api_version: %s" % pio_api_version)

        full_model_server_url = "%s/%s/%s/%s/%s" % (model_server_url, pio_api_version, model_namespace, model_name, model_version)

        print("Predicting file '%s' with model '%s' at '%s'..." % (model_input_file_path, model_name, full_model_server_url))
        print("")
        with open(model_input_file_path, 'rb') as fh:
            model_input_binary = fh.read()

        headers = {'Content-type': model_input_mime_type, 'Accept': model_output_mime_type} 
        response = requests.post(url=full_model_server_url, 
                                 headers=headers, 
                                 data=model_input_binary, 
                                 timeout=request_timeout)
        print(response.text)
        print("...Done!")


    def cluster_describe(self):
        kube_cluster_context = self.config_get()['kube_cluster_context']
        kube_namespace = self.config_get()['kube_namespace']
        pio_api_version = self.pio_api_version()

        print("kube_cluster_context: '%s'" % kube_cluster_context)
        print("kube_namespace: '%s'" % kube_namespace)
        print("pio_api_version: %s" % pio_api_version)

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        print("Services:")
        response = kubeclient_v1.list_namespaced_service(namespace=kube_namespace, watch=False)
        for svc in response.items:
             print("%s\t\t%s" % (svc.metadata.name, svc.status.load_balancer.ingress))

        print("")
        print("Deployments:")
        response = kubeclient_v1_beta1.list_namespaced_deployment(namespace=kube_namespace, watch=False)
        for deploy in response.items:
             print("%s" % (deploy.metadata.name))

        print("")
        print("Pods:")
        response = kubeclient_v1.list_namespaced_pod(namespace=kube_namespace, watch=False)
        for pod in response.items:
             print("%s\t\t%s" % (pod.metadata.name, pod.status.pod_ip))
    

    def get_config_yamls(self, component):
        return 


    def get_secret_yamls(self, component):
        return


    def get_deploy_yamls(self, component):
        (deploy_list, dependencies) = kube_deploy_registry[component]
        for dependency in dependencies:
           return deploy_yamls + self.get_deploy_yamls(dependency)


    def get_svc_yamls(self, component):
        (svc_list, dependencies) = kube_svc_registry[component]
        for dependency in dependencies:
           return svc_yamls + self.get_svc_yamls(dependency)


    def cluster_create(self,
                       components=['jupyter','prediction-python']):

        kube_namespace = self.config_get()['kube_namespace']
        kube_yaml_base_path = self.config_get()['kube_yaml_base_path']
        expanded_kube_yaml_base_path = os.path.expanduser(kube_yaml_base_path)
        pio_api_version = self.pio_api_version()

        print("kube_namespace: '%s'" % kube_namespace)
        print("kube_yaml_base_path: '%s'" % kube_yaml_base_path)
        print("expanded_kube_yaml_base_path: '%s'" % expanded_kube_yaml_base_path)
        print("pio_api_version: %s" % pio_api_version)

        kubeconfig.load_kube_config()
        for component in components:
            config_yaml_filenames = self.get_config_yamls(component)
            secret_yaml_filenames = self.get_secret_yamls(component)
            deploy_yaml_filenames = self.get_deploy_yamls(component)
            svc_yaml_filenames = self.get_svc_yamls(component)

        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        for config_yaml_filename in config_yaml_filenames:
            # TODO
            return 

        for secret_yaml_filename in secret_yaml_filenames:
            # TODO 
            return

        for deploy_yaml_filename in deploy_yaml_filenames:
            with open(os.path.join(expanded_kube_yaml_base_path, deploy_yaml_filename)) as fh:
                deploy_yaml = yaml.load(fh)
                response = kubeclient_v1_beta1.create_namespaced_deployment(body=deploy_yaml, namespace=namespace)
                print("Deployment created from '%s'. Status='%s'." % (deploy_yaml_filename, str(resp.status)))

        for svc_yaml_filename in svc_yaml_filenames:
            with open(os.path.join(expanded_kube_yaml_base_path, svc_yaml_filename)) as fh:
                svc_yaml = yaml.load(fh)
                response = kubeclient_v1_beta1.create_namespaced_deployment(body=svc_yaml, namespace=namespace)
                print("Service created from '%s'. Status='%s'." % (svc_yaml_filename, str(resp.status)))


    kube_deploy_registry = {'jupyter': (['jupyterhub.ml/jupyterhub-deploy.yaml'], []),
                            'spark': (['apachespark.ml/master-deploy.yaml',
                                       'apachespark.ml/worker-deploy.yaml'], ['metastore']),
                            'metastore': (['metastore.ml/metastore-deploy.yaml'], ['mysql']),
                            'hdfs': (['hdfs.ml/namenode-deploy.yaml'], []),
                            'redis': (['keyvalue.ml/redis-master-deploy.yaml'], []),
                            'presto': (['presto.ml/presto-master-deploy.yaml',
                                        'presto.ml/presto-worker-deploy.yaml'], []),
                            'presto-ui': (['presto.ml/presto-ui-deploy.yaml'], ['presto']),
                            'airflow': (['scheduler.ml/airflow-deploy.yaml'], []),
                            'mysql': (['mysql.ml/mysql-master-deploy.yaml'], []),
                            'www': (['web.ml/home-deploy.yaml'], []),
                            'zeppelin': (['zeppelin.ml/zeppelin-deploy.yaml'], []),
                            'zookeeper': (['zookeeper.ml/zookeeper-deploy.yaml'], []),
                            'kafka': (['stream.ml/kafka-0.10-rc.yaml'], ['zookeeper']),
                            'cassandra': (['cassandra.ml/cassandra-rc.yaml'], []),
                            'prediction-java': (['prediction.ml/java-deploy.yaml'], []),
                            'prediction-redis': (['prediction.ml/keyvalue-deploy.yaml'], ['redis']),
                            'prediction-pmml': (['prediction.ml/pmml-deploy.yaml'], []),
                            'prediction-python': (['prediction.ml/python-deploy.yaml'], []),
                            'prediction-tensorflow': (['prediction.ml/tensorflow-deploy.yaml'], []),
                           }

    kube_svc_registry = {'jupyter': (['jupyterhub.ml/jupyterhub-svc.yaml'], []),
                         'spark': (['apachespark.ml/master-svc.yaml'], []),
                         'metastore': (['metastore.ml/metastore-svc.yaml'], []),
                         'hdfs': (['hdfs.ml/namenode-svc.yaml'], []),
                         'redis': (['keyvalue.ml/redis-master-svc.yaml'], []),
                         'presto-ui': (['presto.ml/presto-ui-svc.yaml'], []),
                         'airflow': (['scheduler.ml/airflow-svc.yaml'], []),
                         'www': (['web.ml/home-svc.yaml'], []),
                         'zeppelin': (['zeppelin.ml/zeppelin-svc.yaml'], []),
                         'prediction-java': (['prediction.ml/java-svc.yaml'], []),
                         'prediction-redis': (['prediction.ml/keyvalue-svc.yaml'], []),
                         'prediction-pmml': (['prediction.ml/pmml-svc.yaml'], []),
                         'prediction-python': (['prediction.ml/python-svc.yaml'], []),
                         'prediction-tensorflow': (['prediction.ml/tensorflow-svc.yaml'], []),
                        }


    def git_init(self,
                 git_repo_base_path=".",
                 git_revision='HEAD'):

        expanded_git_repo_base_path = os.path.expanduser(git_repo_base_path)
        pio_api_version = self.pio_api_version()

        print("git_repo_base_path: '%s'" % git_repo_base_path)
        print("expanded_git_repo_base_path: '%s'" % expanded_git_repo_base_path)
        print("git_revision: '%s'" % git_revision)
        print("pio_api_version: %s" % pio_api_version)
 
        config_dict = {'git_repo_base_path': expanded_git_repo_base_path , 'git_revision': git_revision}
        self.config_set(config_dict)


    def git_commit_hash(self):

        git_repo_base_path = self.config_get()['git_repo_base_path']
        expanded_git_repo_base_path = os.path.expanduser(git_repo_base_path)
        git_revision = self.config_get()['git_revision']
        pio_api_version = self.pio_api_version()

        print("git_repo_base_path: '%s'" % git_repo_base_path)
        print("expanded_git_repo_base_path: '%s'" % expanded_git_repo_base_path)
        print("git_revision: '%s'" % git_revision)
        print("pio_api_version: %s" % pio_api_version)

        git_repo = Repo(expanded_git_repo_base_path, search_parent_directories=True)
        hc = git_repo.commit(git_revision)
        print("(%s, %s): %s" % (git_revision, hc.hexsha, hc.message))
        return hc.hexsha 


def main():
    fire.Fire(PioCli)


if __name__ == '__main__':
    main()
