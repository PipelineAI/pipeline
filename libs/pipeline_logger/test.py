from pipeline_logger import log
import logging
import json
from pipeline_logger.kafka_handler import KafkaHandler
from pipeline_logger.tensorboard_handler import TensorboardHandler

_logger = logging.getLogger('test_logger')
_logger.setLevel(logging.INFO)
_logger_stream_handler = logging.StreamHandler()
_logger_stream_handler.setLevel(logging.INFO)
_logger.addHandler(_logger_stream_handler)

_logger_kafka_handler = KafkaHandler(host_list='localhost:9092', topic='prediction-inputs')
_logger.addHandler(_logger_kafka_handler)

_logger_tensorboard_handler = TensorboardHandler(logdir_path='/tmp/pipeline_logger.tensorboard_handler')
_logger.addHandler(_logger_tensorboard_handler)

@log(logger=_logger, 
     labels={'a_label_key': 'a_label_value'})
def test_log_inputs_and_outputs(arg1: int, arg2: int):
    return arg1 + arg2

test_log_inputs_and_outputs(4, 5)
test_log_inputs_and_outputs(4, 5)
test_log_inputs_and_outputs(5, 4)

def _custom_inputs_fn(*args):
    return hash('%s:%s' % (args[0], args[1]))

def _custom_outputs_fn(outputs):
    return hash(outputs)

@log(custom_inputs_fn=_custom_inputs_fn, 
     custom_outputs_fn=_custom_outputs_fn, 
     logger=_logger, 
     labels={'a_label_key': 'a_label_value'})
def test_log_inputs_and_outputs_custom_fn(arg1: int, arg2: int):
    return arg1 + arg2

test_log_inputs_and_outputs_custom_fn(4, 5)

def _custom_inputs_json_fn(*args):
    return hash(json.dumps(json.loads(args[0]), sort_keys=True))

@log(custom_inputs_fn=_custom_inputs_json_fn, 
     custom_outputs_fn=_custom_outputs_fn,
     logger=_logger, 
     labels={'a_label_key': 'a_label_value'})
def test_log_inputs_and_outputs_json_custom_fn(args: str):
    args_json = json.loads(args)
    arg1 = args_json['arg1']
    arg2 = args_json['arg2']
    return arg1 + arg2

test_log_inputs_and_outputs_json_custom_fn('{"arg1":4,"arg2":5}')
test_log_inputs_and_outputs_json_custom_fn('{"arg2":5,"arg1":4}')
