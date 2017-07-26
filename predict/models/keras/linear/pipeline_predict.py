import os
os.environ['KERAS_BACKEND'] = 'theano'
os.environ['THEANO_FLAGS'] = 'floatX=float32,device=cpu'

import pandas as pd
import numpy as np
import json
import logging

from keras_theano_model import KerasTheanoModel
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
_labels= {'model_type':'keras',
          'model_name':'linear'}
                 

def _initialize_upon_import(model_state_path: str) -> KerasTheanoModel:
    ''' Initialize / Restore Model Object.
    '''
    return KerasTheanoModel(model_state_path)


# This is called unconditionally at *module import time*...
_model = _initialize_upon_import(os.path.join('state/keras_theano_linear_model_state.h5'))


@monitor(labels=_labels, name="transform_request")
def _json_to_numpy(request: bytes) -> np.array:
    request_str = request.decode('utf-8')
    request_str = request_str.strip().replace('\n', ',')
    # surround the json with '[' ']' to prepare for conversion
    request_str = '[%s]' % request_str
    request_json = json.loads(request_str)
    request_transformed = ([json_line['ppt'] for json_line in request_json])
    return np.array(request_transformed)


@monitor(labels=_labels, name="transform_response")
def _numpy_to_json(response: np.array) -> bytes:
    return json.dumps(response.tolist())


@monitor(labels=_labels, name="predict")
@log(labels=_labels, logger=_logger)
def predict(request: bytes) -> bytes:
    '''Where the magic happens...'''
    transformed_request = _json_to_numpy(request)

    predictions = _model.predict(transformed_request)

    return _numpy_to_json(predictions)
