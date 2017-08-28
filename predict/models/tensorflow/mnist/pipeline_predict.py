import os
import numpy as np
import json
import logging

from pipeline_models import TensorFlowServingModel
from pipeline_monitors import prometheus_monitor as monitor
from pipeline_loggers import log
from pipeline_loggers.kafka import KafkaHandler

_logger = logging.getLogger('model_logger')
_logger.setLevel(logging.INFO)
_logger_stream_handler = logging.StreamHandler()
_logger_stream_handler.setLevel(logging.INFO)
_logger.addHandler(_logger_stream_handler)

_logger_kafka_handler = KafkaHandler(hosts_list='localhost:9092', topic='prediction-inputs')
_logger.addHandler(_logger_kafka_handler)


__all__ = ['predict']


_labels= {'model_type':'tensorflow',
          'model_name':'mnist'}


def _initialize_upon_import() -> TensorFlowServingModel:
    ''' Initialize / Restore Model Object.
    '''
    return TensorFlowServingModel(host='localhost', 
                                  port=9000,
                                  model_name='mnist',
                                  inputs_name='inputs',
                                  outputs_name='outputs',
                                  timeout=100)


# This is called unconditionally at *module import time*...
_model = _initialize_upon_import()


@log(labels=_labels, logger=_logger)
def predict(request: bytes) -> bytes:
    '''Where the magic happens...'''
    transformed_request = _transform_request(request)

    with monitor(labels=_labels, name="predict"):
        predictions = _model.predict(transformed_request)

    return _transform_response(predictions)


@monitor(labels=_labels, name="transform_request")
def _transform_request(request: bytes) -> np.array:
    request_str = request.decode('utf-8')
    request_json = json.loads(request_str)
    request_np = ((255 - np.array(request_json['image'], dtype=np.uint8)) / 255.0).reshape(1, 784)
    return request_np
         

@monitor(labels=_labels, name="transform_response")    
def _transform_response(response: np.array) -> json:
    return json.dumps({"outputs": response.tolist()[0].decode('utf-8')})
