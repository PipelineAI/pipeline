from pipeline_loggers import log_inputs_and_outputs
import logging

_logger = logging.getLogger('pipeline-logger')
_logger.setLevel(logging.INFO)

_logger_stream_handler = logging.StreamHandler()
_logger_stream_handler.setLevel(logging.INFO)

_logger.addHandler(_logger_stream_handler)

@log_inputs_and_outputs(logger=_logger, labels={'a_label_key': 'a_label_value'})
def test_log_inputs_and_outputs(arg1: int, arg2: int):
    return arg1 + arg2

test_log_inputs_and_outputs(4, 5)
test_log_inputs_and_outputs(4, 5)
test_log_inputs_and_outputs(5, 4)
