from pipeline_model import TensorFlowServingModel
from tensorflow.core.framework import tensor_shape_pb2, tensor_pb2, types_pb2
from tensorflow_serving.apis import predict_pb2, prediction_service_pb2

request = predict_pb2.PredictRequest()
request.model_spec.name = 'blah'
request.model_spec.signature_name = 'blah'
print(request)

# create TensorProto object for a request
dims = [tensor_shape_pb2.TensorShapeProto.Dim(size=1)]
print(dims)

tensor_shape_proto = tensor_shape_pb2.TensorShapeProto(dim=dims)
print(tensor_shape_proto)

tensor_proto = tensor_pb2.TensorProto(
  dtype=types_pb2.DT_STRING,
  tensor_shape=tensor_shape_proto,
  string_val=[b'blah'])
print(tensor_proto)

# put data into TensorProto and copy them into the request object
request.inputs['blah'].CopyFrom(tensor_proto)
print(request)

# call prediction
#result = stub.Predict(request, 60.0)  # 60 secs timeout
