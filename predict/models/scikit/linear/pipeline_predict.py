import os
import numpy as np
from pipeline_monitors import prometheus_monitor as monitor
from pipeline_loggers import log
import ujson
import cloudpickle as pickle
import logging

_logger = logging.getLogger('model_logger')
_logger.setLevel(logging.INFO)
_logger_stream_handler = logging.StreamHandler()
_logger_stream_handler.setLevel(logging.INFO)
_logger.addHandler(_logger_stream_handler)

__all__ = ['predict']


_labels = {'model_type':'scikit',
           'model_name':'linear'}


def _initialize_upon_import():
    ''' Initialize / Restore Model Object.
    '''
    model_pkl_path = 'model.pkl'

    # Load pickled model from model directory
    with open(model_pkl_path, 'rb') as fh:
        restored_model = pickle.load(fh)

    return restored_model


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
    request_str = request_str.strip().replace('\n', ',')
    # surround the json with '[' ']' to prepare for conversion
    request_str = '[%s]' % request_str
    request_json = ujson.loads(request_str)
    request_transformed = ([json_line['feature0'] for json_line in request_json])
    return np.array(request_transformed)


@monitor(labels=_labels, name="transform_response")
def _transform_response(response: np.array) -> ujson:
    return ujson.dumps(response.tolist())
