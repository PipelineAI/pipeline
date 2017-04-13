import os
import tornado.ioloop
import tornado.web
import tornado.httpserver
import tornado.httputil
import tornado.gen
import json
import numpy as np
from hystrix import Command
import asyncio
import fnmatch

from transformers import input_transformer, output_transformer

from grpc.beta import implementations
import tensorflow as tf
import predict_pb2
import prediction_service_pb2

class TensorflowServingGrpcCommand(Command):
    raw_inputs = [[]] 
    grpc_host = ''
    grpc_port = -1
    request_timeout = -1
    model_namespace = ''
    model_name = ''
    model_version = -1

    def run(self):
        # Create gRPC client and request
        channel = implementations.insecure_channel(self.grpc_host, self.grpc_port)
        stub = prediction_service_pb2.beta_create_PredictionService_stub(channel)
        request = predict_pb2.PredictRequest()
        request.model_spec.name = self.model_name
        if self.model_version > 0:
            request.model_spec.version.value = self.model_version

        # Transform raw inputs (ie. json) to TensorFlow Serving Request
        transformed_input = input_transformer(self.raw_inputs)
    
        # Send request
        output = stub.Predict(transformed_input, self.request_timeout)

        # Transform TensorFlow Serving Response to raw output (ie. json)
        return output_transformer(output)

    def fallback(self):
        return 'fallback!'

class MainHandler(tornado.web.RequestHandler):
    grpc_host = ''
    grpc_port = -1
    request_timeout = -1

    def initialize(self, grpc_host, grpc_port, request_timeout):
        self.grpc_host = grpc_host
        self.grpc_port = grpc_port
        self.request_timeout = request_timeout    

    @tornado.gen.coroutine
    def post(self, model_namespace, model_name, model_version):
        command = self.build_command(model_namespace, model_name, model_version, self.request.body)

        output = yield self.build_future(command)   

        self.write(output)
  
    def build_command(self, model_namespace, model_name, model_version, raw_inputs):
        command = TensorflowServingGrpcCommand()
        command.grpc_host = self.grpc_host
        command.grpc_port = self.grpc_port
        command.request_timeout = self.request_timeout
        command.model_namespace = model_namespace
        command.model_name = model_name
        command.model_version = model_version  
        command.raw_inputs = raw_inputs

        command.name = 'TensorflowServingGrpcCommand_%s_%s_%s' % (model_namespace, model_name, model_version)
        command.group_name = 'TensorflowServingGrpcCommandGroup'

        return command

    def build_future(self, command):
        future = command.observe()
        future.add_done_callback(future.result)
        return future

if __name__ == "__main__":
    http_grpc_proxy_port = os.environ['PIO_MODEL_HTTP_GRPC_PROXY_SERVER_PORT']
    grpc_host = "127.0.0.1"
    grpc_port = os.environ['PIO_MODEL_SERVER_PORT']
    request_timeout = 5.0 # 5 seconds

    store_home = os.environ['STORE_HOME']
    model_namespace = os.environ['PIO_MODEL_NAMESPACE']
    model_name = os.environ['PIO_MODEL_NAME']

    app = tornado.web.Application([
      # url: /$PIO_MODEL_NAMESPACE/$PIO_MODEL_NAME/$PIO_MODEL_VERSION/
      (r"/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)", MainHandler, 
        dict={grpc_host=grpc_host, grpc_port=grpc_port, request_timeout=request_timeout}
      )
    ])
    app.listen(http_grpc_proxy_port)

    print("*****************************************************")
    print("PIO_MODEL_HTTP_GRPC_PROXY_SERVER_PORT: %s" % http_grpc_proxy_port)
    print("PIO_MODEL_SERVER_PORT: %s" % grpc_port)
    print("PIO_MODEL_NAMESPACE: %s" % model_namespace)
    print("PIO_MODEL_NAME: %s" % model_name)
    print("*****************************************************")
    tornado.ioloop.IOLoop.current().start()
