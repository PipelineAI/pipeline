from logging import Logger
from datetime import datetime

__version__ = "0.2"

class log_inputs_and_outputs(object):

    def __init__(self,
                 logger: Logger,
                 labels: dict={}):

        self._logger = logger
        self._labels = labels       
        self._timestamp= datetime.now().strftime("%s")

    def __call__(self, function):

        def wrapped_function(*args):
            args_hash = hash(args)

            outputs = function(*args)

            log = '%s::%s::%s::%s::%s' % (self._timestamp, self._labels, args, args_hash, outputs)

            self._logger.info(log)

        return wrapped_function
