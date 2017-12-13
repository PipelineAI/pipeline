# PipelineAI [Home](http://pipeline.ai)
[![PipelineAI Home](http://pipeline.ai/assets/img/pipelineai-home.png)](http://pipeline.ai)
[![PipelineAI Home 1](http://pipeline.ai/assets/img/pipelineai-home-1.png)](http://pipeline.ai)
[![PipelineAI Home 2](http://pipeline.ai/assets/img/pipelineai-home-2.png)](http://pipeline.ai)

# PipelineAI + AWS SageMaker
PipelineAI is fully compatible with [AWS SageMaker](https://docs.aws.amazon.com/sagemaker/latest/dg/how-it-works-hosting.html).

Specifically, you can upload PipelineAI-optimized Docker images to your private AWS Elastic Container Registry (ECR) for use with AWS SageMaker's Custom Docker image support.

Click [HERE](#using-pipelineai-with-aws-sagemaker) for more details.

![PipelineAI + AWS SageMaker Dashboard](http://pipeline.ai/assets/img/sagemaker-dashboard-sm.png)

![PipelineAI + AWS SageMaker Overview](http://pipeline.ai/assets/img/sagemaker-pipelineai-overview.png)

# PipelineAI [Products](http://pipeline.ai/products/)
[Community Edition](http://pipeline.ai/products/)

[Standalone Edition](http://pipeline.ai/products/)

[Enterprise Edition](http://pipeline.ai/products/)

# PipelineAI Deployments
* [Deploy PipelineAI Across Clouds on CPU and GPU](https://github.com/PipelineAI/pipeline/tree/master/docs/deploy)
* [Deploy PipelineAI on GPUs](https://github.com/PipelineAI/pipeline/blob/master/docs/gpu/README.md)
* [PipelineAI 24x7 Global Support](https://support.pipeline.ai/)
* [PipelineAI Workshops (TensorFlow + Spark + GPU)](https://www.eventbrite.com/d/worldwide/pipelineai/?mode=search)

# PipelineAI [Feature Comparison](http://pipeline.ai/features)

## Consistent, Immutable, Reproducible Model Runtimes
![Consistent Model Environments](http://pipeline.ai/assets/img/docker-gobbles-ml.png)

Each model is built into a separate Docker image with the appropriate Python, C++, and Java/Scala Runtime Libraries for training or prediction.

Use the same Docker Image from Local Laptop to Production to avoid dependency surprises.

## Sample Machine Learning and AI Models
Click [**HERE**](https://github.com/PipelineAI/models/tree/master) to view model samples for the following:
* Scikit-Learn
* TensorFlow
* Keras
* Spark ML (formerly called Spark MLlib)
* XgBoost
* Custom Java
* Custom Python
* Ensembles

![Nvidia GPU](http://pipeline.ai/assets/img/nvidia-cuda-338x181.png) ![TensorFlow](http://pipeline.ai/assets/img/tensorflow-logo-202x168.png) 

![Spark ML](http://pipeline.ai/assets/img/spark-logo-254x163.png) ![Scikit-Learn](http://pipeline.ai/assets/img/scikit-logo-277x150.png) 

![R](http://pipeline.ai/assets/img/r-logo-280x212.png) ![PMML](http://pipeline.ai/img/pmml-logo-210x96.png)

![Xgboost](http://pipeline.ai/assets/img/xgboost-logo-280x120.png) ![Ensembles](http://pipeline.ai/assets/img/ensemble-logo-285x125.png)

Coming Soon:  Amazon MXNet, Microsoft CNTK, and Gluon

## Supported Model Runtimes (CPU and GPU)
* Python
* Java
* Scala
* C++
* Caffe2
* Theano
* TensorFlow Serving (TensorFlow Only)
* Nvidia TensorRT (TensorFlow, Caffe2)

Coming Soon:  Amazon MXNet, Microsoft CNTK, and Gluon

# Pre-Requisites
## Docker
* Install [Docker](https://www.docker.com/community-edition#/download)

## Python2 or Python3 (Conda is Optional)
* Install [Miniconda](https://conda.io/docs/install/quick.html) with Python 2 or 3 (Preferred) Support

## Install PipelineCLI
_Note: This command line interface requires **Python 2 or 3** and **Docker** as detailed above._
``` 
pip install cli-pipeline==1.4.26 --ignore-installed --no-cache -U
```

## Verify Successful PipelineCLI Installation
```
pipeline version

### EXPECTED OUTPUT ###
cli_version: 1.4.x    <-- MAKE SURE THIS MATCHES THE VERSION YOU INSTALLED ABOVE
api_version: v1

default build type: docker
default build context path: . => ...

default train base image: docker.io/pipelineai/train:cpu-1.4.0     
default predict base image: docker.io/pipelineai/predict:cpu-1.4.0 

capabilities_enabled: ['train-server-*', 'predict-server-*', 'predict-test-http']
capabilities_available: ['train-cluster-*', 'predict-cluster-*', 'predict-test-stream', 'optimize-predict-*', 'optimize-train-*', 'traffic-router-*', 'spark-cluster-*', 'airflow-cluster-*', 'jupyter-cluster-*', 'kafka-cluster-*']

Email upgrade@pipeline.ai to enable the advanced capabilities.
```

## Review CLI Functionality
[Community Edition](http://pipeline.ai/products/)

[Standalone Edition](http://pipeline.ai/products/)

[Enterprise Edition](http://pipeline.ai/products/)

```
pipeline

### EXPECTED OUTPUT ###
...
Usage:       pipeline                             <-- This List of CLI Commands
             
(Enterprise) pipeline predict-cluster-autoscale   <-- Configure AutoScaling for Model Cluster
             pipeline predict-cluster-connect     <-- Create Secure Tunnel to Model Cluster 
             pipeline predict-cluster-describe    <-- Describe Model Cluster
             pipeline predict-cluster-logs        <-- View Model Cluster Logs 
             pipeline predict-cluster-scale       <-- Scale Model Cluster
             pipeline predict-cluster-shell       <-- Shell into Model Cluster
             pipeline predict-cluster-start       <-- Start Model Cluster from Docker Registry
             pipeline predict-cluster-status      <-- Status of Model Cluster
             pipeline predict-cluster-stop        <-- Stop Model Cluster
             
(Community)  pipeline predict-server-build        <-- Build Model Server
             pipeline predict-server-logs         <-- View Model Server Logs
             pipeline predict-server-pull         <-- Pull Model Server from Docker Registry
             pipeline predict-server-push         <-- Push Model Server to Docker Registry
             pipeline predict-server-shell        <-- Shell into Model Server (Debugging)
             pipeline predict-server-start        <-- Start Model Server
             pipeline predict-server-stop         <-- Stop Model Server

(Community)  pipeline predict-test-http           <-- Predict with Http-based Model Endpoint
             pipeline predict-test-sage           <-- Predict with Sage-based Model Endpoint
(Standalone) pipeline predict-test-stream         <-- Predict with Kafka-based Model Endpoint 

(Enterprise) pipeline traffic-router-describe     <-- Describe Traffic Split and Shadow %
             pipeline traffic-router-split        <-- Split Traffic Among Model Variants
             
(Enterprise) pipeline train-cluster-connect       <-- Create Secure Tunnel to Training Cluster
             pipeline train-cluster-describe      <-- Describe Training Cluster
             pipeline train-cluster-logs          <-- View Training Cluster Logs
             pipeline train-cluster-scale         <-- Scale Training Cluster
             pipeline train-cluster-shell         <-- Shell into Training Cluster
             pipeline train-cluster-start         <-- Start Training Cluster from Docker Registry
             pipeline train-cluster-status        <-- Status of Training Cluster
             pipeline train-cluster-stop          <-- Stop Training Cluster

(Community)  pipeline train-server-build          <-- Build Training Server
             pipeline train-server-logs           <-- View Training Server Logs
             pipeline train-server-pull           <-- Pull Training Server from Docker Registry
             pipeline train-server-push           <-- Push Training Server to Docker Registry
             pipeline train-server-shell          <-- Shell into Training Server (Debugging)
             pipeline train-server-start          <-- Start Training Server
             pipeline train-server-stop           <-- Stop Training Server
             
(Community)  pipeline version                     <-- View This CLI Version
...
```

# Step 1: Retrieve Sample Models
## Clone the PipelineAI Predict Repo
```
git clone https://github.com/PipelineAI/models
```

## Change into `models` Directory
```
cd ./models
```

# Step 2: Train a Model
## Inspect Model Directory
```
ls -l ./tensorflow/census/model

### EXPECTED OUTPUT ###
...
pipeline_conda_environment.yml     <-- Required.  Sets up the conda environment
pipeline_condarc                   <-- Required.  Configure Conda proxy servers (.condarc)
pipeline_train.py                  <-- Required.  `main()` is required. Pass args with `--train-args`
...
```

## Build Training Server
```
pipeline train-server-build --model-type=tensorflow --model-name=census --model-tag=v1 --model-path=./tensorflow/census/model
```
Notes:  
* `--model-path` must be relative.  On Windows, be sure to use the forward slash `\` for `--model-path`.
* If you see `CondaHTTPError: HTTP 000 CONNECTION FAILED for url` or `[Errno 111] Connection refused'` or `ConnectionError(MaxRetryError("HTTPSConnectionPool`, you need to update `./tensorflow/census/model/pipeline_condarc` to include proxy servers per [THIS](https://conda.io/docs/user-guide/configuration/use-condarc.html#configure-conda-for-use-behind-a-proxy-server-proxy-servers) document.

## Start Training UI
```
pipeline train-server-start --model-type=tensorflow --model-name=census --model-tag=v1 --input-path=./tensorflow/census/input --output-path=./tensorflow/census/output --train-args="--train-files=training/adult.training.csv\ --eval-files=validation/adult.validation.csv\ --num-epochs=2\ --learning-rate=0.025"
```
Notes:
* `--train-args` is a single argument passed into the `pipeline_train.py`.  Therefore, you must escape spaces (`\ `) between arguments. 
* `--input-path` and `--output-path` are relative to the current working directory (outside the Docker container) and will be mapped as directories inside the Docker container from `/root`.
* `--train-files` and `--eval-files` are relative to `--input-path` inside the Docker container.
* Models, logs, and event are written to `--output-path` (or a subdirectory within).  These will be available outside of the Docker container.
* To prevent overwriting the output of a previous run, you should either 1) change the `--output-path` between calls or 2) create a new unique subfolder with `--output-path` in your `pipeline_train.py` (ie. timestamp).  See examples below.
* On Windows, be sure to use the forward slash `\` for `--input-path` and `--output-path` (not the args inside of `--train-args`).
* If you see `port is already allocated` or `already in use by container`, you already have a container running.  List and remove any conflicting containers.  For example, `docker ps` and/or `docker rm -f train-census-v1-tensorflow-tfserving-cpu`.

(_We are working on making this more intuitive._)

## View Training Logs
```
pipeline train-server-logs --model-type=tensorflow --model-name=census --model-tag=v1
```

_Press `Ctrl-C` to exit out of the logs._

## View Trained Model Output (Locally)
_Make sure you pressed `Ctrl-C` to exit out of the logs._
```
ls -l ./tensorflow/census/output/

### EXPECTED OUTPUT ###
...
drwxr-xr-x  11 cfregly  staff  352 Nov 22 11:20 1511367633 
drwxr-xr-x  11 cfregly  staff  352 Nov 22 11:21 1511367665
drwxr-xr-x  11 cfregly  staff  352 Nov 22 11:22 1511367765 <= Sub-directories of training output
...
```
_Multiple training runs will produce multiple subdirectories - each with a different timestamp._

## View Training UI (including TensorBoard for TensorFlow Models)
```
http://localhost:6006
```
![PipelineAI TensorBoard UI 0](http://pipeline.ai/assets/img/pipelineai-train-census-tensorboard-0.png)

![PipelineAI TensorBoard UI 1](http://pipeline.ai/assets/img/pipelineai-train-census-tensorboard-1.png)

![PipelineAI TensorBoard UI 2](http://pipeline.ai/assets/img/pipelineai-train-census-tensorboard-2.png)

![PipelineAI TensorBoard UI 3](http://pipeline.ai/assets/img/pipelineai-train-census-tensorboard-3.png)

## Stop Training UI
```
pipeline train-server-stop --model-type=tensorflow --model-name=census --model-tag=v1
```

# Step 3: Predict with Model

## Inspect Model Directory
_Note:  This is relative to where you cloned the `models` repo [above](#clone-the-pipelineai-predict-repo)._
```
ls -l ./tensorflow/mnist/model

### EXPECTED OUTPUT ###
...
pipeline_conda_environment.yml     <-- Required.  Sets up the conda environment
pipeline_condarc                   <-- Required.  Configure Conda proxy servers (.condarc)
pipeline_predict.py                <-- Required.  `predict(request: bytes) -> bytes` is required
pipeline_tfserving/                <-- Optional.  Only TensorFlow Serving requires this directory
...
```
Inspect TensorFlow Serving Model 
```
ls -l ./tensorflow/mnist/model/pipeline_tfserving/

### EXPECTED OUTPUT ###
...
pipeline_tfserving.config  <-- Required by TensorFlow Serving. Custom request-batch sizes, etc.
1510612525/  
1510612528/     <-- TensorFlow Serving finds the latest (highest) version 
...
```

## Build the Model into a Runnable Docker Image
This command bundles the TensorFlow runtime with the model.
```
pipeline predict-server-build --model-type=tensorflow --model-name=mnist --model-tag=v1 --model-path=./tensorflow/mnist/model
```
Notes:
* `--model-path` must be relative.  On Windows, be sure to use the forward slash `\` for `--model-path`.
* If you see `CondaHTTPError: HTTP 000 CONNECTION FAILED for url` or `[Errno 111] Connection refused'` or `ConnectionError(MaxRetryError("HTTPSConnectionPool`, you need to update `./tensorflow/census/pipeline_condarc` to include proxy servers per [THIS](https://conda.io/docs/user-guide/configuration/use-condarc.html#configure-conda-for-use-behind-a-proxy-server-proxy-servers) document.

## Start the Model Server
```
pipeline predict-server-start --model-type=tensorflow --model-name=mnist --model-tag=v1 --memory-limit=2G
```
Notes:  
* If you see `port is already allocated` or `already in use by container`, you already have a container running.  List and remove any conflicting containers.  For example, `docker ps` and/or `docker rm -f train-tfserving-tensorflow-mnist-v1`.

## Inspect `pipeline_predict.py`
_Note:  Only the `predict()` method is required.  Everything else is optional._
```
cat ./tensorflow/mnist/model/pipeline_predict.py

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
                                  inputs_name='inputs',       <-- Optional.  TensorFlow SignatureDef inputs
                                  outputs_name='outputs',     <-- Optional.  TensorFlow SignatureDef outputs
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
pipeline predict-server-logs --model-type=tensorflow --model-name=mnist --model-tag=v1

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
pipeline predict-test-http --model-endpoint-url=http://localhost:8080 --test-request-path=./tensorflow/mnist/input/predict/test_request.json

### EXPECTED OUTPUT ###
...
{"outputs": [0.0022526539396494627, 2.63791100074684e-10, 0.4638307988643646, 0.21909376978874207, 3.2985670372909226e-07, 0.29357224702835083, 0.00019597385835368186, 5.230629176367074e-05, 0.020996594801545143, 5.426473762781825e-06]}

### FORMATTED OUTPUT ###
Digit  Confidence
=====  ==========
0      0.0022526539396494627
1      2.63791100074684e-10
2      0.4638307988643646      <-- Prediction
3      0.21909376978874207
4      3.2985670372909226e-07
5      0.29357224702835083 
6      0.00019597385835368186
7      5.230629176367074e-05
8      0.020996594801545143
9      5.426473762781825e-06

### IGNORE THESE ERRORS BELOW.  WAIT A MINUTE, THEN RE-RUN THE COMMAND ABOVE. ###
...
<html>\r\n<head><title>502 Bad Gateway</title></head></html>
...
...
{"results":["fallback"]}
...
```
Notes:
* You may see `502 Bad Gateway` or `'{"results":["fallback"]}'` if you predict too quickly.  Let the server settle a bit - and try again.

* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.  This usually happens when using Docker Quick Terminal on Windows 7.

## Perform 100 Predictions in Parallel (Mini Load Test)
```
pipeline predict-test-http --model-endpoint-url=http://localhost:8080 --test-request-path=./tensorflow/mnist/input/predict/test_request.json --test-request-concurrency=100
```
Notes:
* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.  This usually happens when using Docker Quick Terminal on Windows 7.


## Predict with REST API
Use the REST API to POST a JSON document representing the number 2.

![MNIST 2](http://pipeline.ai/assets/img/mnist-2-100x101.png)
```
curl -X POST -H "Content-Type: application/json" \
  -d '{"image": [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.05098039656877518, 0.529411792755127, 0.3960784673690796, 0.572549045085907, 0.572549045085907, 0.847058892250061, 0.8156863451004028, 0.9960784912109375, 1.0, 1.0, 0.9960784912109375, 0.5960784554481506, 0.027450982481241226, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.7882353663444519, 0.11764706671237946, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.988235354423523, 0.7921569347381592, 0.9450981020927429, 0.545098066329956, 0.21568629145622253, 0.3450980484485626, 0.45098042488098145, 0.125490203499794, 0.125490203499794, 0.03921568766236305, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.803921639919281, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6352941393852234, 0.9921569228172302, 0.803921639919281, 0.24705883860588074, 0.3490196168422699, 0.6509804129600525, 0.32156863808631897, 0.32156863808631897, 0.1098039299249649, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.007843137718737125, 0.7529412508010864, 0.9921569228172302, 0.9725490808486938, 0.9686275124549866, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.8274510502815247, 0.29019609093666077, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.2549019753932953, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.847058892250061, 0.027450982481241226, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5921568870544434, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.7333333492279053, 0.44705885648727417, 0.23137256503105164, 0.23137256503105164, 0.4784314036369324, 0.9921569228172302, 0.9921569228172302, 0.03921568766236305, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5568627715110779, 0.9568628072738647, 0.7098039388656616, 0.08235294371843338, 0.019607843831181526, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.43137258291244507, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.15294118225574493, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1882353127002716, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6705882549285889, 0.9921569228172302, 0.9921569228172302, 0.12156863510608673, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.2392157018184662, 0.9647059440612793, 0.9921569228172302, 0.6274510025978088, 0.003921568859368563, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08235294371843338, 0.44705885648727417, 0.16470588743686676, 0.0, 0.0, 0.2549019753932953, 0.9294118285179138, 0.9921569228172302, 0.9333333969116211, 0.27450981736183167, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.4941176772117615, 0.9529412388801575, 0.0, 0.0, 0.5803921818733215, 0.9333333969116211, 0.9921569228172302, 0.9921569228172302, 0.4078431725502014, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.7411764860153198, 0.9764706492424011, 0.5529412031173706, 0.8784314393997192, 0.9921569228172302, 0.9921569228172302, 0.9490196704864502, 0.43529415130615234, 0.007843137718737125, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6235294342041016, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9764706492424011, 0.6274510025978088, 0.1882353127002716, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.18431372940540314, 0.5882353186607361, 0.729411780834198, 0.5686274766921997, 0.3529411852359772, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]}' \
  http://localhost:8080/invocations \
  -w "\n\n"

### Expected Output ###
{"outputs": [0.0022526539396494627, 2.63791100074684e-10, 0.4638307988643646, 0.21909376978874207, 3.2985670372909226e-07, 0.29357224702835083, 0.00019597385835368186, 5.230629176367074e-05, 0.020996594801545143, 5.426473762781825e-06]}

### Formatted Output
Digit  Confidence
=====  ==========
0      0.0022526539396494627
1      2.63791100074684e-10
2      0.4638307988643646      <-- Prediction
3      0.21909376978874207
4      3.2985670372909226e-07
5      0.29357224702835083 
6      0.00019597385835368186
7      5.230629176367074e-05
8      0.020996594801545143
9      5.426473762781825e-06
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

## Monitor Detailed Prediction Metrics
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

_Copy and Paste [THIS](https://raw.githubusercontent.com/PipelineAI/predict/master/dashboard/grafana/pipelineai-prediction-dashboard.json) raw json file into the `paste JSON` box_.

_Select the Prometheus-based data source that you setup above and click `Import`_.

_Change the Date Range in the upper right to `Last 5m` and the Refresh Every to `5s`._

_Create additional PipelineAI Prediction widgets using [THIS](https://prometheus.io/docs/practices/histograms/#count-and-sum-of-observations) guide to the Prometheus Syntax._

## Stop Model Server
```
pipeline predict-server-stop --model-type=tensorflow --model-name=mnist --model-tag=v1
```

# Using PipelineAI with AWS SageMaker
PipelineAI is fully compatible with [AWS SageMaker](https://docs.aws.amazon.com/sagemaker/latest/dg/how-it-works-hosting.html).

Specifically, you can upload PipelineAI-optimized Docker images to your private AWS Elastic Container Registry (ECR) for use with AWS SageMaker's Custom Docker image support.

![PipelineAI + AWS SageMaker Dashboard](http://pipeline.ai/assets/img/sagemaker-dashboard-sm.png)

![PipelineAI + AWS SageMaker Overview](http://pipeline.ai/assets/img/sagemaker-pipelineai-overview.png)

## Upload Docker Image to AWS SageMaker
Follow [THESE](http://docs.aws.amazon.com/AmazonECR/latest/userguide/docker-push-ecr-image.html) steps to upload the `predict-mnist` Docker image above to AWS SageMaker.

## Create Model Endpoint
Follow the steps below to create an AWS SageMaker Model Endpoint with the Docker Image uploaded in the previous step.

![PipelineAI + AWS SageMaker Model Dashboard](http://pipeline.ai/assets/img/sagemaker-model-dashboard.png)

![PipelineAI + AWS SageMaker Model Detail 0](http://pipeline.ai/assets/img/sagemaker-model-detail-0.png)

![PipelineAI + AWS SageMaker Model Detail 1](http://pipeline.ai/assets/img/sagemaker-model-detail-1.png)

![PipelineAI + AWS SageMaker Model Detail 2](http://pipeline.ai/assets/img/sagemaker-model-detail-2.png)

![PipelineAI + AWS SageMaker Model Endpoint Configuration](http://pipeline.ai/assets/img/sagemaker-endpoint-configuration.png)

![PipelineAI + AWS SageMaker Model Endpoint](http://pipeline.ai/assets/img/sagemaker-endpoint.png)

![PipelineAI + AWS SageMaker Model Endpoint Detail 1](http://pipeline.ai/assets/img/sagemaker-endpoint-detail-1.png)

![PipelineAI + AWS SageMaker Model Endpoint Detail 2](http://pipeline.ai/assets/img/sagemaker-endpoint-detail-2.png)

## Perform 100 Predictions in Parallel (Mini Load Test)
_Note:  This step assumes you have setup your AWS credentials in your environment.  Follow [THESE](http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html) steps to setup your AWS credentials for this PipelineAI CLI command._
```
pipeline predict-test-sage --model-endpoint-name=<your-endpoint-name> --test-request-path=./tensorflow/mnist/input/predict/test_request.json --test-request-concurrency=100

### EXPECTED OUTPUT ###
...
Variant: 'default-variant-name'            <-- Variant name (ie. v1)

('{"outputs":{"outputs": [0.11128007620573044, 1.4478533557849005e-05, '
 '0.43401220440864563, 0.06995827704668045, 0.0028081508353352547, '
 '0.27867695689201355, 0.017851119861006737, 0.006651509087532759, '
 '0.07679300010204315, 0.001954273320734501]}}')
...
``` 

# Additional PipelineAI [Standalone](http://pipeline.ai/products) and [Enterprise](http://pipeline.ai/products) Features
See below for feature details.  Click [HERE](http://pipeline.ai/products) to compare PipelineAI Products.

## Drag N' Drop Model Deploy
![PipelineAI Drag n' Drop Model Deploy UI](http://pipeline.ai/assets/img/drag-n-drop-tri-color.png)

## Generate Optimize Model Versions Upon Upload
![Automatic Model Optimization and Native Code Generation](http://pipeline.ai/assets/img/automatic-model-optimization-native-code-generation.png)

## Distributed Model Training and Hyper-Parameter Tuning
![PipelineAI Advanced Model Training UI](http://pipeline.ai/assets/img/pipelineai-train-compare-ui.png)

![PipelineAI Advanced Model Training UI 2](http://pipeline.ai/assets/img/pipelineai-train-compare-ui-2.png)

## Continuously Deploy Models to Clusters of PipelineAI Servers
![PipelineAI Weavescope Kubernetes Cluster](http://pipeline.ai/assets/img/weavescope-with-header.png)

## View Real-Time Prediction Stream
![Live Stream Predictions](http://pipeline.ai/assets/img/live-stream-predictions.png)

## Compare Both Offline (Batch) and Real-Time Model Performance
![PipelineAI Model Comparison](http://pipeline.ai/assets/img/dashboard-batch-and-realtime.png)

## Compare Response Time, Throughput, and Cost-Per-Prediction
![PipelineAI Compare Performance and Cost Per Prediction](http://pipeline.ai/assets/img/compare-cost-per-prediction.png)

## Shift Live Traffic to Maximize Revenue and Minimize Cost
![PipelineAI Traffic Shift Multi-armed Bandit Maxmimize Revenue Minimize Cost](http://pipeline.ai/assets/img/maximize-revenue-minimize-costs.png)

## Continuously Fix Borderline Predictions through Crowd Sourcing 
![Borderline Prediction Fixing and Crowd Sourcing](http://pipeline.ai/assets/img/fix-slack.png)
