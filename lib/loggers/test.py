from pipeline_loggers import log_inputs_and_outputs
import logging
import json

_logger = logging.getLogger('pipeline-logger')
_logger.setLevel(logging.INFO)

_logger_stream_handler = logging.StreamHandler()
_logger_stream_handler.setLevel(logging.INFO)

_logger.addHandler(_logger_stream_handler)

@log_inputs_and_outputs(logger=_logger, 
                        labels={'a_label_key': 'a_label_value'})
def test_log_inputs_and_outputs(arg1: int, arg2: int):
    return arg1 + arg2

test_log_inputs_and_outputs(4, 5)
test_log_inputs_and_outputs(4, 5)
test_log_inputs_and_outputs(5, 4)

def _canonical_inputs_fn(*args):
    return hash(args[0][0])

def _canonical_outputs_fn(outputs):
    return hash(outputs)

@log_inputs_and_outputs(canonical_inputs_fn=_canonical_inputs_fn, 
                        canonical_outputs_fn=_canonical_outputs_fn, 
                        logger=_logger, 
                        labels={'a_label_key': 'a_label_value'})
def test_log_inputs_and_outputs_canonical_fn(arg1: int, arg2: int):
    return arg1 + arg2

test_log_inputs_and_outputs_canonical_fn(4, 5)

def _canonical_inputs_json_fn(*args):
    return hash(json.dumps(json.loads(args[0][0]), sort_keys=True))

@log_inputs_and_outputs(canonical_inputs_fn=_canonical_inputs_json_fn, 
                        canonical_outputs_fn=_canonical_outputs_fn,
                        logger=_logger, 
                        labels={'a_label_key': 'a_label_value'})
def test_log_inputs_and_outputs_json_canonical_fn(args: str):
    args_json = json.loads(args)
    arg1 = args_json['arg1']
    arg2 = args_json['arg2']
    return arg1 + arg2

test_log_inputs_and_outputs_json_canonical_fn('{"arg1":4, "arg2":5}')
test_log_inputs_and_outputs_json_canonical_fn('{"arg2":5, "arg1":4}')
