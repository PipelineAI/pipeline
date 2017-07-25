from logging import Logger
from datetime import datetime

__version__ = "0.5"

# TODO:  Allow custom inputs_log_fn and outputs_log_fn
# TODO:  Handle batched inputs and outputs (using above custom fn's - match inputs to outputs!)
# TODO:  Log in a json format (vs. custom :: delimited format)
# TODO:  Add Monitors around these calls!!

class log_inputs_and_outputs(object):

    def __init__(self,
                 logger: Logger,
                 labels: dict={},
                 hash_fn=hash):

        self._logger = logger
        self._labels = labels
        self._hash_fn = hash_fn      
        self._timestamp= datetime.now().strftime("%s")

    def __call__(self, function):

        def wrapped_function(*args):
            args_hash = self._hash_fn(args)
            outputs = function(*args)
            log = '%s::%s::%s::%s::%s' % (self._timestamp, self._labels, args, args_hash, outputs)
            self._logger.info(log)
            return outputs

        return wrapped_function
