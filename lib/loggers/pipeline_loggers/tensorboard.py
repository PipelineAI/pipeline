# Details here:  https://github.com/TeamHG-Memex/tensorboard_logger/tree/0.0.4
from tensorboard_logger import configure, log_value, Logger

class TensorBoardHandler(logging.Handler):

    def __init__(self, logdir_path, flush_secs=5, **kwargs):
        logging.Handler.__init__(self)
        self.key = kwargs.get("key", None)
        self.tensorboard_logger = tensorboard_logger.Logger(logdir_path, flush_secs)

     
    def emit(self, record: float, **kwargs):
        step = kwargs.get("step", None)
        try:
            if not self.key:
                self.tensorboard_logger.log_value('default', record, step)
            else:
                self.tensorboard_logger.log_value(self.key, record, step)
        except (KeyboardInterrupt, SystemExit):
            raise
        except:
            self.handleError(record)


    def close(self):
        logging.Handler.close(self)
