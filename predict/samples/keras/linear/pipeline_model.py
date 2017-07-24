import os
os.environ['KERAS_BACKEND'] = 'theano'
os.environ['THEANO_FLAGS'] = 'floatX=float32,device=cpu'

import pandas as pd
import numpy as np
import json
from keras_theano_model import KerasTheanoModel
from pipeline_monitors import PrometheusMonitor as Monitor
from pipeline_loggers import log_inputs_and_outputs

import logging

_logger = logging.getLogger('model_logger')
_logger.setLevel(logging.INFO)

_logger_stream_handler = logging.StreamHandler()
_logger_stream_handler.setLevel(logging.INFO)

_logger.addHandler(_logger_stream_handler)

# The public objects from this module, see:
#    https://docs.python.org/3/tutorial/modules.html#importing-from-a-package

__all__ = ['predict']

# Performance monitors, a-la prometheus...
_labels= {'model_type':'keras',
          'model_name':'linear'}
                 
_transform_request_monitor = Monitor(labels=_labels, 
                                    action='transform_request', 
                                    description='monitor for request transformation')

_transform_feature_monitor = Monitor(labels=_labels, 
                                     action='transform_features', 
                                     description='monitor for features transformation')

_predict_monitor = Monitor(labels=_labels,
                          action='predict', 
                          description='monitor for actual prediction')

_transform_response_monitor = Monitor(labels=_labels,
                                     action='transform_response', 
                                     description='monitor for response transformation')


def _initialize_upon_import(model_state_path: str) -> KerasTheanoModel:
    ''' Initialize / Restore Model Object.
    '''
    return KerasTheanoModel(model_state_path)


# This is called unconditionally at *module import time*...
_model = _initialize_upon_import(os.path.join('state/keras_theano_linear_model_state.h5'))


def _json_hash_fn(*args):
    request = args[0][0]
    request_str = request.decode('utf-8')
    request_str = request_str.strip().replace('\n', ',')
    # surround the json with '[' ']' to prepare for conversion
    request_str = '[%s]' % request_str
    request_json = json.loads(request_str)
    return hash(json.dumps(request_json, sort_keys=True))


@log_inputs_and_outputs(logger=_logger, labels=_labels, hash_fn=_json_hash_fn) 
def predict(request: bytes) -> bytes:
    '''Where the magic happens...'''
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
