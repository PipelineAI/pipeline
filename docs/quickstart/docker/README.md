![PipelineAI Logo](http://pipeline.ai/assets/img/logo/pipelineai-split-black-258x62.png)

# Pre-requisites
## Install Tools
* [Docker](https://www.docker.com/community-edition#/download)
* Python 2 or 3 ([Conda](https://conda.io/docs/install/quick.html) is Preferred)
* (Windows Only) [PowerShell](https://github.com/PowerShell/PowerShell/tree/master/docs/installation) 

## Install PipelineAI CLI
``` f
pip install cli-pipeline==1.5.65 --ignore-installed --no-cache -U
```
Notes: 
* This command line interface requires **Python 2 or 3** and **Docker** as detailed above in the Pre-Requisites section.
* You may need to specify `--user` if you have issues.
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.

### Verify Successful PipelineAI CLI Installation
```
pipeline version

### EXPECTED OUTPUT ###
cli_version: 1.5.x    <-- MAKE SURE THIS MATCHES THE VERSION YOU INSTALLED ABOVE

default train base image: docker.io/pipelineai/train-cpu:1.5.0     
default predict base image: docker.io/pipelineai/predict-cpu:1.5.0 

capabilities_enabled: ['train-server', 'train-kube', 'train-sage', 'predict-server', 'predict-kube', 'predict-sage', 'predict-kafka']
capabilities_available: ['jupyter-server', 'jupyter-kube', 'jupyter-sage', 'spark', 'airflow', 'kafka']

Email upgrade@pipeline.ai to enable the advanced capabilities.
```

### PipelineAI CLI Overview
```
pipeline help

### EXPECTED OUTPUT ###
...
help                        <-- This List of CLI Commands

predict-http-test           <-- Test Model Cluster (Http Endpoint)

predict-kafka-consume       <-- Consume Kafka Predictions
predict-kafka-describe      <-- Describe Kafka Prediction Cluster
predict-kafka-start         <-- Start Kafka Prediction Cluster
predict-kafka-test          <-- Predict with Kafka-based Model Endpoint 

predict-kube-autoscale      <-- Configure AutoScaling for Model Cluster
predict-kube-connect        <-- Create Secure Tunnel to Model Cluster 
predict-kube-describe       <-- Describe Model Cluster (Raw)
predict-kube-endpoint       <-- Retrieve Model Cluster Endpoint 
predict-kube-endpoints      <-- Retrieve All Model Cluster Endpoints
predict-kube-logs           <-- View Model Cluster Logs 
predict-kube-route          <-- Route Live Traffic  
predict-kube-scale          <-- Scale Model Cluster
predict-kube-shell          <-- Shell into Model Cluster
predict-kube-start          <-- Start Model Cluster from Docker Registry
predict-kube-stop           <-- Stop Model Cluster
predict-kube-test           <-- Test Model Cluster

predict-sage-describe       <-- Describe Model Cluster (SageMaker)
predict-sage-route          <-- Route Live Traffic (SageMaker)
predict-sage-start          <-- Start Model Cluster (SageMaker)
predict-sage-stop           <-- Stop Model Cluster (SageMaker)
predict-sage-test           <-- Test Model Cluster (SageMaker)

predict-server-build        <-- Build Model Server
predict-server-logs         <-- View Model Server Logs
predict-server-pull         <-- Pull Model Server from Docker Registry
predict-server-push         <-- Push Model Server to Docker Registry
predict-server-shell        <-- Shell into Model Server (Debugging)
predict-server-start        <-- Start Model Server
predict-server-stop         <-- Stop Model Server
predict-server-test         <-- Test Model Server

train-kube-connect          <-- Create Secure Tunnel to Training Cluster
train-kube-describe         <-- Describe Training Cluster
train-kube-logs             <-- View Training Cluster Logs
train-kube-scale            <-- Scale Training Cluster
train-kube-shell            <-- Shell into Training Cluster
train-kube-start            <-- Start Training Cluster from Docker Registry
train-kube-stop             <-- Stop Training Cluster

train-server-build          <-- Build Training Server
train-server-logs           <-- View Training Server Logs
train-server-pull           <-- Pull Training Server from Docker Registry
train-server-push           <-- Push Training Server to Docker Registry
train-server-shell          <-- Shell into Training Server (Debugging)
train-server-start          <-- Start Training Server
train-server-stop           <-- Stop Training Server

version                     <-- View This CLI Version
```

# Retrieve Sample PipelineAI Models
## Clone the PipelineAI Predict Repo
```
git clone https://github.com/PipelineAI/models
```

## Change into `models` Directory
```
cd ./models
```

# Train and Deploy Models
* [Train a TensorFlow Model](#train-a-tensorflow-model)
* [Deploy a TensorFlow Model](#deploy-a-tensorflow-model)
* [Train a Scikit-Learn Model](#train-a-scikit-learn-model)
* [Deploy a Scikit-Learn Model](#deploy-a-scikit-learn-model)
* [Train a PyTorch Model](#train-a-pytorch-model)
* [Deploy a PyTorch Model](#deploy-a-pytorch-model)


# Train a TensorFlow Model
## Inspect Model Directory
```
ls -l ./tensorflow/census-cpu/model

### EXPECTED OUTPUT ###
...
pipeline_conda_environment.yml     <-- Required. Sets up the conda environment
pipeline_condarc                   <-- Required, but Empty is OK. Configure Conda proxy servers (.condarc)
pipeline_setup.sh                  <-- Required, but Empty is OK.  Init script performed upon Docker build
pipeline_train.py                  <-- Required. `main()` is required. Pass args with `--train-args`
...
```

## Build Training Server
```
pipeline train-server-build --model-name=census --model-tag=cpu --model-type=tensorflow --model-path=./tensorflow/census-cpu/model
```
Notes:  
* `--model-path` must be relative.  
* Add `--http-proxy=...` and `--https-proxy=...` if you see `CondaHTTPError: HTTP 000 CONNECTION FAILED for url`
* If you have issues, see the comprehensive [**Troubleshooting**](/docs/troubleshooting/README.md) section below.

## Start Training Server
```
pipeline train-server-start --model-name=census --model-tag=cpu --input-path=./tensorflow/census/input --output-path=./tensorflow/census-cpu/output --train-args="--train-files=training/adult.training.csv\ --eval-files=validation/adult.validation.csv\ --num-epochs=2\ --learning-rate=0.025"
```
Notes:
* `--train-args` is a single argument passed into the `pipeline_train.py`.  Therefore, you must escape spaces (`\ `) between arguments. 
* `--input-path` and `--output-path` are relative to the current working directory (outside the Docker container) and will be mapped as directories inside the Docker container from `/root`.
* `--train-files` and `--eval-files` are relative to `--input-path` inside the Docker container.
* Models, logs, and event are written to `--output-path` (or a subdirectory within).  These will be available outside of the Docker container.
* To prevent overwriting the output of a previous run, you should either 1) change the `--output-path` between calls or 2) create a new unique subfolder with `--output-path` in your `pipeline_train.py` (ie. timestamp).  See examples below.
* On Windows, be sure to use the forward slash `\` for `--input-path` and `--output-path` (not the args inside of `--train-args`).
* If you see `port is already allocated` or `already in use by container`, you already have a container running.  List and remove any conflicting containers.  For example, `docker ps` and/or `docker rm -f train-census-cpu-tensorflow-tfserving-cpu`.
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.

(_We are working on making this more intuitive._)

## View Training Logs
```
pipeline train-server-logs --model-name=census --model-tag=cpu
```

_Press `Ctrl-C` to exit out of the logs._

## View Trained Model Output (Locally)
_Make sure you pressed `Ctrl-C` to exit out of the logs._
```
ls -l ./tensorflow/census-cpu/output/

### EXPECTED OUTPUT ###
...
1511367633/  <-- Sub-directories of training output
1511367765/
...
```
_Multiple training runs will produce multiple subdirectories - each with a different timestamp._

## View Training UI (including TensorBoard for TensorFlow Models)
* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.
* This usually happens when using Docker Quick Terminal on Windows 7.
```
http://localhost:6006
```
![PipelineAI TensorBoard UI 0](http://pipeline.ai/assets/img/pipelineai-train-census-tensorboard-0.png)

![PipelineAI TensorBoard UI 1](http://pipeline.ai/assets/img/pipelineai-train-census-tensorboard-1.png)

![PipelineAI TensorBoard UI 2](http://pipeline.ai/assets/img/pipelineai-train-census-tensorboard-2.png)

![PipelineAI TensorBoard UI 3](http://pipeline.ai/assets/img/pipelineai-train-census-tensorboard-3.png)

## Stop Training Server
```
pipeline train-server-stop --model-name=census --model-tag=cpu
```

# Deploy a TensorFlow Model

## Inspect Model Directory
_Note:  This is relative to where you cloned the `models` repo [above](#clone-the-pipelineai-predict-repo)._
```
ls -l ./tensorflow/mnist-0.025/model

### EXPECTED OUTPUT ###
...
pipeline_conda_environment.yml     <-- Required. Sets up the conda environment
pipeline_condarc                   <-- Required, but Empty is OK.  Configure Conda proxy servers (.condarc)
pipeline_modelserver.properties    <-- Required, but Empty is OK.  Configure timeouts and fallbacks
pipeline_predict.py                <-- Required. `predict(request: bytes) -> bytes` is required
pipeline_setup.sh                  <-- Required, but Empty is OK.  Init script performed upon Docker build
pipeline_tfserving/                <-- Optional. Only TensorFlow Serving requires this directory
...
```
Inspect TensorFlow Serving Model 
```
ls -l ./tensorflow/mnist-0.025/pipeline_tfserving/

### EXPECTED OUTPUT ###
...
pipeline_tfserving.config  <-- Required by TensorFlow Serving. Custom request-batch sizes, etc.
1510612525/  
1510612528/                <-- TensorFlow Serving finds the latest (highest) version 
...
```

## Build the Model into a Runnable Docker Image
This command bundles the TensorFlow runtime with the model.
```
pipeline predict-server-build --model-name=mnist --model-tag=025 --model-type=tensorflow --model-path=./tensorflow/mnist-0.025/model
```
Notes:
* `--model-path` must be relative.
* Add `--http-proxy=...` and `--https-proxy=...` if you see `CondaHTTPError: HTTP 000 CONNECTION FAILED for url`
* If you have issues, see the comprehensive [**Troubleshooting**](docs/troubleshooting/README.md) section below.

* `--model-type`: **tensorflow**, **scikit**, **python**, **keras**, **spark**, **java**, **xgboost**, **pmml**
* `--model-runtime`: **jvm** (default for `--model-type==java|spark|xgboost|pmml`, **tfserving** (default for `--model-type==tensorflow`), **python** (default for `--model-type==scikit|python|keras`), **tensorrt** (only for Nvidia GPUs)
* `--model-chip`: **cpu** (default), **gpu**, **tpu**

## Start the Model Server
```
pipeline predict-server-start --model-name=mnist --model-tag=025 --memory-limit=2G
```
Notes:
* Ignore the following warning:  `WARNING: Your kernel does not support swap limit capabilities or the cgroup is not mounted. Memory limited without swap.`
* If you see `port is already allocated` or `already in use by container`, you already have a container running.  List and remove any conflicting containers.  For example, `docker ps` and/or `docker rm -f train-tfserving-tensorflow-mnist-025`.
* You can change the port(s) by specifying the following: `--predict-port=8081`, `--prometheus-port=9001`, `--grafana-port=3001`.  (Be sure to change the ports in the examples below to match your new ports.)
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.

## Inspect `pipeline_predict.py`
_Note:  Only the `predict()` method is required.  Everything else is optional._
```
cat ./tensorflow/mnist-0.025/model/pipeline_predict.py

### EXPECTED OUTPUT ###
import os
import logging
from pipeline_model import TensorFlowServingModel             <-- Optional.  Wraps TensorFlow Serving
from pipeline_monitor import prometheus_monitor as monitor    <-- Optional.  Monitor runtime metrics
from pipeline_logger import log                               <-- Optional.  Log to console, file, kafka

...

__all__ = ['predict']                                         <-- Optional.  Being a good Python citizen.

...

def _initialize_upon_import() -> TensorFlowServingModel:      <-- Optional.  Called once at server startup
    return TensorFlowServingModel(host='localhost',           <-- Optional.  Wraps TensorFlow Serving
                                  port=9000,
                                  model_name=os.environ['PIPELINE_MODEL_NAME'],
                                  timeout=100)                <-- Optional.  TensorFlow Serving timeout

_model = _initialize_upon_import()                            <-- Optional.  Called once upon server startup

_labels = {'model_runtime': os.environ['PIPELINE_MODEL_RUNTIME'],  <-- Optional.  Tag metrics
           'model_type': os.environ['PIPELINE_MODEL_TYPE'],   
           'model_name': os.environ['PIPELINE_MODEL_NAME'],
           'model_tag': os.environ['PIPELINE_MODEL_TAG']}

_logger = logging.getLogger('predict-logger')                 <-- Optional.  Standard Python logging

@log(labels=_labels, logger=_logger)                          <-- Optional.  Sample and compare predictions
def predict(request: bytes) -> bytes:                         <-- Required.  Called on every prediction

    with monitor(labels=_labels, name="transform_request"):   <-- Optional.  Expose fine-grained metrics
        transformed_request = _transform_request(request)     <-- Optional.  Transform input (json) into TensorFlow (tensor)

    with monitor(labels=_labels, name="predict"):
        predictions = _model.predict(transformed_request)       <-- Optional.  Calls _model.predict()

    with monitor(labels=_labels, name="transform_response"):
        transformed_response = _transform_response(predictions) <-- Optional.  Transform TensorFlow (tensor) into output (json)

    return transformed_response                                 <-- Required.  Returns the predicted value(s)
...
```

## Monitor Runtime Logs
Wait for the model runtime to settle...
```
pipeline predict-server-logs --model-name=mnist --model-tag=025

### EXPECTED OUTPUT ###
...
2017-10-10 03:56:00.695  INFO 121 --- [     run-main-0] i.p.predict.jvm.PredictionServiceMain$   : Started PredictionServiceMain. in 7.566 seconds (JVM running for 20.739)
[debug] 	Thread run-main-0 exited.
[debug] Waiting for thread container-0 to terminate.
...
INFO[0050] Completed initial partial maintenance sweep through 4 in-memory fingerprints in 40.002264633s.  source="storage.go:1398"
...
```
Notes:
* You need to `Ctrl-C` out of the log viewing before proceeding.

## Perform Prediction
_Before proceeding, make sure you hit `Ctrl-C` after viewing the logs in the previous step._
```
pipeline predict-server-test --endpoint-url=http://localhost:8080 --test-request-path=./tensorflow/mnist-0.025/input/predict/test_request.json

### EXPECTED OUTPUT ###
...
('{"variant": "mnist-025-tensorflow-tfserving-cpu", "outputs":{"outputs": '
 '[0.11128007620573044, 1.4478533557849005e-05, 0.43401220440864563, '
 '0.06995827704668045, 0.0028081508353352547, 0.27867695689201355, '
 '0.017851119861006737, 0.006651509087532759, 0.07679300010204315, '
 '0.001954273320734501]}}')
 ...

### FORMATTED OUTPUT ###
Digit  Confidence
=====  ==========
0      0.11128007620573044
1      0.00001447853355784
2      0.43401220440864563      <-- Prediction
3      0.06995827704668045
4      0.00280815083533525
5      0.27867695689201355 
6      0.01785111986100673
7      0.00665150908753275
8      0.07679300010204315
9      0.00195427332073450
```
Notes:
* You may see `502 Bad Gateway` or `'{"results":["Fallback!"]}'` if you predict too quickly.  Let the server settle a bit - and try again.
* You will likely see `Fallback!` on the first successful invocation.  This is GOOD!  This means your timeouts are working.  Check out the `PIPELINE_MODEL_SERVER_TIMEOUT_MILLISECONDS` in `pipeline_modelserver.properties`.
* If you continue to see `Fallback!` even after a minute or two, you may need to increase the value of   `PIPELINE_MODEL_SERVER_TIMEOUT_MILLISECONDS` in `pipeline_modelserver.properties`.  (This is rare as the default is 5000 milliseconds, but it may happen.)
* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.  This usually happens when using Docker Quick Terminal on Windows 7.
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.

## Perform 100 Predictions in Parallel (Mini Load Test)
```
pipeline predict-server-test --endpoint-url=http://localhost:8080 --test-request-path=./tensorflow/mnist-0.025/input/predict/test_request.json --test-request-concurrency=100
```
Notes:
* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.  This usually happens when using Docker Quick Terminal on Windows 7.

## Predict with REST API
Use the REST API to POST a JSON document representing the number 2.

![MNIST 2](http://pipeline.ai/assets/img/mnist-2-100x101.png)
```
curl -X POST -H "Content-Type: application/json" \
  -d '{"image": [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.05098039656877518, 0.529411792755127, 0.3960784673690796, 0.572549045085907, 0.572549045085907, 0.847058892250061, 0.8156863451004028, 0.9960784912109375, 1.0, 1.0, 0.9960784912109375, 0.5960784554481506, 0.027450982481241226, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.7882353663444519, 0.11764706671237946, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.988235354423523, 0.7921569347381592, 0.9450981020927429, 0.545098066329956, 0.21568629145622253, 0.3450980484485626, 0.45098042488098145, 0.125490203499794, 0.125490203499794, 0.03921568766236305, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.803921639919281, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6352941393852234, 0.9921569228172302, 0.803921639919281, 0.24705883860588074, 0.3490196168422699, 0.6509804129600525, 0.32156863808631897, 0.32156863808631897, 0.1098039299249649, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.007843137718737125, 0.7529412508010864, 0.9921569228172302, 0.9725490808486938, 0.9686275124549866, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.8274510502815247, 0.29019609093666077, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.2549019753932953, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.847058892250061, 0.027450982481241226, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5921568870544434, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.7333333492279053, 0.44705885648727417, 0.23137256503105164, 0.23137256503105164, 0.4784314036369324, 0.9921569228172302, 0.9921569228172302, 0.03921568766236305, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5568627715110779, 0.9568628072738647, 0.7098039388656616, 0.08235294371843338, 0.019607843831181526, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.43137258291244507, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.15294118225574493, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1882353127002716, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6705882549285889, 0.9921569228172302, 0.9921569228172302, 0.12156863510608673, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.2392157018184662, 0.9647059440612793, 0.9921569228172302, 0.6274510025978088, 0.003921568859368563, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08235294371843338, 0.44705885648727417, 0.16470588743686676, 0.0, 0.0, 0.2549019753932953, 0.9294118285179138, 0.9921569228172302, 0.9333333969116211, 0.27450981736183167, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.4941176772117615, 0.9529412388801575, 0.0, 0.0, 0.5803921818733215, 0.9333333969116211, 0.9921569228172302, 0.9921569228172302, 0.4078431725502014, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.7411764860153198, 0.9764706492424011, 0.5529412031173706, 0.8784314393997192, 0.9921569228172302, 0.9921569228172302, 0.9490196704864502, 0.43529415130615234, 0.007843137718737125, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6235294342041016, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9764706492424011, 0.6274510025978088, 0.1882353127002716, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.18431372940540314, 0.5882353186607361, 0.729411780834198, 0.5686274766921997, 0.3529411852359772, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]}' \
  http://localhost:8080 \
  -w "\n\n"

### Expected Output ###
{"variant": "mnist-025-tensorflow-tfserving-cpu", "outputs":{"outputs": [0.11128007620573044, 1.4478533557849005e-05, 0.43401220440864563, 0.06995827704668045, 0.0028081508353352547, 0.27867695689201355, 0.017851119861006737, 0.006651509087532759, 0.07679300010204315, 0.001954273320734501]}}

### FORMATTED OUTPUT ###
Digit  Confidence
=====  ==========
0      0.11128007620573044
1      0.00001447853355784
2      0.43401220440864563      <-- Prediction
3      0.06995827704668045
4      0.00280815083533525
5      0.27867695689201355 
6      0.01785111986100673
7      0.00665150908753275
8      0.07679300010204315
9      0.00195427332073450
```
Notes:
* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.  This usually happens when using Docker Quick Terminal on Windows 7.

## Monitor Real-Time Prediction Metrics
Re-run the Prediction REST API while watching the following dashboard URL:
```
http://localhost:8080/dashboard/monitor/monitor.html?streams=%5B%7B%22name%22%3A%22%22%2C%22stream%22%3A%22http%3A%2F%2Flocalhost%3A8080%2Fdashboard.stream%22%2C%22auth%22%3A%22%22%2C%22delay%22%3A%22%22%7D%5D
```
Notes:
* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.  This usually happens when using Docker Quick Terminal on Windows 7.

![Real-Time Throughput and Response Time](http://pipeline.ai/assets/img/hystrix-mini.png)

# Monitor Model Prediction Metrics
Re-run the Prediction REST API while watching the following detailed metrics dashboard URL.
```
http://localhost:3000/
```
Notes:
* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.  This usually happens when using Docker Quick Terminal on Windows 7.

![Prediction Dashboard](http://pipeline.ai/assets/img/request-metrics-breakdown.png)

_Username/Password: **admin**/**admin**_

_Set `Type` to `Prometheues`._

_Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.  This usually happens when using Docker Quick Terminal on Windows 7._

_Set `Url` to `http://localhost:9090`._

_Set `Access` to `direct`._

_Click `Save & Test`_.

_Click `Dashboards -> Import` upper-left menu drop-down_.

_Copy and Paste [THIS](https://github.com/PipelineAI/pipeline/blob/master/docs/dashboard/grafana/pipelineai-predict.json) raw json file into the `paste JSON` box_.

_Select the Prometheus-based data source that you setup above and click `Import`_.

_Change the Date Range in the upper right to `Last 5m` and the Refresh Every to `5s`._

_Create additional PipelineAI Prediction widgets using [THIS](https://prometheus.io/docs/practices/histograms/#count-and-sum-of-observations) guide to the Prometheus Syntax._

# Stop Model Server
```
pipeline predict-server-stop --model-name=mnist --model-tag=025
```

# Train a Scikit-Learn Model
## Inspect Model Directory
```
ls -l ./scikit/linear/model

### EXPECTED OUTPUT ###
...
pipeline_conda_environment.yml     <-- Required. Sets up the conda environment
pipeline_condarc                   <-- Required, but Empty is OK. Configure Conda proxy servers (.condarc)
pipeline_setup.sh                  <-- Required, but Empty is OK.  Init script performed upon Docker build
pipeline_train.py                  <-- Required. `main()` is required. Pass args with `--train-args`
...
```

## Build Training Server
```
pipeline train-server-build --model-name=linear --model-tag=cpu --model-type=tensorflow --model-path=./scikit/linear/model
```
Notes:  
* `--model-path` must be relative.  
* Add `--http-proxy=...` and `--https-proxy=...` if you see `CondaHTTPError: HTTP 000 CONNECTION FAILED for url`
* If you have issues, see the comprehensive [**Troubleshooting**](/docs/troubleshooting/README.md) section below.

## Start Training Server
```
pipeline train-server-start --model-name=linear --model-tag=cpu
```
Notes:
* `--train-args` is a single argument passed into the `pipeline_train.py`.  Therefore, you must escape spaces (`\ `) between arguments. 
* `--input-path` and `--output-path` are relative to the current working directory (outside the Docker container) and will be mapped as directories inside the Docker container from `/root`.
* `--train-files` and `--eval-files` are relative to `--input-path` inside the Docker container.
* Models, logs, and event are written to `--output-path` (or a subdirectory within).  These will be available outside of the Docker container.
* To prevent overwriting the output of a previous run, you should either 1) change the `--output-path` between calls or 2) create a new unique subfolder with `--output-path` in your `pipeline_train.py` (ie. timestamp).  See examples below.
* On Windows, be sure to use the forward slash `\` for `--input-path` and `--output-path` (not the args inside of `--train-args`).
* If you see `port is already allocated` or `already in use by container`, you already have a container running.  List and remove any conflicting containers.  For example, `docker ps` and/or `docker rm -f train-linear-cpu-scikit-python-cpu`.
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.

(_We are working on making this more intuitive._)

## View Training Logs
```
pipeline train-server-logs --model-name=linear --model-tag=cpu
```

_Press `Ctrl-C` to exit out of the logs._

## View Trained Model Output (Locally)
_Make sure you pressed `Ctrl-C` to exit out of the logs._
```
ls -l ./scikit/linear/model/

### EXPECTED OUTPUT ###
...
model.pkl   <-- Pickled Model File
...
```
_Multiple training runs will produce multiple subdirectories - each with a different timestamp._

## View Training UI (including TensorBoard for TensorFlow Models)
* TODO:  Add PipelineAI `tensorboard_logger` training-side metrics to scikit linear demo
* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.
* This usually happens when using Docker Quick Terminal on Windows 7.
```
http://localhost:6006
```

# Deploy a Scikit-Learn Model
The following model server uses a pickled scikit-learn model file.

## View Training Code
Click [HERE](https://github.com/PipelineAI/models/tree/90ab808f0135e61af3e3ab14a5f3f4293f69e601/scikit/linear) to see the Scikit-Learn Model.
```
ls -l ./scikit/linear/model/
```
```
cat ./scikit/linear/model/pipeline_train.py
```

## Build the Scikit-Learn Training Server
```
pipeline train-server-build --model-name=linear --model-tag=cpu --model-type=scikit --model-path=./scikit/linear/model/
```

## Start the Training Server
```
pipeline train-server-start --model-name=linear --model-tag=cpu --output-path=./scikit/linear/model/
```

## View the Training Logs
```
pipeline train-server-logs --model-name=linear --model-tag=cpu

### EXPECTED OUTPUT ###

Pickled model to "/opt/ml/output/model.pkl"   <-- This docker-internal path maps to --output-path above
```

## Build the Scikit-Learn Model Server
```
pipeline predict-server-build --model-name=linear --model-tag=cpu --model-type=scikit --model-path=./scikit/linear/model/
```

## Start the Model Server
```
pipeline predict-server-start --model-name=linear --model-tag=cpu
```

## View the Model Server Logs
```
pipeline predict-server-logs --model-name=linear --model-tag=cpu
```

## Predict with the Model 
### Curl Predict
```
curl -X POST -H "Content-Type: application/json" \
  -d '{"feature0": 0.03807590643342410180}' \
  http://localhost:8080  \
  -w "\n\n"

### Expected Output ###

{"variant": "linear-a-scikit-python-cpu", "outputs":[188.6431188435]}
```

### PipelineCLI Predict
```
pipeline predict-server-test --endpoint-url=http://localhost:8080/invocations --test-request-path=./scikit/linear/input/predict/test_request.json

### EXPECTED OUTPUT ###

'{"variant": "linear-a-scikit-python-cpu", "outputs":[188.6431188435]}'
```
Notes:
* You may see `502 Bad Gateway` or `'{"results":["Fallback!"]}'` if you predict too quickly.  Let the server settle a bit - and try again.
* You will likely see `Fallback!` on the first successful invocation.  This is GOOD!  This means your timeouts are working.  Check out the `PIPELINE_MODEL_SERVER_TIMEOUT_MILLISECONDS` in `pipeline_modelserver.properties`.
* If you continue to see `Fallback!` even after a minute or two, you may need to increase the value of   `PIPELINE_MODEL_SERVER_TIMEOUT_MILLISECONDS` in `pipeline_modelserver.properties`.  (This is rare as the default is 5000 milliseconds, but it may happen.)
* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.  This usually happens when using Docker Quick Terminal on Windows 7.
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.


# Train a PyTorch Model
## Inspect Model Directory
```
ls -l ./pytorch/mnist-cpu/model

### EXPECTED OUTPUT ###
...
pipeline_conda_environment.yml     <-- Required. Sets up the conda environment
pipeline_condarc                   <-- Required, but Empty is OK. Configure Conda proxy servers (.condarc)
pipeline_setup.sh                  <-- Required, but Empty is OK.  Init script performed upon Docker build
pipeline_train.py                  <-- Required. `main()` is required. Pass args with `--train-args`
...
```

## Build Training Server
```
pipeline train-server-build --model-name=mnist --model-tag=cpu --model-type=pytorch --model-path=./pytorch/mnist-cpu/model
```
Notes:  
* `--model-path` must be relative.  
* Add `--http-proxy=...` and `--https-proxy=...` if you see `CondaHTTPError: HTTP 000 CONNECTION FAILED for url`
* If you have issues, see the comprehensive [**Troubleshooting**](/docs/troubleshooting/README.md) section below.

## Start Training Server
```
pipeline train-server-start --model-name=mnist --model-tag=cpu
```
Notes:
* `--train-args` is a single argument passed into the `pipeline_train.py`.  Therefore, you must escape spaces (`\ `) between arguments. 
* `--input-path` and `--output-path` are relative to the current working directory (outside the Docker container) and will be mapped as directories inside the Docker container from `/root`.
* `--train-files` and `--eval-files` are relative to `--input-path` inside the Docker container.
* Models, logs, and event are written to `--output-path` (or a subdirectory within).  These will be available outside of the Docker container.
* To prevent overwriting the output of a previous run, you should either 1) change the `--output-path` between calls or 2) create a new unique subfolder with `--output-path` in your `pipeline_train.py` (ie. timestamp).  See examples below.
* On Windows, be sure to use the forward slash `\` for `--input-path` and `--output-path` (not the args inside of `--train-args`).
* If you see `port is already allocated` or `already in use by container`, you already have a container running.  List and remove any conflicting containers.  For example, `docker ps` and/or `docker rm -f train-mnist-cpu-pytorch-python-cpu`.
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.

(_We are working on making this more intuitive._)

## View Training Logs
```
pipeline train-server-logs --model-name=mnist --model-tag=cpu
```

_Press `Ctrl-C` to exit out of the logs._

## View Trained Model Output (Locally)
_Make sure you pressed `Ctrl-C` to exit out of the logs._
```
ls -l ./pytorch/mnist-cpu/model/

### EXPECTED OUTPUT ###
...
model.pkl   <-- Pickled Model File
...
```
_Multiple training runs will produce multiple subdirectories - each with a different timestamp._

## View Training UI (including TensorBoard for TensorFlow Models)
* TODO:  Add PipelineAI `tensorboard_logger` training-side metrics to pytorch linear demo
* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.
* This usually happens when using Docker Quick Terminal on Windows 7.
```
http://localhost:6006
```

# Deploy a PyTorch Model
The following model server uses a pickled pytorch model file.

## View Training Code
Click [HERE](https://github.com/PipelineAI/models/tree/master/pytorch/mnist-cpu/model) to see the Model.
```
ls -l ./pytorch/mnist-cpu/model/
```
```
cat ./pytorch/mnist-cpu/model/pipeline_train.py
```

## Build the PyTorch Training Server
```
pipeline train-server-build --model-name=mnist --model-tag=cpu --model-type=pytorch --model-path=./pytorch/mnist-cpu/model/
```

## Start the Training Server
```
pipeline train-server-start --model-name=mnist --model-tag=cpu --output-path=./pytorch/mnist-cpu/model/
```

## View the Training Logs
```
pipeline train-server-logs --model-name=mnist --model-tag=cpu

### EXPECTED OUTPUT ###

Pickled model to "/opt/ml/output/model.pkl"   <-- This docker-internal path maps to --output-path above
```

## Build the PyTorch Model Server
```
pipeline predict-server-build --model-name=mnist --model-tag=cpu --model-type=pytorch --model-path=./pytorch/mnist-cpu/model/
```

## Start the PyTorch Model Server
```
pipeline predict-server-start --model-name=mnist --model-tag=cpu
```

## View the PyTorch Model Server Logs
```
pipeline predict-server-logs --model-name=mnist --model-tag=cpu
```

## Predict with the Model 
### Curl Predict
```
curl -X POST -H "Content-Type: application/json" \
  -d '{"image": [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.05098039656877518, 0.529411792755127, 0.3960784673690796, 0.572549045085907, 0.572549045085907, 0.847058892250061, 0.8156863451004028, 0.9960784912109375, 1.0, 1.0, 0.9960784912109375, 0.5960784554481506, 0.027450982481241226, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.7882353663444519, 0.11764706671237946, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.988235354423523, 0.7921569347381592, 0.9450981020927429, 0.545098066329956, 0.21568629145622253, 0.3450980484485626, 0.45098042488098145, 0.125490203499794, 0.125490203499794, 0.03921568766236305, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.803921639919281, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6352941393852234, 0.9921569228172302, 0.803921639919281, 0.24705883860588074, 0.3490196168422699, 0.6509804129600525, 0.32156863808631897, 0.32156863808631897, 0.1098039299249649, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.007843137718737125, 0.7529412508010864, 0.9921569228172302, 0.9725490808486938, 0.9686275124549866, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.8274510502815247, 0.29019609093666077, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.2549019753932953, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.847058892250061, 0.027450982481241226, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5921568870544434, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.7333333492279053, 0.44705885648727417, 0.23137256503105164, 0.23137256503105164, 0.4784314036369324, 0.9921569228172302, 0.9921569228172302, 0.03921568766236305, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5568627715110779, 0.9568628072738647, 0.7098039388656616, 0.08235294371843338, 0.019607843831181526, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.43137258291244507, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.15294118225574493, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1882353127002716, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6705882549285889, 0.9921569228172302, 0.9921569228172302, 0.12156863510608673, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.2392157018184662, 0.9647059440612793, 0.9921569228172302, 0.6274510025978088, 0.003921568859368563, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08235294371843338, 0.44705885648727417, 0.16470588743686676, 0.0, 0.0, 0.2549019753932953, 0.9294118285179138, 0.9921569228172302, 0.9333333969116211, 0.27450981736183167, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.4941176772117615, 0.9529412388801575, 0.0, 0.0, 0.5803921818733215, 0.9333333969116211, 0.9921569228172302, 0.9921569228172302, 0.4078431725502014, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.7411764860153198, 0.9764706492424011, 0.5529412031173706, 0.8784314393997192, 0.9921569228172302, 0.9921569228172302, 0.9490196704864502, 0.43529415130615234, 0.007843137718737125, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6235294342041016, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9764706492424011, 0.6274510025978088, 0.1882353127002716, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.18431372940540314, 0.5882353186607361, 0.729411780834198, 0.5686274766921997, 0.3529411852359772, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]}' \
  http://localhost:8080 \
  -w "\n\n"
  
### EXPECTED OUTPUT ###

'{"variant": "mnist-cpu-pytorch-python-cpu", ...}'
```

### PipelineCLI Predict
```
pipeline predict-server-test --endpoint-url=http://localhost:8080/invocations --test-request-path=./pytorch/mnist-cpu/input/predict/test_request.json

### EXPECTED OUTPUT ###

'{"variant": "mnist-cpu-pytorch-python-cpu", ...}'
```
Notes:
* You may see `502 Bad Gateway` or `'{"results":["Fallback!"]}'` if you predict too quickly.  Let the server settle a bit - and try again.
* You will likely see `Fallback!` on the first successful invocation.  This is GOOD!  This means your timeouts are working.  Check out the `PIPELINE_MODEL_SERVER_TIMEOUT_MILLISECONDS` in `pipeline_modelserver.properties`.
* If you continue to see `Fallback!` even after a minute or two, you may need to increase the value of   `PIPELINE_MODEL_SERVER_TIMEOUT_MILLISECONDS` in `pipeline_modelserver.properties`.  (This is rare as the default is 5000 milliseconds, but it may happen.)
* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.  This usually happens when using Docker Quick Terminal on Windows 7.
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.
