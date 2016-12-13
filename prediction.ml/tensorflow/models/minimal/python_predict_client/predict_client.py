#!/usr/bin/env python

import numpy

from grpc.beta import implementations
import tensorflow as tf

import predict_pb2
import prediction_service_pb2

tf.app.flags.DEFINE_string("host", "127.0.0.1", "gRPC server host")
tf.app.flags.DEFINE_integer("port", 9000, "gRPC server port")
tf.app.flags.DEFINE_string("model_name", "cancer", "TensorFlow model name")
tf.app.flags.DEFINE_integer("model_version", 1, "TensorFlow model version")
tf.app.flags.DEFINE_float("request_timeout", 10.0, "Timeout of gRPC request")
FLAGS = tf.app.flags.FLAGS


def main():
  host = FLAGS.host
  port = FLAGS.port
  model_name = FLAGS.model_name
  model_version = FLAGS.model_version
  request_timeout = FLAGS.request_timeout

  # Generate inference data
  features = numpy.asarray(
      [1, 2, 3, 4, 5, 6, 7, 8, 9])
  features_tensor_proto = tf.contrib.util.make_tensor_proto(features,
                                                            dtype=tf.float32)

  # Create gRPC client and request
  channel = implementations.insecure_channel(host, port)
  stub = prediction_service_pb2.beta_create_PredictionService_stub(channel)
  request = predict_pb2.PredictRequest()
  request.model_spec.name = model_name
  request.model_spec.version.value = model_version
  request.inputs['features'].CopyFrom(features_tensor_proto)

  # Send request
  result = stub.Predict(request, request_timeout)
  print(result)


if __name__ == '__main__':
  main()
