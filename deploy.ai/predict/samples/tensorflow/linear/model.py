import os
import numpy as np
from pio_monitors import Monitor
import json
from pio_models import TensorFlowServingModel

# The public objects from this module, see:
#    https://docs.python.org/3/tutorial/modules.html#importing-from-a-package

__all__ = ['predict']


# Performance monitors, a-la prometheus...
_monitor_labels= {'model_type':'tensorflow',
                  'model_name':'linear'}

_transform_request_monitor = Monitor(labels=_monitor_labels,
                                    action='transform_request',
                                    description='monitor for request transformation')

_predict_monitor = Monitor(labels=_monitor_labels,
                          action='predict',
                          description='monitor for actual prediction')

_transform_response_monitor = Monitor(labels=_monitor_labels,
                                     action='transform_response',
                                     description='monitor for response transformation')


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
    request_np = np.asarray([request_json['inputs']])
    return request_np
         
    
def _transform_response(response: np.array) -> json:
    return json.dumps({"outputs": response.tolist()[0]})
