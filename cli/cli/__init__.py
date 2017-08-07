#-*- coding: utf-8 -*-

__version__ = "0.35"

# References:
#   https://github.com/kubernetes-incubator/client-python/blob/master/kubernetes/README.md
#   https://github.com/kubernetes/kops/blob/master/docs/aws.md

import warnings
import requests
import fire
import tarfile
import os
import sys
import kubernetes.client as kubeclient
from kubernetes.client.rest import ApiException
import kubernetes.config as kubeconfig
import yaml
import json
from pprint import pprint
import subprocess
from datetime import timedelta
import importlib.util
import jinja2


class PipelineCli(object):

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
                            'kafka': (['stream.ml/kafka-0.11-deploy.yaml'], ['zookeeper']),
                            'cassandra': (['cassandra.ml/cassandra-deploy.yaml'], []),
                            'jenkins': (['jenkins/jenkins-deploy.yaml'], []),
                            'prediction-java': (['prediction.ml/java-deploy.yaml'], []),
                            'prediction-python3': (['prediction.ml/python3-deploy.yaml'], []),
                            'prediction-scikit': (['prediction.ml/scikit-deploy.yaml'], []),
                            'prediction-pmml': (['prediction.ml/pmml-deploy.yaml'], []),
                            'prediction-spark': (['prediction.ml/spark-deploy.yaml'], []),
                            'prediction-tensorflow': (['prediction.ml/tensorflow-deploy.yaml'], []),
                            'prediction-tensorflow-gpu': (['prediction.ml/tensorflow-gpu-deploy.yaml'], []),
                            'turbine': (['dashboard.ml/turbine-deploy.yaml'], []),
                            'hystrix': (['dashboard.ml/hystrix-deploy.yaml'], []),
#                            'weave-scope-app': (['dashboard.ml/weavescope/scope-1.3.0.yaml'], []),
#                            'kubernetes-dashboard': (['dashboard.ml/kubernetes-dashboard/v1.6.3.yaml'], []),
#                            'heapster': (['metrics.ml/monitoring-standalone/v1.7.0.yaml'], []),
#                            'route53-mapper': (['dashboard.ml/route53-mapper/v1.3.0.yml'], []), 
#                            'kubernetes-logging': (['dashboard.ml/logging-elasticsearch/v1.5.0.yaml'], []),
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
                         'kafka': (['stream.ml/kafka-0.11-svc.yaml'], ['zookeeper']),
                         'cassandra': (['cassandra.ml/cassandra-svc.yaml'], []),
                         'jenkins': (['jenkins/jenkins-svc.yaml'], []),
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

    _kube_deploy_template_registry = {'predict': (['predict-deploy.yaml.template'], [])}
    _kube_svc_template_registry = {'predict': (['predict-svc.yaml.template'], [])}
    _kube_autoscale_template_registry = {'predict': (['predict-autoscale.yaml.template'], [])}

    _pipeline_api_version = 'v1' 


    def config(self):
        print("api_version: '%s'" % PipelineCli._pipeline_api_version)


    def version(self):
        print(__version__)
        self.config()


    def model_env(self,
                  environment='pipeline'):
        print("Make sure you are running in the '%s` conda env for your local development and testing." % environment)
        print("(Your model should have a `pipeline_conda_environment.yml` file that you can use to create the '%s' conda environment.)")
        print("")
        print("Exporting '%s' conda environment..." % environment)
        print("")

        cmd = 'conda env export -n %s' % environment
        process = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE)
        (output, error) = process.communicate()

        print("")
        print("...export complete!")
        return output.rstrip().decode('utf-8')


    def service_proxy(self,
                      service_name,
                      local_port=None,
                      service_port=None):

        pod = self._get_pod_by_service_name(service_name)
        if not pod:
            print("")
            print("App '%s' is not running." % service_name)
            print("")
            return
        if not service_port:
            svc = self._get_svc_by_service_name(service_name)
            if not svc:
                print("")
                print("App '%s' proxy port cannot be found." % service_name)
                print("")
                return
            service_port = svc.spec.ports[0].target_port

        if not local_port:
            print("")
            print("Proxying local port '<randomly-chosen>' to app '%s' port '%s' using pod '%s'." % (service_port, service_name, pod.metadata.name))
            print("")
            print("Use 'http://127.0.0.1:<randomly-chosen>' to access app '%s' on port '%s'." % (service_name, service_port))
            print("")
            print("If you break out of this terminal, your proxy session will end.")
            print("")
            subprocess.call('kubectl port-forward %s :%s' % (pod.metadata.name, service_port), shell=True)
            print("")
        else:
            print("")
            print("Proxying local port '%s' to app '%s' port '%s' using pod '%s'." % (local_port, service_port, service_name, pod.metadata.name))
            print("")
            print("Use 'http://127.0.0.1:%s' to access app '%s' on port '%s'." % (local_port, service_name, service_port))
            print("")
            print("If you break out of this terminal, your proxy session will end.")
            print("")
            subprocess.call('kubectl port-forward %s %s:%s' % (pod.metadata.name, local_port, service_port), shell=True)
            print("")


    def service_top(self,
                    service_name):

        self._get_service_resources(service_name)
        self.cluster_resources()


    def cluster_resources(self):

        self._get_cluster_resources()


    def _get_cluster_resources(self):
        subprocess.call("kubectl top node", shell=True)
        print("")


    def _get_service_resources(self,
                              service_name):

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()
        
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_pod_for_all_namespaces(watch=False, 
                                                                 pretty=True)
            pods = response.items
            for pod in pods: 
                if (service_name in pod.metadata.name):
                    subprocess.call('kubectl top pod %s' % pod.metadata.name, shell=True)
        print("")


