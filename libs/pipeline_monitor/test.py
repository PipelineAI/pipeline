from pipeline_monitor import prometheus_monitor as monitor

_labels= {'a_label_key':'a_label_value'}

@monitor(labels=_labels, name="test_monitor")
def test_log_inputs_and_outputs(arg1: int, arg2: int):
    return arg1 + arg2

test_log_inputs_and_outputs(4, 5)
