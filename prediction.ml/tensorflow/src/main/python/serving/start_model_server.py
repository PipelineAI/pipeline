#!/usr/bin/env python3

import os
import tornado.ioloop
import tornado.web
import tornado.httpserver
import tornado.httputil
import tornado.gen
import json
import pickle
import numpy as np
from hystrix import Command
import asyncio
import fnmatch

from transformers import input_transformer, output_transformer

class PredictCommand(Command):
    inputs = [[]]
    grpc_port = -1
    model = None

    def run(self):
        # Create gRPC client and request
        grpc_port = int(sys.argv[2])
        channel = implementations.insecure_channel(grpc_host, grpc_port)
        stub = prediction_service_pb2.beta_create_PredictionService_stub(channel)
        request = predict_pb2.PredictRequest()
        request.model_spec.name = model_name
        if model_version > 0:
            request.model_spec.version.value = model_version

        # TODO:  don't hard code this!
        inputs_np = np.asarray([1.0])
        #print(inputs_np)
        inputs_tensor_proto = tf.contrib.util.make_tensor_proto(inputs_np,
                                                                dtype=tf.float32)
        request.inputs['x_observed'].CopyFrom(inputs_tensor_proto)

        # Send request
        result = stub.Predict(request, request_timeout)
        #print(result)

        result_np = tf.contrib.util.make_ndarray(result.outputs['y_pred'])
        #print(result_np)

    return result_np

    def fallback(self):
        return 'fallback!'

class MainHandler(tornado.web.RequestHandler):
    @tornado.gen.coroutine
    def post(self, model_namespace, model_name, model_version):
        self.set_header("Content-Type", "application/json")
        command = self.build_command(model_namespace, model_name, model_version)
        output = yield self.build_future(command)
        self.write(output_transformer(output))

    def build_command(self, model_namespace, model_name, model_version):
        command = PredictCommand()
        model_key = '%s_%s_%s' % (model_namespace, model_name, model_version)

        command.name = 'Predict_%s' % model_key
        command.group_name = 'PredictGroup'
        model_key = '%s_%s_%s' % (model_namespace, model_name, model_version)
        if model_key in model_registry:
            model = model_registry[model_key]
        else:
            (model_absolute_path, model) = load_model(model_namespace, model_name, model_version)
            model_registry[model_key] = model
        command.model = model
        command.inputs = input_transformer(self.request.body)
        return command

    def build_future(self, command):
        future = command.observe()
        future.add_done_callback(future.result)
        return future

def load_model(model_namespace, model_name, model_version):
    model_absolute_path = os.path.join(store_home, model_namespace)
    model_absolute_path = os.path.join(model_absolute_path, model_name)
    model_absolute_path = os.path.join(model_absolute_path, model_version)

    # TODO:  dynamically find model_filename similar to `run` bash script
#    model_filename = os.environ['PIO_MODEL_FILENAME']
    model_filename = fnmatch.filter(os.listdir(model_absolute_path), "*.pkl")[0]

    model_absolute_path = os.path.join(model_absolute_path, model_filename)

    with open(model_absolute_path, 'rb') as model_file:
        model = pickle.load(model_file)
    return (model_absolute_path, model)

if __name__ == '__main__':
    port = os.environ['PIO_MODEL_SERVER_PORT']

    store_home = os.environ['STORE_HOME']
    model_namespace = os.environ['PIO_MODEL_NAMESPACE']
    model_name = os.environ['PIO_MODEL_NAME']
    model_version = os.environ['PIO_MODEL_VERSION']

    model_registry = {}

    model_key = '%s_%s_%s' % (model_namespace, model_name, model_version)
    (model_absolute_path, model) = load_model(model_namespace, model_name, model_version)

    model_registry[model_key] = model

    app = tornado.web.Application([
      # url: /$PIO_NAMESPACE/$PIO_MODELNAME/$PIO_MODEL_VERSION/
      (r"/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)", MainHandler)
    ])
    app.listen(port)

    print("")
    print("Started Tornado-based Http Server on Port %s" % port)
    print("")
    print("Loaded Model from `%s`" % model_absolute_path)
    print("")

    tornado.ioloop.IOLoop.current().start()
