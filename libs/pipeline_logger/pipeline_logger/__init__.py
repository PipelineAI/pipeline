from logging import Logger
from datetime import datetime
import json
from typing import Callable

__version__ = "1.0.2"

# TODO:  Handle batched inputs and outputs (using above custom fn's - match inputs to outputs!)
# TODO:  Add Monitors around these calls!!

class log(object):

    def __init__(self,
                 labels: dict,
                 logger: Logger,
                 custom_inputs_fn: Callable=None,
                 custom_outputs_fn: Callable=None):

        self._labels = labels
        self._logger = logger
        self._custom_inputs_fn = custom_inputs_fn
        self._custom_outputs_fn = custom_outputs_fn


    def __call__(self, function):

        def wrapped_function(*args: bytes):
            log_dict = {
                        'log_labels': self._labels,
                        'log_inputs': str(args),
                       }            
 
            if self._custom_inputs_fn:
                 custom_inputs = self._custom_inputs_fn(*args),
                 log_dict['log_custom_inputs'] = custom_inputs

            outputs = function(*args)

            log_dict['log_outputs'] = outputs

            if self._custom_outputs_fn:
                custom_outputs = self._custom_outputs_fn(outputs)
                log_dict['log_custom_outputs'] = custom_outputs

            self._logger.info(json.dumps(log_dict))

            return outputs 

        return wrapped_function
