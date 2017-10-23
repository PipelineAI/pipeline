# PipelineAI [Home](http://pipeline.ai)
![PipelineAI Home](http://pipeline.ai/assets/img/pipelineai-home.png)

# PipelineAI [Products](http://pipeline.ai/products/)
[Community Edition](http://pipeline.ai/products/)

[Standalone Edition](http://pipeline.ai/products/)

[Enterprise Edition](http://pipeline.ai/products/)

# PipelineAI Resources
[PipelineAI Open Source](https://github.com/PipelineAI/)

[PipeilneAI + Kubernetes](https://github.com/PipelineAI/pipeline/wiki/)

[PipelineAI 24x7 Global Support](https://support.pipeline.ai/)

[TensorFlow + Spark + GPU Workshop](https://www.eventbrite.com/d/worldwide/pipelineai/?mode=search)

[AWS + GPU](https://github.com/PipelineAI/pipeline/wiki/AWS-GPU-TensorFlow-Docker)

[Google Cloud + GPU](https://github.com/PipelineAI/pipeline/wiki/GCP-GPU-TensorFlow-Docker)

# PipelineAI Core [Features](http://pipeline.ai/features)

## Consistent, Immutable, Reproducible Model Runtimes
![Consistent Model Environments](http://pipeline.ai/assets/img/docker-gobbles-ml.png)

Each model is built into a separate Docker image with the appropriate Python, C++, and Java/Scala Runtime Libraries for training or prediction.

Use the same Docker Image from Local Laptop to Production to avoid dependency surprises.

## Supported Model Types 
[scikit](https://github.com/PipelineAI/predict/tree/r1.3/models/scikit/), [tensorflow](https://github.com/PipelineAI/predict/tree/r1.3/models/tensorflow/), [python](https://github.com/PipelineAI/predict/tree/r1.3/models/java/), [keras](https://github.com/PipelineAI/predict/tree/r1.3/models/keras/), [pmml](https://github.com/PipelineAI/predict/tree/r1.3/models/pmml/), [spark](https://github.com/PipelineAI/predict/tree/r1.3/models/spark/), [java](https://github.com/PipelineAI/predict/tree/r1.3/models/java/), [xgboost](https://github.com/PipelineAI/predict/tree/r1.3/models/xgboost/), R

More [model samples](https://github.com/PipelineAI/models) coming soon (ie. R).

![Nvidia GPU](http://pipeline.ai/img/nvidia-cuda-338x181.png) ![TensorFlow](http://pipeline.ai/img/tensorflow-logo-202x168.png) 

![Spark ML](http://pipeline.ai/img/spark-logo-254x163.png) ![Scikit-Learn](http://pipeline.ai/img/scikit-logo-277x150.png) 

![R](http://pipeline.ai/img/r-logo-280x212.png) ![PMML](http://pipeline.ai/img/pmml-logo-210x96.png)

![Xgboost](http://pipeline.ai/img/xgboost-logo-280x120.png) ![Ensembles](http://pipeline.ai/img/ensemble-logo-285x125.png)

# Pre-Requisites
## Docker
* Install [Docker](https://www.docker.com/community-edition#/download)

## Python3 (Conda is Optional)
* Install [Miniconda](https://conda.io/docs/install/quick.html) with Python3 Support

## Install PipelineCLI
_Note: This command line interface requires **Python3** and **Docker** as detailed above._
``` 
pip install cli-pipeline==1.3.6 --ignore-installed --no-cache -U
```

## Verify Successful PipelineCLI Installation
```
pipeline version

### EXPECTED OUTPUT ###
cli_version: 1.3.6
api_version: v1

capabilities_enabled: ['server', 'prediction', 'version']
capabilities_disabled: ['cluster', 'optimizer', 'traffic']

Email `upgrade@pipeline.ai` to enable the advanced capabilities.
```

## Review CLI Functionality
```
pipeline

### EXPECTED OUTPUT ###
Usage:       pipeline                     <-- This List of CLI Commands

(Enterprise) pipeline cluster-connect     <-- Create Secure Tunnel to Model Server Cluster 
             pipeline cluster-describe    <-- Describe Model Server Cluster
             pipeline cluster-logs        <-- View Model Server Cluster Logs 
             pipeline cluster-quarantine  <-- Remove Instance from Model Server Cluster (Forensic Debugging)
             pipeline cluster-rollback    <-- Rollback Model Server Cluster (undo cluster-rollout)
             pipeline cluster-rollout     <-- Rollout New Version of Model Server Cluster
             pipeline cluster-scale       <-- Scale Model Server Cluster
             pipeline cluster-shell       <-- Shell into Model Server Cluster
             pipeline cluster-start       <-- Start Model Server Cluster (from Registry)
             pipeline cluster-status      <-- Status of Model Server Cluster
             pipeline cluster-stop        <-- Stop Model Server Cluster
             pipeline cluster-train       <-- Train Model on Distributed Cluster of Servers

(Standalone) pipeline optimizer-generate  <-- Generate Optimized Models for a Given Model

(Community)  pipeline prediction-loadtest <-- Prediction Load Test on Model Server
             pipeline prediction-test     <-- Prediction Test on Model Server

(Enterprise) pipeline traffic-deregister  <-- De-register a Model Server Cluster from Taking Traffic
             pipeline traffic-describe    <-- Show Traffic Status of a Model Server Cluster
             pipeline traffic-register    <-- Register a Model Server Cluster to Take Traffic
             pipeline traffic-shadow      <-- Duplicate Traffic to Model Server Cluster (Shadow Canary)
             pipeline traffic-split       <-- Split Traffic within Model Server Cluster (Split Canary)
             pipeline traffic-status      <-- Show Traffic Status of all Model Server Clusters

(Community)  pipeline server-build        <-- Build Model Server
             pipeline server-logs         <-- View Model Server Logs
             pipeline server-pull         <-- Pull Model Server from Registry (ie. docker pull)
             pipeline server-push         <-- Push Model Server to Registry (ie. docker push)
             pipeline server-shell        <-- Shell into Model Server (Forensic Debugging)
             pipeline server-start        <-- Start Model Server
             pipeline server-stop         <-- Stop Model Server
             pipeline server-train        <-- Train Model on Single Server

(Community)  pipeline version             <-- View This CLI Version
```

# Prepare Model Samples
## Clone the PipelineAI Predict Repo
```
git clone https://github.com/PipelineAI/predict
```

## Change into `predict` Directory
```
cd predict 
```

# Model Predictions
## Inspect Model Directory
```
ls -l ./models/tensorflow/mnist

### EXPECTED OUTPUT ###
pipeline_conda_environment.yml     <-- Required.  Sets up the conda environment
pipeline_install.sh                <-- Optional.  If file exists, we run it
pipeline_predict.py                <-- Required.  `predict(request: bytes) -> bytes` is required
pipeline_train.py                  <-- Optional.  `main()` to train the model
versions/                          <-- Optional.  If directory exists, we start TensorFlow Serving
```

## Inspect PipelineAI Predict Module `./models/tensorflow/mnist/pipeline_predict.py`
```
cat ./models/tensorflow/mnist/pipeline_predict.py

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

_model = _initialize_upon_import()  <-- Optional.  Called once upon server startup

_labels = {'model_type': os.environ['PIPELINE_MODEL_TYPE'],   <-- Optional.  Tag metrics
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

## Build Example Model into Docker Image
```
pipeline server-build --model-type=tensorflow --model-name=mnist --model-tag=v1 --model-path=./models/tensorflow/mnist
```
_`model-path` must be a relative path._

## Start the Model Server
```
pipeline server-start --model-type=tensorflow --model-name=mnist --model-tag=v1 --memory-limit=4G
```
_If the port is already allocated, run `docker ps`, then `docker rm -f <container-id>`._

## Monitor Runtime Logs
Wait for the model runtime to settle...
```
pipeline server-logs --model-type=tensorflow --model-name=mnist --model-tag=v1

### EXPECTED OUTPUT ###
...
2017-10-10 03:56:00.695  INFO 121 --- [     run-main-0] i.p.predict.jvm.PredictionServiceMain$   : Started PredictionServiceMain. in 7.566 seconds (JVM running for 20.739)
[debug] 	Thread run-main-0 exited.
[debug] Waiting for thread container-0 to terminate.
...
INFO[0050] Completed initial partial maintenance sweep through 4 in-memory fingerprints in 40.002264633s.  source="storage.go:1398"
...
```
_You need to `ctrl-c` out of the log viewing before proceeding._

## PipelineAI Prediction CLI
### Perform Prediction
_The first call takes 10-20x longer than subsequent calls (and may timeout causing a "fallback" message) due to lazy initialization and warm-up._

_Try the call again if you see a "fallback" message._

_Before proceeding, make sure you hit `ctrl-c` after viewing the logs in the command above._
```
pipeline predict-model --model-type=tensorflow --model-name=mnist --model-tag=v1 --predict-server-url=http://localhost:6969 --test-request-path=./models/tensorflow/mnist/data/test_request.json

### Expected Output ###
{"outputs": [0.0022526539396494627, 2.63791100074684e-10, 0.4638307988643646, 0.21909376978874207, 3.2985670372909226e-07, 0.29357224702835083, 0.00019597385835368186, 5.230629176367074e-05, 0.020996594801545143, 5.426473762781825e-06]}

### Formatted Output ###
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

### Perform 100 Predictions in Parallel (Mini Load Test)
```
pipeline predict-model --model-type=tensorflow --model-name=mnist --model-tag=v1 --predict-server-url=http://localhost:6969 --test-request-path=./models/tensorflow/mnist/data/test_request.json --test-request-concurrency=100
```

## PipelineAI Prediction REST API
Use the REST API to POST a JSON document representing the number 2.

![MNIST 2](http://pipeline.ai/assets/img/mnist-2-100x101.png)

```
curl -X POST -H "Content-Type: application/json" \
  -d '{"image": [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.05098039656877518, 0.529411792755127, 0.3960784673690796, 0.572549045085907, 0.572549045085907, 0.847058892250061, 0.8156863451004028, 0.9960784912109375, 1.0, 1.0, 0.9960784912109375, 0.5960784554481506, 0.027450982481241226, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.7882353663444519, 0.11764706671237946, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.988235354423523, 0.7921569347381592, 0.9450981020927429, 0.545098066329956, 0.21568629145622253, 0.3450980484485626, 0.45098042488098145, 0.125490203499794, 0.125490203499794, 0.03921568766236305, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.803921639919281, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6352941393852234, 0.9921569228172302, 0.803921639919281, 0.24705883860588074, 0.3490196168422699, 0.6509804129600525, 0.32156863808631897, 0.32156863808631897, 0.1098039299249649, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.007843137718737125, 0.7529412508010864, 0.9921569228172302, 0.9725490808486938, 0.9686275124549866, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.8274510502815247, 0.29019609093666077, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.2549019753932953, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.847058892250061, 0.027450982481241226, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5921568870544434, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.7333333492279053, 0.44705885648727417, 0.23137256503105164, 0.23137256503105164, 0.4784314036369324, 0.9921569228172302, 0.9921569228172302, 0.03921568766236305, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5568627715110779, 0.9568628072738647, 0.7098039388656616, 0.08235294371843338, 0.019607843831181526, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.43137258291244507, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.15294118225574493, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1882353127002716, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6705882549285889, 0.9921569228172302, 0.9921569228172302, 0.12156863510608673, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.2392157018184662, 0.9647059440612793, 0.9921569228172302, 0.6274510025978088, 0.003921568859368563, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08235294371843338, 0.44705885648727417, 0.16470588743686676, 0.0, 0.0, 0.2549019753932953, 0.9294118285179138, 0.9921569228172302, 0.9333333969116211, 0.27450981736183167, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.4941176772117615, 0.9529412388801575, 0.0, 0.0, 0.5803921818733215, 0.9333333969116211, 0.9921569228172302, 0.9921569228172302, 0.4078431725502014, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.7411764860153198, 0.9764706492424011, 0.5529412031173706, 0.8784314393997192, 0.9921569228172302, 0.9921569228172302, 0.9490196704864502, 0.43529415130615234, 0.007843137718737125, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6235294342041016, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9764706492424011, 0.6274510025978088, 0.1882353127002716, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.18431372940540314, 0.5882353186607361, 0.729411780834198, 0.5686274766921997, 0.3529411852359772, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]}' \
  http://localhost:6969/api/v1/model/predict/tensorflow/mnist/v1 \
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

## Monitor Real-Time Prediction Metrics
Re-run the Prediction REST API while watching the following dashboard URL:
```
http://localhost:6969/hystrix-dashboard/monitor/monitor.html?streams=%5B%7B%22name%22%3A%22%22%2C%22stream%22%3A%22http%3A%2F%2Flocalhost%3A6969%2Fhystrix.stream%22%2C%22auth%22%3A%22%22%2C%22delay%22%3A%22%22%7D%5D
```
![Real-Time Throughput and Response Time](http://pipeline.ai/assets/img/hystrix-mini.png)

## Monitor Detailed Prediction Metrics
Re-run the Prediction REST API while watching the following detailed metrics dashboard URL:
```
http://localhost:3000/
```
![Prediction Dashboard](http://pipeline.ai/assets/img/request-metrics-breakdown.png)

_Username/Password: **admin**/**admin**_
_Set `Type` to `Prometheues`._

_Set `Url` to `http://localhost:9090`._

_Set `Access` to `direct`._

_Click `Save & Test`_.

_Click `Dashboards -> Import` upper-left menu drop-down_.

_Copy and Paste [THIS](https://raw.githubusercontent.com/PipelineAI/predict/r1.3/dashboard/grafana/pipelineai-prediction-dashboard.json) raw json file into the `paste JSON` box_.

_Select the Prometheus-based data source that you setup above and click `Import`_.

_Create additional PipelineAI Prediction widgets using [THIS](https://prometheus.io/docs/practices/histograms/#count-and-sum-of-observations) guide to the Prometheus Syntax._

## Stop Model Server
```
pipeline server-stop --model-type=tensorflow --model-name=mnist --model-tag=v1
```

# [PipelineAI Standalone and Enterprise Features](http://pipeline.ai/features)
Click [HERE](http://pipeline.ai/products) to compare PipelineAI Products.

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
