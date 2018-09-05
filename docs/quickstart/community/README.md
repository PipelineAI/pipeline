[![PipelineAI Logo](http://pipeline.ai/assets/img/logo/pipelineai-community-logo.png)](https://community.cloud.pipeline.ai)

# Install [PipelineAI CLI](../README.md#install-pipelinecli)
* Install Python 2 or 3 ([Conda](https://conda.io/docs/install/quick.html) is Preferred)
* Install (Windows Only) Install [PowerShell](https://github.com/PowerShell/PowerShell/tree/master/docs/installation) 
* Click [**HERE**](../README.md#install-pipelinecli) to install the PipelineAI CLI

# Deploy a TensorFlow Model - CLI
## Clone Sample Models from [PipelineAI GitHub](https://github.com/PipelineAI/models)
```
git clone https://github.com/PipelineAI/models
```

## Change to Clone `models/` directory
```
cd models/
```

## Review the Model Before We Optimize & Deploy
### TensorFlow Model
```
ls -al models/tensorflow/mnist-v1/model/pipeline_tfserving/0

### EXPECTED OUTPUT ###

-rw-r--r--  1 cfregly  staff  40136 Jul 20 18:38 saved_model.pb   <== TensorFlow Model
drwxr-xr-x  4 cfregly  staff    128 Jul 20 18:38 variables
```

### Sample `pipeline_conda_environment.yaml` Requirements
```
cat ./tensorflow/mnist-v1/model/pipeline_invoke_python.py

### EXPECTED OUTPUT ###

dependencies:
  - python=3.6
  - pip:
    - grpcio>=1.0
    - tensorflow==1.10.1    
    - pipeline-runtime==1.0.8
```

## Sample `pipeline_invoke_<runtime>.py` Function
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
           'tag': 'v1',
           'type': 'tensorflow',
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
    request_np = (np.array(request_json['image'], dtype=np.float32) / 255.0).reshape(1, 28, 28)
    return {"image": request_np}


def _transform_response(response):
    return json.dumps({"classes": response['classes'].tolist(),
                       "probabilities": response['probabilities'].tolist(),
                      })

if __name__ == '__main__':
    with open('./pipeline_test_request.json', 'rb') as fb:
        request_bytes = fb.read()
        response_bytes = invoke(request_bytes)
        print(response_bytes)
```

# Login to Community
```
https://community.cloud.pipeline.ai
```

# Upload Model to PipelineAI

You will need to fill in the unique values for the following:
* `<YOUR_USER_ID>`  - 8 character id that uniquely identifies the PipelineAI user.  You will see the UserId in the upper right hand corner of the Settings tab after you login to [PipelineAI Community Edition](https://community.cloud.pipeline.ai)

![user-id](https://pipeline.ai/assets/img/user-id.png)

* `<UNIQUE_MODEL_NAME>` - User-defined model/project name that uniquely identifies a model/project within your account.
* `<UNIQUE_TAG_NAME>` - User-defined tag that uniquely identifies the model tag/version within a model/project
```
pipeline resource-upload --host community.cloud.pipeline.ai --user-id <YOUR_USER_ID> --resource-type model --resource-subtype tensorflow  --name <UNIQUE_MODEL_NAME> --tag <UNIQUE_TAG_NAME> --path ./tensorflow/mnist-v1/model
```

Actions performed:
* Compress resource source code into a tar archive.
* Create required directories and generate deployment and service resource definitions.
* Receive resource source code - or trained binary (ie. tensorflow SavedModel binary)
  from client as a tar archive then uncompress and extract on the PipelineAI server.
* Initialize training resource

# Optimize and Deploy the Model
You can optimize (select one or more chips and/or one or more runtimes) and deploy your model using the CLI or the [UI](https://community.cloud.pipeline.ai) (Choose either the CLI or UI).

## UI - Navigate to [PipelineAI Community Edition](https://community.cloud.pipeline.ai)
```
https://community.cloud.pipeline.ai
```

### Click `Models` in the Top Nav

![Select Model](https://pipeline.ai/assets/img/select-model.png)

### Click the `Deploy` Button on Your Model
![Optimize and Deploy](https://pipeline.ai/assets/img/trained-models.png)

# Route Live Traffic to the Model
Wait a sec for your model to show up in the `Route Traffic` panel below

![Route Traffic](https://pipeline.ai/assets/img/route-traffic-2.png)

Note: You can select `Traffic Shadow` or assign `Traffic Split %` 

# Predict with UI `Invoke` Button
![Predict UI Invoke](https://pipeline.ai/assets/img/model-invoke.png)

# Real-Time Prediction Dashboards

![Prediction Dashboard](http://pipeline.ai/assets/img/multi-cloud-prediction-dashboard.png)

![Prediction Dashboard](http://pipeline.ai/assets/img/request-metrics-breakdown.png)

# PipelineAI Quick Start (CPU, GPU, and TPU)
Train and Deploy your ML and AI Models in the Following Environments:
* [Hosted Community Edition](/docs/quickstart/community)
* [Docker](/docs/quickstart/docker)
* [Kubernetes](/docs/quickstart/kubernetes)
* [AWS SageMaker](/docs/quickstart/sagemaker)
