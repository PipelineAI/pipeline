import os
import numpy as np
import json
import logging
from pipeline_monitors import PrometheusMonitor as Monitor
from pipeline_models import TensorFlowServingModel
from pipeline_loggers import log_inputs_and_outputs


_logger = logging.getLogger(__name__)
_logger.setLevel(logging.INFO)
_handler = logging.StreamHandler()
_handler.setLevel(logging.INFO)
_logger.addHandler(_handler)


# The public objects from this module, see:
#    https://docs.python.org/3/tutorial/modules.html#importing-from-a-package

__all__ = ['predict']


# Performance monitors, a-la prometheus...
_labels= {'model_type':'tensorflow',
          'model_name':'mnist'}

_transform_request_monitor = Monitor(labels=_labels,
                                    action='transform_request',
                                    description='monitor for request transformation')

_predict_monitor = Monitor(labels=_labels,
                          action='predict',
                          description='monitor for actual prediction')

_transform_response_monitor = Monitor(labels=_labels,
                                     action='transform_response',
                                     description='monitor for response transformation')


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


@log_inputs_and_outputs(logger=_logger,
                        labels=_labels)
def predict(request: bytes) -> bytes:
    '''Where the magic happens...'''
    with _transform_request_monitor:
        transformed_request = _transform_request(request)

    with _predict_monitor:
        predictions = _model.predict(transformed_request)

    with _transform_response_monitor:
        return _transform_response(predictions)


def _transform_request(request: bytes) -> np.array:
    request_str = request.decode('utf-8')
    request_json = json.loads(request_str)
    request_np = ((255 - np.array(request_json['image'], dtype=np.uint8)) / 255.0).reshape(1, 784)
    return request_np
         
    
def _transform_response(response: np.array) -> json:
    return json.dumps({"outputs": response.tolist()[0]})
