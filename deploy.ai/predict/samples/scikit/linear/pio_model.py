import os
import numpy as np
from pio_monitors import Monitor
import ujson
import cloudpickle as pickle

# The public objects from this module, see:
#    https://docs.python.org/3/tutorial/modules.html#importing-from-a-package

__all__ = ['predict']

# Performance monitors, a-la prometheus...
_monitor_labels= {'model_type':'scikit',
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
        predictions = _model.predict(transformed_request)

    with _transform_response_monitor:
        return _transform_response(predictions)


def _transform_request(request: bytes) -> np.array:
        request_str = request.decode('utf-8')
        request_str = request_str.strip().replace('\n', ',')
        # surround the json with '[' ']' to prepare for conversion
        request_str = '[%s]' % request_str
        request_json = ujson.loads(request_str)
        request_transformed = ([json_line['feature0'] for json_line in request_json])
        return np.array(request_transformed)


def _transform_response(response: np.array) -> ujson:
        return ujson.dumps(response.tolist())
