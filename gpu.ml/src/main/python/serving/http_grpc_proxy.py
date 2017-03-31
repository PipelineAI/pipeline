import sys
import tornado.ioloop
import tornado.web
import tornado.httpserver
import tornado.httputil
import tornado.gen
from tornado import escape
import json
import pickle
import numpy as np

from grpc.beta import implementations
import tensorflow as tf
import predict_pb2
import prediction_service_pb2

from hystrix import Command
import asyncio

grpc_host = "127.0.0.1"
grpc_port = 9000
model_name = "linear"
model_version = -1 # Latest version 
request_timeout = 5.0 # 5 seconds

class TensorflowServingGrpcCommand(Command):
  def run(self):
    # TODO:  pass on the actual inputs
    return self.do_post([[1,1,3,4]]) 

  def do_post(self, inputs):
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
  def post(self):
    self.set_header("Content-Type", "application/json")

    command = self.build_command()

    do_post_result = yield self.build_future(command)   

    # TODO:  Convert do_post_result from numpy_array to json 

    self.write(json.dumps(do_post_result.tolist()))

  def build_command(self):
    command = TensorflowServingGrpcCommand()
    command.name = 'TensorflowServingGrpcCommand'
    command.group_name = 'TensorflowServingGrpcCommandGroup'
    return command

  def build_future(self, command):
    future = command.observe()
    future.add_done_callback(future.result)
    return future

#  def return_result(self, future):
#    return future.result()
 
#  @tornado.gen.coroutine
#  def do_post(self, inputs):
#    return decision_tree_model.predict(inputs).tolist()

  def prepare(self):
    #print(self.request.body)
    if self.request.headers["Content-Type"].startswith("application/json"):
        # TODO:  Fix this to actually accept inputs
        self.json_args = None
          #json.loads(str(self.request.body))
    else:
        self.json_args = None

if __name__ == "__main__":
#  decision_tree_pkl_filename = '1/python_balancescale.pkl'
#  decision_tree_model_pkl = open(decision_tree_pkl_filename, 'rb')
#  decision_tree_model = pickle.load(decision_tree_model_pkl)
  app = tornado.web.Application([
      (r"/", MainHandler),
  ])
  listen_port = int(sys.argv[1])
  app.listen(listen_port)

  print("*****************************************************")
  print("Tornado-based http_grpc_proxy listening on port %s" % listen_port)
  print("*****************************************************")
  tornado.ioloop.IOLoop.current().start()
