# -*- coding: utf-8 -*-

__version__ = "0.8"

import requests
import fire
import tarfile
import os
import kubernetes.client as kubeclient
import kubernetes.config as kubeconfig
import pick
import yaml
import json

# TODO: enums
#   input_mime_type = ['application/xml', 'application/json']
#   output_mime_type = ['application/json', 'text/plain']
#   model_bundle_compression_type =['gz']

# References:
#   https://github.com/kubernetes-incubator/client-python/blob/master/kubernetes/README.md

class PioCli(object):
    """PipelineIO CLI"""
    def cluster_init(self,
             context,
             namespace='default'):
   
        print('context: %s' % context)
        print('namespace: %s' % namespace)

        cluster_dict = {'context': context, 'namespace': namespace}
        cluster_yaml = yaml.dump(cluster_dict, default_flow_style=False, explicit_start=True)

        print(cluster_yaml)
 
    def model_init(self,
                   model_server_url,
                   model_namespace,
                   model_name,
                   model_version,
                   model_bundle_path,
                   model_bundle_compression_type='gz'):

        print('context: %s' % context)
        print('namespace: %s' % namespace)

    def model_deploy(self, 
                     model_server_url, 
                     model_namespace, 
                     model_name, 
                     model_version,
                     model_bundle_path,
                     model_bundle_compression_type='gz',
                     request_timeout=600):

        print('model_server_url: %s' % model_server_url)
        print('model_namespace: %s' % model_namespace)
        print('model_name: %s' % model_name)
        print('model_version: %s' % model_version)
        print('model_bundle_path: %s' % model_bundle_path)
        print('model_bundle_compression_type: %s' % model_bundle_compression_type)
        print('request_timeout: %s' % request_timeout)
 
        compressed_model_bundle_filename = model_bundle_path.rstrip(os.sep)
        compressed_model_bundle_filename = '%s.tar.%s' % (compressed_model_bundle_filename, 
                                                          model_bundle_compression_type)

        print("Compressing '%s' into '%s'" % (model_bundle_path, compressed_model_bundle_filename))  
        print("")

        with tarfile.open(compressed_model_bundle_filename, 'w:%s' % model_bundle_compression_type) as tar:
            tar.add(model_bundle_path, arcname='.')

        with open(compressed_model_bundle_filename, 'rb') as fh:
            files = [('bundle', (compressed_model_bundle_filename, fh))]

            model_server_url = "%s/%s/%s/%s" % (model_server_url, model_namespace, model_name, model_version)
            print("Deploying model bundle '%s' to '%s'" % (compressed_model_bundle_filename, model_server_url))
            print("")

            headers = {'Accept': 'application/json'}
            response = requests.post(url=model_server_url, headers=headers, files=files, timeout=request_timeout)
            print(response.text)


    def model_predict(self, 
                      model_server_url, 
                      model_namespace, 
                      model_name, 
                      model_version, 
                      input_file_path,
                      input_mime_type='application/json', 
                      output_mime_type='application/json',
                      request_timeout=30):

        print('model_server_url: %s' % model_server_url)
        print('model_namespace: %s' % model_namespace)
        print('model_name: %s' % model_name)
        print('model_version: %s' % model_version)
        print('input_file_path: %s' % input_file_path)
        print('input_mime_type: %s' % input_mime_type)
        print('output_mime_type: %s' % output_mime_type)
        print('request_timeout: %s' % request_timeout)

        full_model_server_url = "%s/%s/%s/%s" % (model_server_url, model_namespace, model_name, model_version)
        print('full_model_server_url: %s' % full_model_server_url)
        print('')

        with open(input_file_path, 'rb') as fh:
            input_binary = fh.read()

        headers = {'Content-type': input_mime_type, 'Accept': output_mime_type} 
        response = requests.post(url=full_model_server_url, 
                                 headers=headers, 
                                 data=input_binary, 
                                 timeout=request_timeout)
        print(response.text)


    def cluster_describe(self,
                         namespace='default'):

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        print("Services:")
        response = kubeclient_v1.list_namespaced_service(namespace=namespace, watch=False)
        for svc in response.items:
             print("%s\t\t%s" % (svc.metadata.name, svc.status.load_balancer.ingress))
               # getattr(svc.status.load_balancer.ingress, 'ip'), getattr(svc.status.load_balancer.ingress, 'hostname')))

        print("")
        print("Deployments:")
        response = kubeclient_v1_beta1.list_namespaced_deployment(namespace=namespace, watch=False)
        for deploy in response.items:
             print("%s" % (deploy.metadata.name))

        print("")
        print("Pods:")
        response = kubeclient_v1.list_namespaced_pod(namespace=namespace, watch=False)
        for pod in response.items:
             print("%s\t\t%s" % (pod.metadata.name, pod.status.pod_ip))
    
    def get_config_yamls(compoment):
        return 

    def get_secret_yamls(component):
        return

    def get_deploy_yamls(component):
        (deploy_list, dependencies) = kube_deploy_registry[component]
        for dependency in dependencies:
           return deploy_yamls + get_deploy_yamls(dependency)

    def get_svc_yamls(component):
        (svc_list, dependencies) = kube_svc_registry[component]
        for dependency in dependencies:
           return svc_yamls + get_svc_yamls(dependency)

    def cluster_create(self,
                       namespace='default',
                       components=['jupyter','prediction-python']):

        kubeconfig.load_kube_config()
        for component in components:
            config_yaml_filenames = get_config_yamls(component)
            secret_yaml_filenames = get_secret_yamls(component)
            deploy_yaml_filenames = get_deploy_yamls(component)
            svc_yaml_filenames = get_svc_yamls(component)

        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        for config_yaml_filename in config_yaml_filenames:
            # TODO
            _

        for secret_yaml_filename in secret_yaml_filenames:
            # TODO 
            _

        for deploy_yaml_filename in deploy_yaml_filenames:
            with open(os.path.join(base_path, deploy_yaml_filename)) as fh:
                deploy_yaml = yaml.load(fh)
                response = kubeclient_v1_beta1.create_namespaced_deployment(body=deploy_yaml, namespace=namespace)
                print("Deployment created from '%s'. Status='%s'." % (deploy_yaml_filename, str(resp.status)))

        for svc_yaml_filename in svc_yaml_filenames:
            with open(os.path.join(base_path, svc_yaml_filename)) as fh:
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

def main():
    fire.Fire(PioCli)


if __name__ == '__main__':
    main()
