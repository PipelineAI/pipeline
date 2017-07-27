import os
import numpy as np
import json
import cloudpickle as pickle
import logging

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
_labels= {'model_type':'python',
                  'model_name':'zscore'}


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
        predictions = _predict(transformed_request)

    return _transform_response(predictions)


def _predict(inputs: dict) -> bytes:
    cat_affinity_score = sum([ d['weight'] * d['user_score'] for d in inputs if 'cat' in d['tags'] ])
    dog_affinity_score = sum([ d['weight'] * d['user_score'] for d in inputs if 'dog' in d['tags'] ])

    # create normalized z score for compare/classify
    cat_zscore = (cat_affinity_score - _model['cat_mean'])/_model['cat_stdv']
    dog_zscore = (dog_affinity_score - _model['dog_mean'])/_model['dog_stdv']

    # classify
    if abs(cat_zscore) > abs(dog_zscore):
        if cat_zscore >= 0:
            category = 'cat_lover'
        else:
            category = 'cat_hater'
    else:
        if dog_zscore >= 0:
            category = 'dog_lover'
        else:
            category = 'dog_hater'

    response = {
        'category': category,
        'cat_affinity_score': cat_affinity_score,
        'dog_affinity_score': dog_affinity_score,
        'cat_zscore': cat_zscore,
        'cat_zscore': dog_zscore
    }

    return response


@monitor(labels=_labels, name="transform_request")
def _transform_request(self,
                      request):
    request_str = request.decode('utf-8')
    request_str = request_str.strip().replace('\n', ',')
    request_dict = json.loads(request_str)
    return request_dict


@monitor(labels=_labels, name="transform_response")
def _transform_response(self,
                       response):
    response_json = json.dumps(response)
    return response_json
