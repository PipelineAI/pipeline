#!/usr/bin/env python3

import os
import sys
import tornado.ioloop
import tornado.web
import tornado.httpserver
import tornado.httputil
import tornado.gen
import importlib.util
from grpc.beta import implementations
import asyncio
import tensorflow as tf
import predict_pb2
import prediction_service_pb2


class ModelPredictPython3TensorFlow1Handler(tornado.web.RequestHandler):
    def initialize(self, 
                   bundle_parent_path,
                   grpc_host,
                   grpc_port,
                   request_timeout):
        self.bundle_parent_path = bundle_parent_path
        self.grpc_host = grpc_host
        self.grpc_port = grpc_port
        self.request_timeout = request_timeout
        self.registry = {}


    @tornado.gen.coroutine
    def post(self, model_namespace, model_name, model_version):
        (model_base_path, transformers_module) = self.get_model_assets(model_namespace,
                                                                       model_name,
                                                                       model_version)

        output = yield self.do_post(self.request.body, model_base_path, transformers_module, model_name, model_version)
        self.write(output)


    @tornado.gen.coroutine
    def do_post(self, inputs, model_base_path, transformers_module, model_name, model_version):
        request = predict_pb2.PredictRequest()
        request.model_spec.name = model_name
        request.model_spec.version.value = model_version
        transformed_inputs = transformers_module.transform_inputs(inputs)
        request.inputs['inputs'].CopyFrom(transformed_inputs)
        channel = implementations.insecure_channel(self.grpc_host, self.grpc_port)
        stub = prediction_service_pb2.beta_create_PredictionService_stub(channel)
        outputs = stub.Predict(request, self.request_timeout)
        transformed_outputs = transformers_module.transform_outputs(outputs)
        return transformed_outputs


    def get_model_assets(self, model_namespace, model_name, model_version):
        model_key = '%s_%s_%s' % (model_namespace, model_name, model_version)
        if model_key in self.registry:
            (model_base_path, transformers_module) = self.registry[model_key]
        else:
            model_base_path = os.path.join(self.bundle_parent_path, model_namespace)
            model_base_path = os.path.join(model_base_path, model_name)
            model_base_path = os.path.join(model_base_path, model_version)

            # Load model_io_transformers from model directory
            transformers_module_name = 'model_io_transformers'
            transformers_source_path = os.path.join(model_base_path, '%s.py' % transformers_module_name)
            spec = importlib.util.spec_from_file_location(transformers_module_name, transformers_source_path)
            transformers_module = importlib.util.module_from_spec(spec)
            spec.loader.exec_module(transformers_module)

            self.registry[model_key] = (model_base_path, transformers_module)

        return self.registry[model_key]


class ModelPython3TensorFlow1UploadHandler(tornado.web.RequestHandler):
    def initialize(self, bundle_parent_path):
        self.bundle_parent_path = bundle_parent_path

    def post(self, model_namespace, model_name, model_version):
        fileinfo = self.request.files['bundle'][0]
        absolutepath = fileinfo['filename']
        (_, filename) = os.path.split(absolutepath)
        bundle_path = os.path.join(self.bundle_parent_path, model_namespace)
        bundle_path_with_model_name = os.path.join(bundle_path, model_name)
        bundle_path = os.path.join(bundle_path_with_model_name, model_version)
        bundle_path_filename = os.path.join(bundle_path, filename)
        try:
            os.makedirs(bundle_path, exist_ok=False)
            with open(bundle_path_filename, 'wb+') as fh:
                fh.write(fileinfo['body'])
            print("%s uploaded %s, saved as %s" %
                        ( str(self.request.remote_ip),
                          str(filename),
                          bundle_path_filename) )
            self.write("Uploading and extracting bundle '%s' into '%s'...\n" % (filename, bundle_path))
            with tarfile.open(bundle_path_filename, "r:gz") as tar:
                tar.extractall(path=bundle_path)
            self.write('...Done!\n')
            self.write('Installing bundle and updating environment...\n')
            # TODO:  Restart TensorFlow Model Serving and point to bundle_path_with_model_name
            #completed_process = subprocess.run('cd %s && ./install.sh' % bundle_path,
            #                                   timeout=600,
            #                                   shell=True,
            #                                   stdout=subprocess.PIPE)
            self.write('...Done!\n')
        except IOError as e:
            print('Failed to write file due to IOError %s' % str(e))
            self.write('Failed to write file due to IOError %s' % str(e))
            raise e

    def write_error(self, status_code, **kwargs):
        self.write('Error %s' % status_code)
        if "exc_info" in kwargs:
            self.write(", Exception: %s" % kwargs["exc_info"][0].__name__)

if __name__ == '__main__':
    port = os.environ['PIO_MODEL_SERVER_PORT']
    bundle_parent_path = os.environ['STORE_HOME']
    model_namespace = os.environ['PIO_MODEL_NAMESPACE']
    model_name = os.environ['PIO_MODEL_NAME']
    model_version = os.environ['PIO_MODEL_VERSION']
    grpc_port = os.environ['PIO_MODEL_TENSORFLOW_SERVING_PORT']

    app = tornado.web.Application([
      # url: /v1/model/predict/python3/tensorflow1/$PIO_NAMESPACE/$PIO_MODELNAME/$PIO_MODEL_VERSION/
      (r"/v1/model/predict/python3/tensorflow1/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)", ModelPredictPython3TensorFlow1Handler,
          dict(bundle_parent_path=bundle_parent_path,
               grpc_host='127.0.0.1',
               grpc_port=grpc_port,
               request_timeout=30)),
      # TODO:  Disable this if we're not explicitly in PIO_MODEL_ENVIRONMENT=dev mode
      # url: /v1/model/upload/$PIO_MODEL_NAMESPACE/$PIO_MODEL_NAME/$PIO_MODEL_VERSION/
      (r"/v1/model/upload/python3/tensorflow1/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)", ModelUploadPython3TensorFlow1Handler,
          dict(bundle_parent_path=bundle_parent_path))
    ])
    app.listen(port)

    print("")
    print("Started Tornado-based Http Server on Port '%s'" % port)
    print("")

    tornado.ioloop.IOLoop.current().start()
