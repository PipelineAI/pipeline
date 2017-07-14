import os
os.environ['KERAS_BACKEND'] = 'theano'
os.environ['THEANO_FLAGS'] = 'floatX=float32,device=cpu'

import pandas as pd
import numpy as np
import json
from keras_theano_model import KerasTheanoModel
from pio_monitors import Monitor

# The public objects from this module, see:
#    https://docs.python.org/3/tutorial/modules.html#importing-from-a-package

__all__ = ['predict']

# Performance monitors, a-la prometheus...
_monitor_labels= {'model_type':'keras',
                  'model_name':'linear'}
                 
_transform_request_monitor = Monitor(labels=_monitor_labels, 
                                    action='transform_request', 
                                    description='monitor for request transformation')

_transform_feature_monitor = Monitor(labels=_monitor_labels, 
                                     action='transform_features', 
                                     description='monitor for features transformation')

_predict_monitor = Monitor(labels=_monitor_labels,
                          action='predict', 
                          description='monitor for actual prediction')

_transform_response_monitor = Monitor(labels=_monitor_labels,
                                     action='transform_response', 
                                     description='monitor for response transformation')


def _initialize_upon_import(model_state_path: str) -> KerasTheanoModel:
    ''' Initialize / Restore Model Object.
    '''
    return KerasTheanoModel(model_state_path)


# This is called unconditionally at *module import time*...
_model = _initialize_upon_import(os.path.join('state/keras_theano_linear_model_state.h5'))


def predict(request: bytes) -> bytes:
    '''Where the magic happens...'''
    print(request)
    with _transform_request_monitor:
        transformed_request = _json_to_numpy(request)

    with _predict_monitor:
        predictions = _model.predict(transformed_request)

    with _transform_response_monitor:
        return _numpy_to_json(predictions)


def _json_to_numpy(request: bytes) -> bytes:
    request_str = request.decode('utf-8')
    request_str = request_str.strip().replace('\n', ',')
    # surround the json with '[' ']' to prepare for conversion
    request_str = '[%s]' % request_str
    request_json = json.loads(request_str)
    request_transformed = ([json_line['ppt'] for json_line in request_json])
    return np.array(request_transformed)


def _numpy_to_json(response: np.array) -> bytes:
    return json.dumps(response.tolist())
