import numpy
import tensorflow as tf
import threading

from grpc.beta import implementations

from tensorflow_serving.apis import predict_pb2
from tensorflow_serving.apis import prediction_service_pb2

def _create_rpc_callback():
  """Creates RPC callback function.

  Args:
    label: The correct label for the predicted example.
    result_counter: Counter for the prediction result.
  Returns:
    The callback function.
  """
  def _callback(result_future):
    """Callback function.

    Calculates the statistics for the prediction result.

    Args:
      result_future: Result future of the RPC.
    """
    exception = result_future.exception()
    if exception:
      print(exception)
    else:
      sys.stdout.write('.')
      sys.stdout.flush()

      response = numpy.array(result_future.result().outputs['y_pred'].float_val)
      print('\n%s\n', % str(response))
      
      prediction = response
  return _callback

if __name__ == "__main__":
  host = "localhost"
  port = 9000

  channel = implementations.insecure_channel(host, int(port))
  stub = prediction_service_pb2.beta_create_PredictionService_stub(channel)
  num_tests = 1
  for _ in range(num_tests):
    request = predict_pb2.PredictRequest()

    request.model_spec.name = 'linear_cpu'
    request.model_spec.signature_name = 'predict'

    request.inputs['x_observed'].CopyFrom(
        tf.contrib.util.make_tensor_proto(values=[1.5], dtype=float))

    result_future = stub.Predict.future(request, 5.0)  # 5 seconds

    result_future.add_done_callback(_create_rpc_callback())


