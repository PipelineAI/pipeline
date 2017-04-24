# -*- coding: utf-8 -*-

__version__ = "0.18"

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

# TODO: enums
#   model_input_mime_type = ['application/xml', 'application/json']
#   model_output_mime_type = ['application/json', 'text/plain']

# References:
#   https://github.com/kubernetes-incubator/client-python/blob/master/kubernetes/README.md

class PioCli(object):
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
                            'mysql': (['sql.ml/mysql-master-deploy.yaml'], []),
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
                         'spark': (['apachespark.ml/master-svc.yaml', 'apachespark.ml/worker-svc.yaml'], []),
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

    def pio_api_version(self):
        return 'v1'

    def config_get(self,
                   config_key):
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


    def config_view(self):
        return pprint(self.config_get_all())


    def cluster_init(self,
                     pio_home,
                     pio_version,
                     kube_cluster_context,
                     kube_namespace='default'):

        pio_api_version = self.config_get_all()['pio_api_version']

        expanded_pio_home = os.path.expandvars(pio_home)
        expanded_pio_home = os.path.expanduser(expanded_pio_home)
        expanded_pio_home = os.path.abspath(expanded_pio_home)

        config_dict = {'pio_home': expanded_pio_home, 'pio_version': pio_version, 'kube_cluster_context': kube_cluster_context, 'kube_namespace': kube_namespace}
        self.config_merge_dict(config_dict)
        pprint(self.config_get_all())


    def model_init(self,
                   model_server_url,
                   model_namespace,
                   model_name,
                   model_input_mime_type='application/json',
                   model_output_mime_type='application/json'):

        pio_api_version = self.config_get_all()['pio_api_version']

        config_dict = {"model_server_url": model_server_url, 
                       "model_namespace": model_namespace,
                       "model_name": model_name,
                       "model_input_mime_type": model_input_mime_type,
                       "model_output_mime_type": model_output_mime_type,
        }

        self.config_merge_dict(config_dict)
        pprint(self.config_get_all())


    def model_deploy(self,
                     model_version, 
                     model_bundle_path,
                     request_timeout=600):

        pio_api_version = self.config_get_all()['pio_api_version']

        try:
            model_server_url = self.config_get_all()['model_server_url']
            model_namespace = self.config_get_all()['model_namespace']
            model_name = self.config_get_all()['model_name']
        except:
            print("Model needs to be initialized.")
            return

        pprint(self.config_get_all())

        print('model_version: %s' % model_version)
        print('model_bundle_path: %s' % model_bundle_path)
        print('request_timeout: %s' % request_timeout)
 
        compressed_model_bundle_filename = 'bundle-%s-%s-%s.tar.gz' % (model_namespace, model_name, model_version)

        print("Compressing '%s' into '%s'..." % (model_bundle_path, compressed_model_bundle_filename))  
        print("")
        with tarfile.open(compressed_model_bundle_filename, 'w:gz') as tar:
            tar.add(model_bundle_path, arcname='.')
        print("...Done!")

        with open(compressed_model_bundle_filename, 'rb') as fh:
            files = [('bundle', (compressed_model_bundle_filename, fh))]

            full_model_server_url = "%s:81/%s/%s/%s/%s" % (model_server_url, pio_api_version, model_namespace, model_name, model_version)
            print("Deploying model bundle '%s' to '%s'..." % (compressed_model_bundle_filename, full_model_server_url))
            print("")
            headers = {'Accept': 'application/json'}
            response = requests.post(url=full_model_server_url, headers=headers, files=files, timeout=request_timeout)
            pprint(response.text)
            print("...Done!")

            print("Removing model bundle '%s'..." % compressed_model_bundle_filename)
            os.remove(compressed_model_bundle_filename)
            print("...Done!")


    def model_predict(self, 
                      model_version, 
                      model_input_file_path,
                      request_timeout=30):

        pio_api_version = self.config_get_all()['pio_api_version']

        try:
            model_server_url = self.config_get_all()['model_server_url']
            model_namespace = self.config_get_all()['model_namespace']
            model_name = self.config_get_all()['model_name']
            model_input_mime_type = self.config_get_all()['model_input_mime_type']
            model_output_mime_type = self.config_get_all()['model_output_mime_type']
        except:
            print("Model needs to be initialized.")
            return

        pprint(self.config_get_all())

        print('model_version: %s' % model_version)
        print('model_input_file_path: %s' % model_input_file_path)
        print('request_timeout: %s' % request_timeout)

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
        pprint(response.text)
        print("...Done!")

    def cluster_view(self):
        self.cluster_describe()
 
    def cluster_describe(self):
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

        print("Services:")
        response = kubeclient_v1.list_namespaced_service(namespace=kube_namespace, watch=False, pretty=True)
        for svc in response.items:
             print("%s\t\t%s" % (svc.metadata.name, svc.status.load_balancer.ingress))

        print("")
        print("Deployments:")
        response = kubeclient_v1_beta1.list_namespaced_deployment(namespace=kube_namespace, watch=False, pretty=True)
        for deploy in response.items:
             print("%s" % (deploy.metadata.name))

        print("")
        print("Pods:")
        response = kubeclient_v1.list_namespaced_pod(namespace=kube_namespace, watch=False, pretty=True)
        for pod in response.items:
             print("%s\t\t%s" % (pod.metadata.name, pod.status.pod_ip))

    
    def get_config_yamls(self, component):
        return [] 


    def get_secret_yamls(self, component):
        return []


    def get_deploy_yamls(self, component):
        (deploy_yamls, dependencies) = PioCli.kube_deploy_registry[component]
        if len(dependencies) > 0:
            for dependency in dependencies:
                return deploy_yamls + self.get_deploy_yamls(dependency)
        else:
            return deploy_yamls 


    def get_svc_yamls(self, component):
        (svc_yamls, dependencies) = PioCli.kube_svc_registry[component]
        if len(dependencies) > 0:
            for dependency in dependencies:
                return svc_yamls + self.get_svc_yamls(dependency)
        else:
            return svc_yamls 


    def cluster_deploy(self,
                       components):

        pio_api_version = self.config_get_all()['pio_api_version']

        try: 
            kube_namespace = self.config_get_all()['kube_namespace']

            pio_home = self.config_get_all()['pio_home']

            expanded_pio_home = os.path.expandvars(pio_home)
            expanded_pio_home = os.path.expanduser(expanded_pio_home)
            expanded_pio_home = os.path.abspath(expanded_pio_home)
        except:
            print("Cluster needs to be initialized.")
            return

        pprint(self.config_get_all())
        components_list = components.split(',')
        print("components: '%s'" % components_list)

        kubeconfig.load_kube_config()

        config_yaml_filenames = [] 
        secret_yaml_filenames = [] 
        deploy_yaml_filenames = []
        svc_yaml_filenames = [] 
       
        for component in components_list:
            config_yaml_filenames = config_yaml_filenames + self.get_config_yamls(component)
            secret_yaml_filenames = secret_yaml_filenames + self.get_secret_yamls(component)
            deploy_yaml_filenames = deploy_yaml_filenames + self.get_deploy_yamls(component)
            svc_yaml_filenames = svc_yaml_filenames + self.get_svc_yamls(component)

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
                with open(os.path.join(expanded_pio_home, deploy_yaml_filename)) as fh:
                    deploy_yaml = yaml.load(fh)
                    response = kubeclient_v1_beta1.create_namespaced_deployment(body=deploy_yaml, namespace=kube_namespace, pretty=True)
                    pprint(response) 
            except ApiException as e: 
                print("Deployment not created for '%s':\n%s\n" % (deploy_yaml_filename, str(e)))

        for svc_yaml_filename in svc_yaml_filenames:
            try:
                with open(os.path.join(expanded_pio_home, svc_yaml_filename)) as fh:
                    svc_yaml = yaml.load(fh)
                    response = kubeclient_v1.create_namespaced_service(body=svc_yaml, namespace=kube_namespace, pretty=True)
                    pprint(response)
            except ApiException as e: 
                print("Service not created for '%s':\n%s\n" % (svc_yaml_filename, str(e)))


    def git_init(self,
                 git_repo_base_path,
                 git_revision='HEAD'):

        expanded_git_repo_base_path = os.path.expandvars(git_repo_base_path)
        expanded_git_repo_base_path = os.path.expanduser(expanded_git_repo_base_path)
        expanded_git_repo_base_path = os.path.abspath(expanded_git_repo_base_path)

        pio_api_version = self.config_get_all()['pio_api_version']

        print("git_repo_base_path: '%s'" % git_repo_base_path)
        print("expanded_git_repo_base_path: '%s'" % expanded_git_repo_base_path)
        print("git_revision: '%s'" % git_revision)

        git_repo = Repo(expanded_git_repo_base_path, search_parent_directories=True)
 
        config_dict = {'git_repo_base_path': git_repo.working_tree_dir , 'git_revision': git_revision}

        self.config_merge_dict(config_dict)
        pprint(self.config_get_all())


    def git_view(self):
        self.git_describe()


    def git_describe(self):
        pio_api_version = self.config_get_all()['pio_api_version']
        try: 
            git_repo_base_path = self.config_get_all()['git_repo_base_path']

            expanded_git_repo_base_path = os.path.expandvars(git_repo_base_path)
            expanded_git_repo_base_path = os.path.expanduser(expanded_git_repo_base_path)
            expanded_git_repo_base_path = os.path.abspath(expanded_git_repo_base_path)

            git_revision = self.config_get_all()['git_revision']
        except:
            print("Git needs to be initialized.")
            return

        pprint(self.config_get_all())

        git_repo = Repo(expanded_git_repo_base_path, search_parent_directories=False)
        ch = git_repo.commit(git_revision)

        print("Git repo base path: '%s'" % git_repo_base_path)
        print("Git revision: '%s'" % git_revision)
        print("Git commit message: '%s'" % ch.message)
        print("Git commit hash: '%s'" % ch.hexsha)


def main():
    fire.Fire(PioCli)


if __name__ == '__main__':
    main()
