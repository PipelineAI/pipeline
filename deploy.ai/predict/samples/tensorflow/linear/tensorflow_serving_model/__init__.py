import numpy as np
from grpc.beta import implementations
import asyncio
import tensorflow as tf
import predict_pb2
import prediction_service_pb2
 
class TensorFlowServingModel():

    def __init__(self,
                 host: str,
                 port: int,
                 inputs_name: str,
                 outputs_name: str,
                 timeout: int):

        self.host = host
        self.port = port
        self.inputs_name = inputs_name 
        self.outputs_name = outputs_name
        self.timeout = timeout


    def predict(inputs: nparray) -> nparray:

        # TODO:  Reuse instead of creating this channel everytime
        channel = implementations.insecure_channel(self.host,
                                                   self.port) 
        stub = prediction_service_pb2.beta_create_PredictionService_stub(channel)

        # Transform inputs into TensorFlow PredictRequest
        inputs_tensor_proto = tf.make_tensor_proto(inputs,
                                                   dtype=tf.float32)
        tf_request = predict_pb2.PredictRequest()
        tf_request.inputs[self.inputs_name].CopyFrom(inputs_tensor_proto)

        tf_request.model_spec.name = model_name
        #tf_request.model_spec.version.value = int(model_version)

        # Transform TensorFlow PredictResponse into output
        response = stub.Predict(tf_request, self.timeout)
        response_np = tf.contrib.util.make_ndarray(response.output[self.outputs_name])

        return response_np
