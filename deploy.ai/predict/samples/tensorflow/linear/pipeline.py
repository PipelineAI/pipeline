import os
import numpy as np
from pio_monitors import Monitor
from tensorflow_serving_model import TensorFlowServingModel
import json

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

def _initialize_upon_import() -> TensorFlowServingModel 
    ''' Initialize / Restore Model Object.
    '''
    return TensorFlowServingModel(host='localhost', 
                                  port=9000,
                                  inputs_name='inputs',
                                  outputs_name='outputs',
                                  timeout=100)


# This is called unconditionally at *module import time*...
_model = _initialize_upon_import(os.path.join('state/keras_theano_linear_model_state.h5'))


def predict(request: bytes) -> bytes:
    '''Where the magic happens...'''
    print(request)
    with _transform_request_monitor:
        transformed_request = _transform_request(request)

    with _predict_monitor:
        predictions = _model.predict(transformed_request)

    with _transform_response_monitor:
        return _transform_response(predictions)


def _transform_request(self,
                       request: bytes) -> nparray:
    request_str = request.decode('utf-8')
    request_json = json.loads(request_str)
    request_np = np.asarray([request_json['inputs']])
    return request_np
         
    
def _transform_response(self,
                       response: nparray) -> json:
    return json.dumps({"outputs": response.tolist()[0]})


def _test() -> bytes:
    with open('data/test_request.json', 'rb') as fh:
        request_binary = fh.read()

    response = predict(request_binary)
    return response


if __name__ == '__main__':
    response = _test()
    print(response)

#if __name__ == '__main__':
#    import cloudpickle as pickle

#    pipeline = Pipeline()

#    pipeline_pkl_path = 'pipeline.pkl'

#    with open(pipeline_pkl_path, 'wb') as fh:
#        pickle.dump(pipeline, fh)
