# Python Predict Client

## Introduction

TensorFlow serving is the gRPC service for general TensorFlow models. We can implement the Python gRPC client to predict.

## Usage

```
./predict_client.py --host 127.0.0.1 --port 9000 --model_name cancer --model_version 1
```

For sparse data, you can run with this command.

```
./sparse_predict_client.py --host 127.0.0.1 --port 9000 --model_name sparse --model_version 1
```

You can use `cloudml` to predict with json file. Notice that `cloudml` is not public yet.

```
{
  "keys_dtype": "int32",
  "keys": [[1], [2]],
  "features_dtype": "float32",
  "features": [[1,2,3,4,5,6,7,8,9], [1,2,3,4,5,6,7,8,9]]
}
```

```
cloudml models predict cancer v1 -d ./data.json
```

## Development

The gPRC client relies on the generated Python files from Protobuf. You should not generate by `bazel build //tensorflow_serving/example:mnist_client` from TensorFlow serving's documents. Because it relies on bazel and you can not run without bazel.

We provide the proto files and script to generate the Python files in [./generate_python_files/](./generate_python_files/). The proto files are from [serving](https://github.com/tensorflow/serving/tree/master/tensorflow_serving/apis) and most source files are from [tensorflow](https://github.com/tensorflow/tensorflow/tree/master/tensorflow). We edit the import paths in `predict.proto` and `prediction_service.proto`. Notice that if the gRPC server upgrades, you need to update the source code and rebuild again.

```
cd ./generate_python_files/ && ./generate_python_files.sh
```
