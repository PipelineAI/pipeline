#!/usr/bin/env python
import sys

import numpy

from grpc.beta import implementations
import tensorflow as tf
import os
import predict_pb2
import prediction_service_pb2

def main():
  host = "127.0.0.1" 
  port = os.environ['PIO_MODEL_SERVER_PORT']
  namespace = os.environ['PIO_MODEL_NAMESPACE'] 
  model_name = os.environ['PIO_MODEL_NAME'] 
  model_version = os.environ['PIO_MODEL_VERSION']
  request_timeout = 5.0

  # Create gRPC client and request
  channel = implementations.insecure_channel(host, port)
  stub = prediction_service_pb2.beta_create_PredictionService_stub(channel)
  request = predict_pb2.PredictRequest()
  request.model_spec.name = model_name
  if model_version > 0:
    request.model_spec.version.value = model_version

  inputs_raw = sys.argv[1]
  # TODO:  convert raw json request.body into np array
  inputs_np = numpy.asarray([inputs_raw])
  inputs_tensor_proto = tf.contrib.util.make_tensor_proto(inputs_np,
                                                          dtype=tf.float32)
  request.inputs['x_observed'].CopyFrom(inputs_tensor_proto)

  # Send request
  result = stub.Predict(request, request_timeout)
  print(result)
  
  result_np = tf.contrib.util.make_ndarray(result.outputs['y_pred'])
  print('\n%s\n' % result_np) 

if __name__ == '__main__':
  main()
