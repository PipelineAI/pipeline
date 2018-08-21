![PipelineAI Logo](http://pipeline.ai/assets/img/logo/pipelineai-logo.png)

## Pull PipelineAI [Sample Models](https://github.com/PipelineAI/models)
```
git clone https://github.com/PipelineAI/models
```
**Change into the new `models/` directory**
```
cd models
```

# Install PipelineAI
## System Requirements
* 8GB
* 4 Cores

## Requirements
* Install [Docker](https://www.docker.com/community-edition#/download)
* Install Python 2 or 3 ([Conda](https://conda.io/docs/install/quick.html) is Preferred)
* Install (Windows Only) Install [PowerShell](https://github.com/PowerShell/PowerShell/tree/master/docs/installation) 

# Install [PipelineAI CLI](../README.md#install-pipelinecli)
* Click [**HERE**](../README.md#install-pipelinecli) to install the PipelineAI CLI

## Sync Latest Docker Images
```
pipeline env-registry-sync --tag=1.5.0
```
* For GPU images, add `--chip=gpu`

# Deploy Models
* [Deploy a TensorFlow Model](#deploy-a-tensorflow-model)
* [Deploy a Scikit-Learn Model](#deploy-a-scikit-learn-model)
* [Deploy a PyTorch Model](#deploy-a-pytorch-model)
* [Deploy an Xgboost Model](#deploy-an-xgboost-model)
* [Deploy an MXNet Model](#deploy-an-mxnet-model)

# Deploy a TensorFlow Model

## Inspect Model Directory
_Note:  This is relative to where you cloned the `models` repo [above](#clone-the-pipelineai-predict-repo)._
```
ls -l ./tensorflow/mnist-v1/model

### EXPECTED OUTPUT ###
...
pipeline_conda_environment.yaml    <-- Required. Sets up the conda environment
pipeline_condarc                   <-- Required, but Empty is OK.  Configure Conda proxy servers (.condarc)
pipeline_modelserver.properties    <-- Required, but Empty is OK.  Configure timeouts and fallbacks
pipeline_invoke_python.py          <-- Required if using Python Runtime (python)
pipeline_invoke_tflite.py          <-- Required if using TensorFlow Lite Runtime (tflite)
pipeline_invoke_tfserving.py       <-- Required if using TensorFlow Serving Runtime (tfserving)
pipeline_setup.sh                  <-- Required, but Empty is OK.  Init script performed upon Docker build.
pipeline_tfserving.properties      <-- Required by TensorFlow Serving. Custom request-batch sizes, etc.
pipeline_tfserving/                <-- Required by TensorFlow Serving. Contains the TF SavedModel
...
```
Inspect TensorFlow Serving Model 
```
ls -l ./tensorflow/mnist-v1/pipeline_tfserving/

### EXPECTED OUTPUT ###
...
0/  
1510612528/  <-- TensorFlow Serving finds the latest (highest) version 
...
```

## Build the Model into a Runnable Docker Image
* This command bundles the TensorFlow runtime with the model.

```
pipeline predict-server-build --model-name=mnist --model-tag=v1tensorflow --model-type=tensorflow --model-runtime=python --model-path=./tensorflow/mnist-v1/model
```

Notes:
* `--model-path` must be relative.
* Add `--http-proxy=...` and `--https-proxy=...` if you see `CondaHTTPError: HTTP 000 CONNECTION FAILED for url`
* If you have issues, see the comprehensive [**Troubleshooting**](docs/troubleshooting/README.md) section below.
* `--model-type`: **tensorflow**, **scikit**, **python**, **keras**, **spark**, **java**, **xgboost**, **pmml**, **caffe**
* `--model-runtime`: **jvm**, **tfserving**, **python**, **cpp**, **tensorrt**
* `--model-chip`: **cpu**, **gpu**, **tpu**
* For GPU-based models, make sure you specify `--model-chip=gpu`

## Start the Model Server
```
pipeline predict-server-start --model-name=mnist --model-tag=v1tensorflow --memory-limit=2G
```
Notes:
* Ignore `WARNING: Your kernel does not support swap limit capabilities or the cgroup is not mounted. Memory limited without swap.`
* If you see `port is already allocated` or `already in use by container`, you already have a container running.  List and remove any conflicting containers.  For example, `docker ps` and/or `docker rm -f train-mnist-v1`.
* You can change the port(s) by specifying the following: `--predict-port=8081`, `--prometheus-port=9091`, `--grafana-port=3001`.  
* If you change the ports, be sure to change the ports in the examples below to match your new ports.
* Also, your nginx and prometheus configs will need to be adjusted.
* In other words, try not to change the ports!
* For GPU-based models, make sure you specify `--start-cmd=nvidia-docker` - and make sure you have `nvidia-docker` installed!
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.

## Inspect `pipeline_invoke_python.py`
_Note:  Only the `invoke()` method is required.  Everything else is optional._
```
cat ./tensorflow/mnist-v1/model/pipeline_invoke_python.py

### EXPECTED OUTPUT ###

import os
import numpy as np
import json
import logging                                                 <== Optional.  Log to console, file, kafka
from pipeline_monitor import prometheus_monitor as monitor     <== Optional.  Monitor runtime metrics
from pipeline_logger import log

import tensorflow as tf
from tensorflow.contrib import predictor

_logger = logging.getLogger('pipeline-logger')
_logger.setLevel(logging.INFO)
_logger_stream_handler = logging.StreamHandler()
_logger_stream_handler.setLevel(logging.INFO)
_logger.addHandler(_logger_stream_handler)

__all__ = ['invoke']                                           <== Optional.  Being a good Python citizen.

_labels = {                                                    <== Optional.  Used for metrics/labels
           'name': 'mnist',
           'tag': 'v1tensorflow',
           'runtime': 'python',
           'chip': 'cpu',
          }

def _initialize_upon_import():                                 <== Optional.  Called once upon server startup
    ''' Initialize / Restore Model Object.
    '''
    saved_model_path = './pipeline_tfserving/0'
    return predictor.from_saved_model(saved_model_path)


# This is called unconditionally at *module import time*...
_model = _initialize_upon_import()


@log(labels=_labels, logger=_logger)                           <== Optional.  Sample and compare predictions
def invoke(request):                                           <== Required.  Called on every prediction
    '''Where the magic happens...'''

    with monitor(labels=_labels, name="transform_request"):    <== Optional.  Expose fine-grained metrics
        transformed_request = _transform_request(request)      <== Optional.  Transform input (json) into TensorFlow (tensor)

    with monitor(labels=_labels, name="invoke"):               <== Optional.  Calls _model.predict()
        response = _model(transformed_request)

    with monitor(labels=_labels, name="transform_response"):   <== Optional.  Transform TensorFlow (tensor) into output (json)
        transformed_response = _transform_response(response)

    return transformed_response                                <== Required.  Returns the predicted value(s)


def _transform_request(request):
    request_str = request.decode('utf-8')
    request_json = json.loads(request_str)
    request_np = ((255 - np.array(request_json['image'], dtype=np.uint8)) / 255.0).reshape(1, 28, 28)
    return {"image": request_np}


def _transform_response(response):
    return json.dumps({"classes": response['classes'].tolist(),
                       "probabilities": response['probabilities'].tolist(),
                      })

if __name__ == '__main__':
    with open('../input/predict/test_request.json', 'rb') as fb:
        request_bytes = fb.read()
        response_bytes = invoke(request_bytes)
        print(response_bytes)
```

## Monitor Runtime Logs
* Wait for the model runtime to settle...
```
pipeline predict-server-logs --model-name=mnist --model-tag=v1tensorflow

### EXPECTED OUTPUT ###
...
2017-10-10 03:56:00.695  INFO 121 --- [     run-main-0] i.p.predict.jvm.PredictionServiceMain$   : Started PredictionServiceMain. in 7.566 seconds (JVM running for 20.739)
[debug] 	Thread run-main-0 exited.
[debug] Waiting for thread container-0 to terminate.
```
Notes:
* You need to `Ctrl-C` out of the log viewing before proceeding.

## Predict in Any Language
* Use the REST API to POST a JSON document representing a number.
```
http://localhost:8080
```
![PipelineAI REST API](http://pipeline.ai/assets/img/api-embed-har-localhost.png)

![MNIST 8](http://pipeline.ai/assets/img/mnist-8-100x95.png)
```
curl -X POST -H "Content-Type: application/json" \
  -d '{"image": [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,10,150,238,254,255,237,150,150,225,161,221,203,18,0,0,0,0,0,0,0,0,0,0,0,0,0,0,66,146,253,253,253,253,253,253,253,253,253,253,253,173,0,0,0,0,0,0,0,0,0,0,0,0,0,140,251,253,253,178,114,114,114,114,114,114,167,253,253,154,0,0,0,0,0,0,0,0,0,0,0,12,84,240,253,248,170,28,0,0,0,0,0,0,90,253,250,90,0,0,0,0,0,0,0,0,0,0,10,129,226,253,235,128,0,0,0,0,0,0,0,8,188,253,190,0,0,0,0,0,0,0,0,0,0,0,56,250,253,246,98,0,0,0,0,0,0,0,0,76,243,234,100,0,0,0,0,0,0,0,0,0,0,0,185,253,248,44,0,0,0,0,0,0,0,0,34,245,253,95,0,0,0,0,0,0,0,0,0,0,0,0,69,187,87,0,0,0,0,0,0,0,0,22,164,253,223,63,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,55,247,253,85,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,47,230,253,184,10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,145,253,241,43,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,57,250,253,206,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,135,253,253,40,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,105,251,248,108,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,68,226,253,180,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,40,233,253,205,13,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,5,189,253,240,72,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,110,253,253,109,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,47,242,253,159,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,43,198,228,24,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]}' \
  http://localhost:8080 \
  -w "\n\n"

### Expected Output ###
{"variant": "mnist-v1scikit-scikit-python-cpu", "outputs":{"outputs": 5.0}}
 
### FORMATTED OUTPUT ###
Digit  Confidence
=====  ==========
0      0.00138249155133962
1      0.00036483019357547
2      0.00370581657625734
3      0.01074937824159860
4      0.00158193788956850
5      0.00006451825902331
6      0.00010775036207633
7      0.00010466964886290
8      0.98193389177322390   <-- Prediction
9      0.00000471303883387
```
Notes:
* Ignore the following warning: `WARNING: Your kernel does not support swap limit capabilities or the cgroup is not mounted. Memory limited without swap.`
* You may see `502 Bad Gateway` or `'{"results":["Fallback!"]}'` if you predict too quickly.  Let the server settle a bit - and try again.
* You will likely see `Fallback!` on the first successful invocation.  This is GOOD!  This means your timeouts are working.  Check out the `PIPELINE_MODEL_SERVER_TIMEOUT_MILLISECONDS` in `pipeline_modelserver.properties`.
* If you continue to see `Fallback!` even after a minute or two, you may need to increase the value of   `PIPELINE_MODEL_SERVER_TIMEOUT_MILLISECONDS` in `pipeline_modelserver.properties`.  (This is rare as the default is 5000 milliseconds, but it may happen.)
* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.  This usually happens when using Docker Quick Terminal on Windows 7.
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.

### PipelineCLI Predict
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline predict-server-test --endpoint-url=http://localhost:8080/invoke --test-request-path=./scikit/mnist/input/predict/test_request.json

### EXPECTED OUTPUT ###

'{"variant": "mnist-v1scikit-scikit-python-cpu", "outputs":[188.6431188435]}'
```

# Deploy a PyTorch Model
## View Prediction Code
```
cat ./pytorch/mnist-v1/model/pipeline_invoke_python.py
```

## Build the PyTorch Model Server
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline predict-server-build --model-name=mnist --model-tag=v1pytorch --model-type=pytorch --model-runtime=python --model-path=./pytorch/mnist-v1/model/ 
```
* For GPU-based models, make sure you specify `--model-chip=gpu` - and make sure you have `nvidia-docker` installed!

## Start the PyTorch Model Server
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline predict-server-start --model-name=mnist --model-tag=v1pytorch
```
* Ignore the following warning: `WARNING: Your kernel does not support swap limit capabilities or the cgroup is not mounted. Memory limited without swap.`
* For GPU-based models, make sure you specify `--start-cmd=nvidia-docker` - and make sure you have `nvidia-docker` installed!

## View the PyTorch Model Server Logs
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline predict-server-logs --model-name=mnist --model-tag=v1pytorch
```

# Deploy an Xgboost Model
## View Prediction Code
```
cat ./xgboost/mnist-v1/model/pipeline_invoke_python.py
```

## Build the Xgboost Model Server
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline predict-server-build --model-name=mnist --model-tag=v1xgboost --model-type=xgboost --model-runtime=python --model-path=./xgboost/mnist-v1/model/ 
```
* For GPU-based models, make sure you specify `--model-chip=gpu` - and make sure you have `nvidia-docker` installed!

## Start the Xgboost Model Server
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline predict-server-start --model-name=mnist --model-tag=v1xgboost
```
* Ignore the following warning: `WARNING: Your kernel does not support swap limit capabilities or the cgroup is not mounted. Memory limited without swap.`
* For GPU-based models, make sure you specify `--start-cmd=nvidia-docker` - and make sure you have `nvidia-docker` installed!

## View the Xgboost Model Server Logs
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline predict-server-logs --model-name=mnist --model-tag=v1xgboost
```

# Deploy an MXNet Model
## View Prediction Code
```
cat ./mxnet/mnist-v1/model/pipeline_invoke_python.py
```

## Build the MXNet Model Server
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline predict-server-build --model-name=mnist --model-tag=v1mxnet --model-type=mxnet --model-runtime=mxnet --model-path=./mxnet/mnist-v1/model/ 
```
* For GPU-based models, make sure you specify `--model-chip=gpu` - and make sure you have `nvidia-docker` installed!

## Start the MXNet Model Server
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline predict-server-start --model-name=mnist --model-tag=v1mxnet
```
* Ignore the following warning: `WARNING: Your kernel does not support swap limit capabilities or the cgroup is not mounted. Memory limited without swap.`
* For GPU-based models, make sure you specify `--start-cmd=nvidia-docker` - and make sure you have `nvidia-docker` installed!

## View the MXNet Model Server Logs
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline predict-server-logs --model-name=mnist --model-tag=v1mxnet
```

## Model Predict
### Predict with REST API
```
curl -X POST -H "Content-Type: application/json" \
  -d '{"image": [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,10,150,238,254,255,237,150,150,225,161,221,203,18,0,0,0,0,0,0,0,0,0,0,0,0,0,0,66,146,253,253,253,253,253,253,253,253,253,253,253,173,0,0,0,0,0,0,0,0,0,0,0,0,0,140,251,253,253,178,114,114,114,114,114,114,167,253,253,154,0,0,0,0,0,0,0,0,0,0,0,12,84,240,253,248,170,28,0,0,0,0,0,0,90,253,250,90,0,0,0,0,0,0,0,0,0,0,10,129,226,253,235,128,0,0,0,0,0,0,0,8,188,253,190,0,0,0,0,0,0,0,0,0,0,0,56,250,253,246,98,0,0,0,0,0,0,0,0,76,243,234,100,0,0,0,0,0,0,0,0,0,0,0,185,253,248,44,0,0,0,0,0,0,0,0,34,245,253,95,0,0,0,0,0,0,0,0,0,0,0,0,69,187,87,0,0,0,0,0,0,0,0,22,164,253,223,63,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,55,247,253,85,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,47,230,253,184,10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,145,253,241,43,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,57,250,253,206,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,135,253,253,40,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,105,251,248,108,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,68,226,253,180,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,40,233,253,205,13,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,5,189,253,240,72,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,110,253,253,109,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,47,242,253,159,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,43,198,228,24,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]}' \
  http://localhost:8080 \
  -w "\n\n"
  
### EXPECTED OUTPUT ###

'{"variant": "mnist-v1pytorch-pytorch-python-cpu", ...}'
```
Notes:
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
* You may see `502 Bad Gateway` or `'{"results":["Fallback!"]}'` if you predict too quickly.  Let the server settle a bit - and try again.
* You will likely see `Fallback!` on the first successful invocation.  This is GOOD!  This means your timeouts are working.  Check out the `PIPELINE_MODEL_SERVER_TIMEOUT_MILLISECONDS` in `pipeline_modelserver.properties`.
* If you continue to see `Fallback!` even after a minute or two, you may need to increase the value of   `PIPELINE_MODEL_SERVER_TIMEOUT_MILLISECONDS` in `pipeline_modelserver.properties`.  (This is rare as the default is 5000 milliseconds, but it may happen.)
* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.  This usually happens when using Docker Quick Terminal on Windows 7.
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.

### Predict with CLI
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline predict-server-test --endpoint-url=http://localhost:8080/invoke --test-request-path=./pytorch/mnist-v1/input/predict/test_request.json

### EXPECTED OUTPUT ###

'{"variant": "mnist-v1pytorch-pytorch-python-cpu", ...}'
```

## Train Models with PipelineAI
* Click [HERE](README-training.md) to Train Models

## PipelineAI Quick Start (CPU, GPU, and TPU)
Train and Deploy your ML and AI Models in the Following Environments:
* [Hosted Community Edition](/docs/quickstart/community)
* [Docker](/docs/quickstart/docker)
* [Kubernetes](/docs/quickstart/kubernetes)
* [AWS SageMaker](/docs/quickstart/sagemaker)
