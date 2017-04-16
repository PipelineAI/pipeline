# -*- coding: utf-8 -*-

__version__ = "0.3"

import requests
import fire
import tarfile
import os

# TODO: enums
#   model_type = ['xml', 'file']
#   return_type = ['json']
#   compression_type =['None', 'tar', 'tar.gz']

class PioCli(object):
    """PipelineIO CLI"""
    
    def deploy_with_string(_sentinel=None,
                           request_timeout=10, 
                           model_server_url=None, 
                           model_namespace=None, 
                           model_name=None, 
                           model_version=None, 
                           model_type=None, 
                           model_str=None, 
                           return_type=None):

        model_server_url = "%s/%s/%s/%s" % (model_server_url, model_namespace, model_name, model_version)
        print("Deploy model '%s' to %s" % (model_name, model_server_url))
        print("")

        headers = {'Content-type': 'application/%s' % model_type, 'Accept': 'application/%s' % return_type}
        response = requests.post(url=model_server_url, headers=headers, data=model_str, timeout=request_timeout)
        print(response.text)

    def predict_with_string(_sentinel=None, 
                            request_timeout=5, 
                            model_server_url=None, 
                            model_namespace=None, 
                            model_name=None, 
                            model_version=None, 
                            input_type=None, 
                            input_str=None, 
                            return_type=None):

        model_server_url = "%s/%s/%s/%s" % (model_server_url, model_namespace, model_name, model_version)
        print("Evaluate string '%s' at '%s'" % (input_str, model_server_url))
        print("")

        headers = {'Content-type': 'application/%s' % input_type, 'Accept': 'application/%s' % return_type}
        response = requests.post(url=model_server_url, headers=headers, data=input_str, timeout=request_timeout)
        print(response.text)

    def deploy_with_file(_sentinel=None, 
                         request_timeout=10, 
                         model_server_url=None, 
                         model_namespace=None, 
                         model_name=None, 
                         model_version=None, 
                         model_type=None, 
                         model_file_key=None, 
                         model_filename=None, 
                         return_type=None):
        with open(model_filename, 'rb') as file:
            files = [(model_file_key, (model_filename, file))]

        model_server_url = "%s/%s/%s/%s" % (model_server_url, model_namespace, model_name, model_version)
        print("Deploy model '%s' to %s" % (model_filename, model_server_url))
        print("")

        headers = {'Accept': 'application/%s' % return_type}
        response = requests.post(url=model_server_url, headers=headers, files=files, timeout=request_timeout)
        print(response.text)

    def predict_with_file(_sentinel=None, 
                          request_timeout=10, 
                          model_server_url=None, 
                          model_namespace=None, 
                          model_name=None, 
                          model_version=None, 
                          input_type=None, 
                          input_file_key=None, 
                          input_filename=None, 
                          return_type=None):
        with open(input_filename, 'rb') as file:
            files = [(input_file_key, (input_filename, file))]

        model_server_url = "%s/%s/%s/%s" % (model_server_url, model_namespace, model_name, model_version)
        print("Predict file '%s' at '%s'" % (input_filename, model_server_url))
        print("")

        headers = {'Accept': 'application/%s' % return_type} 
        response = requests.post(url=model_server_url, headers=headers, files=files, timeout=request_timeout)
        print(response.text)

    def deploy_with_dir(_sentinel=None,
                        request_timeout=10, 
                        model_server_url=None, 
                        model_namespace=None, 
                        model_name=None, 
                        model_version=None, 
                        model_type=None, 
                        model_file_key=None, 
                        model_dir=None, 
                        return_type=None, 
                        compression_type=None):
        # TODO:  Compress the dir
        model_filename = '/tmp/bundle.tar.gz'


        with tarfile.open(model_filename, 'w:gz') as tar:
            tar.add(model_dir)

        model_server_url = "%s/%s/%s/%s" % (model_server_url, model_namespace, model_name, model_version)
        print("Deploying model bundle '%s' to %s" % (model_filename, model_server_url))
        print("")

        with open(model_filename, 'rb') as fh:
            files = [(model_file_key, (model_filename, fh))]
            headers = {'Accept': 'application/%s' % return_type}
            response = requests.post(url=model_server_url, headers=headers, files=files, timeout=request_timeout)
            print(response.text)

def main():
    fire.Fire(PioCli)

if __name__ == '__main__':
    main()
