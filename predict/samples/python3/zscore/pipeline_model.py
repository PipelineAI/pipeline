import os
import numpy as np
from pipeline_monitors import PipelineMonitor as Monitor
import json
import cloudpickle as pickle

# The public objects from this module, see:
#    https://docs.python.org/3/tutorial/modules.html#importing-from-a-package

__all__ = ['predict']


# Performance monitors, a-la prometheus...
_monitor_labels= {'model_type':'python3',
                  'model_name':'zscore'}

_transform_request_monitor = Monitor(labels=_monitor_labels,
                                    action='transform_request',
                                    description='monitor for request transformation')

_predict_monitor = Monitor(labels=_monitor_labels,
                          action='predict',
                          description='monitor for actual prediction')

_transform_response_monitor = Monitor(labels=_monitor_labels,
                                     action='transform_response',
                                     description='monitor for response transformation')


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


def predict(request: bytes) -> bytes:
    '''Where the magic happens...'''
    with _transform_request_monitor:
        transformed_request = _transform_request(request)

    with _predict_monitor:
        predictions = _predict(transformed_request)

    with _transform_response_monitor:
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


def _transform_request(self,
                      request):
    request_str = request.decode('utf-8')
    request_str = request_str.strip().replace('\n', ',')
    request_dict = json.loads(request_str)
    return request_dict

def _transform_response(self,
                       response):
    response_json = json.dumps(response)
    return response_json
