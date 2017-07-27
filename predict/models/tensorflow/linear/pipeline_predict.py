import os
import numpy as np
import json

from pipeline_models import TensorFlowServingModel
from pipeline_monitors import prometheus_monitor as monitor
from pipeline_loggers import log

_logger = logging.getLogger('model_logger')
_logger.setLevel(logging.INFO)
_logger_stream_handler = logging.StreamHandler()
_logger_stream_handler.setLevel(logging.INFO)
_logger.addHandler(_logger_stream_handler)


# The public objects from this module, see:
#    https://docs.python.org/3/tutorial/modules.html#importing-from-a-package

__all__ = ['predict']


# Performance monitors, a-la prometheus...
_labels= {'model_type':'tensorflow',
          'model_name':'linear'}


def _initialize_upon_import() -> TensorFlowServingModel:
    ''' Initialize / Restore Model Object.
    '''
    return TensorFlowServingModel(host='localhost', 
                                  port=9000,
                                  model_name='linear',
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
    request_np = np.asarray([request_json['inputs']])
    return request_np
         

@monitor(labels=_labels, name="transform_repsonse") 
def _transform_response(response: np.array) -> json:
    return json.dumps({"outputs": response.tolist()[0]})
