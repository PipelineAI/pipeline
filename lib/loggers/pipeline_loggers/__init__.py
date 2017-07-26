from logging import Logger
from datetime import datetime
import json

__version__ = "0.6"

# TODO:  Handle batched inputs and outputs (using above custom fn's - match inputs to outputs!)
# TODO:  Add Monitors around these calls!!

class log_inputs_and_outputs(object):

    def __init__(self,
                 logger: Logger,
                 labels: dict={},
                 canonical_inputs_fn=hash,
                 canonical_outputs_fn=hash):

        self._timestamp = datetime.now().strftime("%s")
        self._logger = logger
        self._labels = labels
        self._canonical_inputs_fn = canonical_inputs_fn
        self._canonical_outputs_fn = canonical_outputs_fn


    def __call__(self, function):

        def wrapped_function(*args):
            outputs = function(*args)
            log_dict = {
                        "timestamp": self._timestamp, 
                        "labels": self._labels,
                        "raw_inputs": args,
                        "canonical_inputs": self._canonical_inputs_fn(args),
                        "raw_outputs": outputs,
                        "canonical_outputs": self._canonical_outputs_fn(outputs)
                       }
            self._logger.info(json.dumps(log_dict))
            return outputs

        return wrapped_function