#    def model_init(self,
#                   model_type,
#                   model_name,
#                   model_tag,
#                   model_path='.',
#                   model_chip='cpu',
#                   template_path='./templates/'):

#        context = {'PIPELINE_MODEL_TYPE': model_type,
#                   'PIPELINE_MODEL_NAME': model_name,
#                   'PIPELINE_MODEL_CHIP': model_chip,
#                   'PIPELINE_MODEL_TAG': model_tag}

#        model_build_Dockerfile_template_path = os.path.join(template_path, 'predict-Dockerfile-tensorflow.template')

#        path, filename = os.path.split(model_build_Dockerfile_template_path)
#        rendered = jinja2.Environment(loader=jinja2.FileSystemLoader(path or './')).get_template(filename).render(context)
#        rendered_Dockerfile = 'Dockerfile-%s-%s-%s-%s' % (model_type, model_name, model_chip, model_tag)
#        with open(rendered_Dockerfile, 'wt') as fh:
#            fh.write(rendered)
#        print("'%s' -> '%s'." % (filename, rendered_Dockerfile))


    def model_build(self,
                   model_type,
                   model_name,
                   model_path,
                   model_tag,
                   model_chip='cpu',
                   template_path='./templates/',
                   build_type='docker',
                   build_path='.'):

        if build_type == 'docker':
            if model_chip == 'gpu':
                docker_cmd = 'nvidia-docker'
            else:
                docker_cmd = 'docker'

            cmd = '%s build -t fluxcapacitor/predict-%s-%s-%s:%s --build-arg model_type=%s --build-arg model_name=%s --build-arg model_path=%s -f %s' % (docker_cmd, model_type, model_name, model_chip, model_tag, model_type, model_name, model_path, build_path)

            print(cmd)
            print("")
            process = subprocess.call(cmd, shell=True)
        else:
            self._model_build_tar(model_type,
                                  model_name,
                                  model_path,
                                  model_tag,
                                  build_path)  


    # TODO: Pull ./templates/ into this cli project 
    #       (or otherwise handle the location of templates outside of the cli)
    def model_yaml(self,
                   model_type,
                   model_name,
                   model_tag,
                   model_chip='cpu',
                   template_path='./templates/',
                   memory_limit='2G',
                   cpu_limit='4000m',
                   target_cpu_util_percentage='75',
                   min_replicas='1',
                   max_replicas='2'):

        template_path = os.path.expandvars(template_path)
        template_path = os.path.expanduser(template_path)
        template_path = os.path.abspath(template_path)

        print("")
        print("Using templates in '%s'." % template_path)
        print("(Specify --template-path if the templates live elsewhere.)") 
        print("")
 
        context = {'PIPELINE_MODEL_TYPE': model_type,
                   'PIPELINE_MODEL_NAME': model_name,
                   'PIPELINE_MODEL_CHIP': model_chip,
                   'PIPELINE_MODEL_TAG': model_tag,
                   'PIPELINE_CPU_LIMIT': cpu_limit,
                   'PIPELINE_MEMORY_LIMIT': memory_limit,
                   'PIPELINE_TARGET_CPU_UTIL_PERCENTAGE': target_cpu_util_percentage,
                   'PIPELINE_MIN_REPLICAS': min_replicas,
                   'PIPELINE_MAX_REPLICAS': max_replicas}

        model_predict_deploy_yaml_template_path = os.path.join(template_path, PipelineCli._kube_deploy_template_registry['predict'][0][0])

        path, filename = os.path.split(model_predict_deploy_yaml_template_path)
        rendered = jinja2.Environment(loader=jinja2.FileSystemLoader(path or './')).get_template(filename).render(context)
        rendered_filename = './%s-%s-%s-%s-deploy.yaml' % (model_type, model_name, model_chip, model_tag)
        with open(rendered_filename, 'wt') as fh:
            fh.write(rendered)
        model_predict_svc_yaml_template_path = os.path.join(template_path, PipelineCli._kube_svc_template_registry['predict'][0][0])
        print("'%s' -> '%s'." % (filename, rendered_filename))

        path, filename = os.path.split(model_predict_svc_yaml_template_path)
        rendered = jinja2.Environment(loader=jinja2.FileSystemLoader(path or './')).get_template(filename).render(context)    
        rendered_filename = './%s-%s-%s-%s-svc.yaml' % (model_type, model_name, model_chip, model_tag)
        with open(rendered_filename, 'wt') as fh:
            fh.write(rendered)
        print("'%s' -> '%s'." % (filename, rendered_filename)) 

        model_predict_autoscale_yaml_template_path = os.path.join(template_path, PipelineCli._kube_autoscale_template_registry['predict'][0][0])

        path, filename = os.path.split(model_predict_autoscale_yaml_template_path)
        rendered = jinja2.Environment(loader=jinja2.FileSystemLoader(path or './')).get_template(filename).render(context)                     
        rendered_filename = './%s-%s-%s-%s-autoscale.yaml' % (model_type, model_name, model_chip, model_tag)
        with open(rendered_filename, 'wt') as fh:
            fh.write(rendered) 
        print("'%s' -> '%s'." % (filename, rendered_filename))


    def model_shell(self,
                    model_type,
                    model_name,
                    model_tag,
                    model_chip='cpu'):

        if model_chip == 'gpu':
            docker_cmd = 'nvidia-docker'
        else:
            docker_cmd = 'docker'

        cmd = '%s exec -it predict-%s-%s-%s-%s bash' % (docker_cmd, model_type, model_name, model_chip, model_tag)
        print(cmd)
        print("")
        process = subprocess.call(cmd, shell=True)


    def model_push(self,
                   model_type,
                   model_name,
                   model_tag,
                   model_chip='cpu'):

        if model_chip == 'gpu':
            docker_cmd = 'nvidia-docker'
        else:
            docker_cmd = 'docker'

        cmd = '%s push fluxcapacitor/predict-%s-%s-%s:%s' % (docker_cmd, model_type, model_name, model_chip, model_tag)
        process = subprocess.call(cmd, shell=True)


    def model_pull(self,
                   model_type,
                   model_name,
                   model_tag,
                   model_chip='cpu'):

        if model_chip == 'gpu':
            docker_cmd = 'nvidia-docker'
        else:
            docker_cmd = 'docker'

        cmd = '%s pull fluxcapacitor/predict-%s-%s-%s:%s' % (docker_cmd, model_type, model_name, model_chip, model_tag)
        process = subprocess.call(cmd, shell=True)


    def model_start(self,
                    model_type,
                    model_name,
                    model_tag,
                    model_chip='cpu',
                    memory_limit='2G'):

        if model_chip == 'gpu':
            docker_cmd = 'nvidia-docker'
        else:
            docker_cmd = 'docker'

        cmd = '%s run -itd --name=predict-%s-%s-%s-%s -m %s -p 6969:6969 -p 9876:9876 -p 9000:9000 -p 10254:10254 -p 9040:9040 -p 9090:9090 -p 3000:3000 -p 6333:6333 -e "PIO_MODEL_TYPE=%s" -e "PIO_MODEL_NAME=%s" fluxcapacitor/predict-%s-%s-%s:%s' % (docker_cmd, model_type, model_name, model_chip, model_tag, memory_limit, model_type, model_name, model_type, model_name, model_chip, model_tag)

        process = subprocess.call(cmd, shell=True)


    def model_stop(self,
                   model_type,
                   model_name,
                   model_tag,
                   model_chip='cpu'): 

        if model_chip == 'gpu':
            docker_cmd = 'nvidia-docker'
        else:
            docker_cmd = 'docker'

        cmd = '%s rm -f predict-%s-%s-%s-%s' % (docker_cmd, model_type, model_name, model_chip, model_tag)

        process = subprocess.call(cmd, shell=True)


    def model_logs(self,
                   model_type,
                   model_name,
                   model_tag,
                   model_chip='cpu'):

        if model_chip == 'gpu':
            docker_cmd = 'nvidia-docker'
        else:
            docker_cmd = 'docker'

        cmd = '%s logs -f predict-%s-%s-%s-%s' % (docker_cmd, model_type, model_name, model_chip, model_tag)

        process = subprocess.call(cmd, shell=True)


    def service_upgrade(self,
                        service_name,
                        service_image,
                        service_tag):

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1_beta1.list_deployment_for_all_namespaces(watch=False,
                                                                              pretty=True)
            found = False
            deployments = response.items
            for deployment in deployments:
                if service_name in deployment.metadata.name:
                    found = True
                    break
            if found:
                print("")
                print("Upgrading service '%s' using Docker image '%s:%s'." % (deployment.metadata.name, service_image, service_tag))
                print("")
                cmd = "kubectl set image deploy %s %s=%s:%s" % (deployment.metadata.name, deployment.metadata.name, service_image, service_tag)
                print("Running '%s'." % cmd)
                print("")
                subprocess.call(cmd, shell=True)
                print("")
                cmd = "kubectl rollout status deploy %s" % deployment.metadata.name
                print("Running '%s'." % cmd)
                print("")
                subprocess.call(cmd, shell=True)
                print("")
                cmd = "kubectl rollout history deploy %s" % deployment.metadata.name
                print("Running '%s'." % cmd)
                print("")
                subprocess.call(cmd, shell=True)
                print("")
                print("Check status with 'pipeline services'.")
                print("")
            else:
                print("")
                print("App '%s' is not running." % service_name)
                print("")


    def service_rollback(self,
                         service_name,
                         to_revision=None):

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1_beta1.list_deployment_for_all_namespaces(watch=False,
                                                                              pretty=True)
            found = False
            deployments = response.items
            for deployment in deployments:
                if service_name in deployment.metadata.name:
                    found = True
                    break
            if found:
                print("")
                if to_revision:
                    print("Rolling back app '%s' to revision '%s'." % deployment.metadata.name, revision)
                    cmd = "kubectl rollout undo deploy %s --to-revision=%s" % (deployment.metadata.name, to_revision)
                else:
                    print("Rolling back app '%s'." % deployment.metadata.name)
                    cmd = "kubectl rollout undo deploy %s" % deployment.metadata.name
                print("")
                print("Running '%s'." % cmd)
                print("")
                subprocess.call(cmd, shell=True)
                print("")
                cmd = "kubectl rollout status deploy %s" % deployment.metadata.name
                print("Running '%s'." % cmd)
                print("")
                subprocess.call(cmd, shell=True)
                print("")
                cmd = "kubectl rollout history deploy %s" % deployment.metadata.name
                print("Running '%s'." % cmd)
                print("")
                subprocess.call(cmd, shell=True)
                print("")
                print("Check status with 'pipeline services'.")
                print("")
            else:
                print("")
                print("App '%s' is not running." % service_name)
                print("")


    def _filter_tar(self,
                    tarinfo):
        # TODO:  Load this from .pipelineignore
        ignore_list = []
        for ignore in ignore_list:
            if ignore in tarinfo.name:
                return None

        return tarinfo


    def _model_build_tar(self,
                           model_type,
                           model_name,
                           model_path,
                           model_tag,
                           tar_path,
                           tar_name,
                           filemode='w',
                           compression='gz'):

        model_path = os.path.expandvars(model_path)
        model_path = os.path.expanduser(model_path)
        model_path = os.path.abspath(model_path)

        tar_path = os.path.expandvars(tar_path)
        tar_path = os.path.expanduser(tar_path)
        tar_path = os.path.abspath(tar_path)
       
        tar_absolute_path = os.path.join(tar_path, tar_name) 

        # TODO:  Incorporate model_tag if relevant
        with tarfile.open(tar_absolute_path, '%s:%s' % (filemode, compression)) as tar:
            tar.add(model_path, arcname='.', filter=self._filter_tar)


    def _model_deploy(self,
                      model_type,
                      model_name,
                      model_path,
                      model_tag,
                      model_server_url,
                      timeout=60):

        model_path = os.path.expandvars(model_path)
        model_path = os.path.expanduser(model_path)
        model_path = os.path.abspath(model_path)

        print('model_server_url: %s' % model_server_url)
        print('model_type: %s' % model_type)
        print('model_name: %s' % model_name)
        print('model_path: %s' % model_path)
        print('model_tag: %s' % model_tag)

        if (os.path.isdir(model_path)):
            compressed_model_tar_filename = 'pipeline.tar.gz' 

            print("")
            print("Compressing model tar '%s' into '%s'." % (model_path, compressed_model_tar_filename))  
            self.model_build_tar(path_to_model=model_path,
                                 tar_name=compressed_model_tar_filename,
                                 filemode='w',
                                 compression='gz')
            model_file = compressed_model_tar_filename
            upload_key = 'file'
            upload_value = compressed_model_tar_filename
        else:
            print("")
            print("Model path must be a directory.  All contents of the directory will be uploaded.")
            return

        
        full_model_url = "%s/api/%s/model/deploy/%s/%s" % (model_server_url.rstrip('/'), PipelineCli._pipeline_api_version, model_type, model_name) 

        with open(model_file, 'rb') as fh:
            files = [(upload_key, (upload_value, fh))]
            print("")
            print("Deploying model '%s' to '%s'." % (model_file, full_model_url))
            headers = {'Accept': 'application/json'}
            try:
                response = requests.post(url=full_model_url, 
                                         headers=headers, 
                                         files=files, 
                                         timeout=timeout)

                if response.status_code != requests.codes.ok:
                    if response.text:
                        print("")
                        pprint(response.text)

                if response.status_code == requests.codes.ok:
                    print("")
                    print("Success!")
                    print("")
                    print("curl -X POST -H 'Content-Type: [request_mime_type]' -d '[request_body]' %s" % full_model_url.replace('/deploy/','/predict/'))
                else:
                    response.raise_for_status()
                    print("")
            except requests.exceptions.HTTPError as hte:
                print("Error while deploying model.\nError: '%s'" % str(hte))
                print("")
            except IOError as ioe:
                print("Error while deploying model.\nError: '%s'" % str(ioe))
                print("")
 
        if (os.path.isdir(model_path)):
            print("")
            #print("Cleaning up compressed model tar '%s'..." % model_file)
            #print("")
            os.remove(model_file)


    def _predict(self,
                model_server_url,
                model_type,
                model_name,
                model_tag,
                model_test_request_path,
                model_request_mime_type='application/json',
                model_response_mime_type='application/json',
                timeout=10):

        model_test_request_path = os.path.expandvars(model_test_request_path)
        model_test_request_path = os.path.expanduser(model_test_request_path)
        model_test_request_path = os.path.abspath(model_test_request_path)

        print('model_server_url: %s' % model_server_url)
        print('model_type: %s' % model_type)
        print('model_name: %s' % model_name)
        print('model_tag: %s' % model_tag)
        print('model_test_request_path: %s' % model_test_request_path)
        print('model_request_mime_type: %s' % model_request_mime_type)
        print('model_response_mime_type: %s' % model_response_mime_type)

        full_model_url = "%s/api/%s/model/predict/%s/%s" % (model_server_url.rstrip('/'), PipelineCli._pipeline_api_version, model_type, model_name)
        print("")
        print("Predicting with file '%s' using '%s'" % (model_test_request_path, full_model_url))
        print("")

        with open(model_test_request_path, 'rb') as fh:
            model_input_binary = fh.read()

        headers = {'Content-type': model_request_mime_type, 'Accept': model_response_mime_type} 
        from datetime import datetime 

        begin_time = datetime.now()
        response = requests.post(url=full_model_url, 
                                 headers=headers, 
                                 data=model_input_binary, 
                                 timeout=timeout)
        end_time = datetime.now()

        if response.text:
            print("")
            pprint(response.text)

        if response.status_code == requests.codes.ok:
            print("")
            print("Success!")

        total_time = end_time - begin_time
        print("")
        print("Request time: %s milliseconds" % (total_time.microseconds / 1000))
        print("")


    def model_predict(self,
                      model_type,
                      model_name,
                      model_tag,
                      model_server_url,
                      model_test_request_path,
                      model_test_request_concurrency=1,
                      model_request_mime_type='application/json',
                      model_response_mime_type='application/json'):

        from concurrent.futures import ThreadPoolExecutor, as_completed

        with ThreadPoolExecutor(max_workers=model_test_request_concurrency) as executor:
            for _ in range(model_test_request_concurrency):
                executor.submit(self._predict(model_server_url,
                                              model_type,
                                              model_name,
                                              model_tag,
                                              model_test_request_path,
                                              model_request_mime_type,
                                              model_response_mime_type))


    def services(self):

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        print("")
        print("Available Services")
        print("******************")
        self._get_all_available_services()

        print("")
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
        print("Deployments")
        print("***********")
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1_beta1.list_deployment_for_all_namespaces(watch=False,
                                                                              pretty=True)
            deployments = response.items
            for deployment in deployments:
                print("%s (Available Replicas: %s)" % (deployment.metadata.name, deployment.status.available_replicas))

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
        self.config()
        print("")


    def _get_pod_by_service_name(self,
                             service_name):

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        found = False 
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_pod_for_all_namespaces(watch=False, pretty=True)
            pods = response.items
            for pod in pods:
                if service_name in pod.metadata.name:
                    found = True
                    break
        if found:
            return pod
        else:
            return None


    def _get_svc_by_service_name(self,
                             service_name):

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
                if service_name in svc.metadata.name:
                    found = True
                    break
        if found:
            return svc 
        else:
            return None


    def _get_all_available_services(self):

        available_services = list(PipelineCli._kube_deploy_registry.keys())
        available_services.sort()
        for service in available_services:
            print(service)


    def cluster_nodes(self):

        print("")
        print("Nodes")
        print("*****")
        self._get_all_nodes()
        print("")


    def _get_all_nodes(self):

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_node(watch=False, pretty=True)
            nodes = response.items
            for node in nodes:
                print("%s" % node.metadata.labels['kubernetes.io/hostname'])


    def service_shell(self,
                     service_name):

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1.list_pod_for_all_namespaces(watch=False, 
                                                                 pretty=True)
            pods = response.items
            for pod in pods:
                if service_name in pod.metadata.name:
                    break
            print("")
            print("Connecting to '%s'" % pod.metadata.name)      
            print("")
            subprocess.call("kubectl exec -it %s bash" % pod.metadata.name, shell=True)
        print("")


    def service_logs(self,
                     service_name):

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
                if service_name in pod.metadata.name:
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
                print("App '%s' is not running." % service_name)
                print("")


    def service_scale(self,
                      service_name,
                      replicas):

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
                if service_name in deploy.metadata.name:
                    found = True
                    break
            if found:
                print("")
                print("Scaling service '%s' to '%s' replicas." % (deploy.metadata.name, replicas))
                print("")
                cmd = "kubectl scale deploy %s --replicas=%s" % (deploy.metadata.name, replicas)
                print("Running '%s'." % cmd)
                print("")
                subprocess.call(cmd, shell=True)
                print("")
                print("Check status with 'pipeline services'.")
                print("")
            else:
                print("")
                print("App '%s' is not running." % service_name)
                print("") 


    def cluster_volumes(self):

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


    def _get_deploy_yamls(self, 
                          service_name,
                          git_home,
                          git_version):
        try:
            (deploy_yamls, dependencies) = PipelineCli._kube_deploy_registry[service_name]
        except:
            dependencies = []
            deploy_yamls = []

        if len(dependencies) > 0:
            for dependency in dependencies:
                deploy_yamls = deploy_yamls + self._get_deploy_yamls(dependency)

        deploy_yamls = ['%s/%s/%s' % (git_home, git_version, deploy_yaml) for deploy_yaml in deploy_yamls]

        return deploy_yamls 


    def _get_svc_yamls(self, 
                       service_name,
                       git_home,
                       git_version):
        try:
            (svc_yamls, dependencies) = PipelineCli._kube_svc_registry[service_name]
        except:
            dependencies = []
            svc_yamls = []
       
        if len(dependencies) > 0:
            for dependency in dependencies:
                svc_yamls = svc_yamls + self._get_svc_yamls(dependency)

        svc_yamls = ['%s/%s/%s' % (git_home, git_version, svc_yaml) for svc_yaml in svc_yamls]

        return svc_yamls


    def kube_create(self,
                    yaml_path,
                    kube_namespace='default'):

        cmd = "kubectl --namespace %s create -f %s --record" % (kube_namespace, yaml_path)
        self.kube_cmd(cmd)


    def kube_delete(self,
                    yaml_path,
                    kube_namespace='default'):

        cmd = "kubectl --namespace %s delete -f %s" % (kube_namespace, yaml_path)
        self.kube_cmd(cmd) 
   
 
    def kube_cmd(self,
                 cmd):
        print("")
        print("Running '%s'." % cmd)
        print("")
        subprocess.call(cmd, shell=True)
        print("")


    """
    Specifying --service-name will use the internally-configured deploy, svc, config, 
    and secret configs in the _kube_registry.  This will override *_yaml_path params passed.
    """
    def service_create(self,
                       service_name,
                       git_home='https://github.com/fluxcapacitor/source.ml',
                       git_version='master',
                       kube_namespace='default'):

        deploy_yaml_filenames = []
        svc_yaml_filenames = []

        deploy_yaml_filenames = deploy_yaml_filenames + self._get_deploy_yamls(service_name, git_home, git_version)
        deploy_yaml_filenames = [deploy_yaml_filename.replace('github.com', 'raw.githubusercontent.com') for deploy_yaml_filename in deploy_yaml_filenames]
        print("Using '%s'" % deploy_yaml_filenames)
 
        svc_yaml_filenames = svc_yaml_filenames + self._get_svc_yamls(service_name, git_home, git_version)
        svc_yaml_filenames = [svc_yaml_filename.replace('github.com', 'raw.githubusercontent.com') for svc_yaml_filename in svc_yaml_filenames]

        print("Using '%s'" % svc_yaml_filenames)

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        print("")
        print("Starting service '%s'." % service_name)
        print("")
        print("Kubernetes Deployments:")
        print("")
        for deploy_yaml_filename in deploy_yaml_filenames:
            cmd = "kubectl create -f %s --record" % deploy_yaml_filename
            print("Running '%s'." % cmd)
            print("")
            subprocess.call(cmd, shell=True)
            print("")
        print("")
        print("Kubernetes Services:")
        print("")
        for svc_yaml_filename in svc_yaml_filenames:
            cmd = "kubectl create -f %s --record" % svc_yaml_filename
            print("Running '%s'." % cmd)
            print("")
            subprocess.call(cmd, shell=True)
            print("")
        print("")
        print("Ignore any 'Already Exists' errors.  These are OK.")
        print("")
        print("Check service status with 'pipeline services'.")
        print("")


    def service_delete(self,
                       service_name,
                       kube_namespace='default'):

        kubeconfig.load_kube_config()
        kubeclient_v1 = kubeclient.CoreV1Api()
        kubeclient_v1_beta1 = kubeclient.ExtensionsV1beta1Api()

        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            response = kubeclient_v1_beta1.list_deployment_for_all_namespaces(watch=False, pretty=True)
            found = False
            deployments = response.items
            for deploy in deployments:
                if service_name in deploy.metadata.name:
                    found = True
                    break
            if found:
                print("")
                print("Deleting service '%s'." % deploy.metadata.name)
                print("")
                cmd = "kubectl delete deploy %s" % deploy.metadata.name
                print("Running '%s'." % cmd)
                print("")
                subprocess.call(cmd, shell=True)
                print("")
                print("Check service status with 'pipeline services'.")
                print("")
            else:
                print("")
                print("Service '%s' is not running." % service_name)
                print("")


def main():
    fire.Fire(PipelineCli)


if __name__ == '__main__':
    main()
