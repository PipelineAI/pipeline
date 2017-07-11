import numpy as np
from grpc.beta import implementations
import asyncio
import tensorflow as tf
from pio_models import model_pb2, predict_pb2, prediction_service_pb2

__version__ = "0.3"

class TensorFlowServingModel():

    def __init__(self,
                 host: str,
                 port: int,
                 model_name: str,
                 inputs_name: str,
                 outputs_name: str,
                 timeout: int):

        self._host = host
        self._port = port
        self._model_name = model_name
        self._inputs_name = inputs_name
        self._outputs_name = outputs_name
        self._timeout = timeout


    def predict(self,
                inputs: np.array) -> np.array:

        # TODO:  Reuse instead of creating this channel everytime
        channel = implementations.insecure_channel(self._host,
                                                   self._port)
        stub = prediction_service_pb2.beta_create_PredictionService_stub(channel)

        # Transform inputs into TensorFlow PredictRequest
        inputs_tensor_proto = tf.make_tensor_proto(inputs,
                                                   dtype=tf.float32)
        tf_request = predict_pb2.PredictRequest()
        tf_request.inputs[self._inputs_name].CopyFrom(inputs_tensor_proto)

        tf_request.model_spec.name = self._model_name
        #tf_request.model_spec.version.value = int(model_version)

        # Transform TensorFlow PredictResponse into output
        response = stub.Predict(tf_request, self._timeout)
        response_np = tf.contrib.util.make_ndarray(response.outputs[self._outputs_name])

        return response_np
