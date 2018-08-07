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

# Login to Community
```
https://community.cloud.pipeline.ai
```

# Upload Model to PipelineAI

You will need to fill in the unique values for the following:
* `<YOUR_USER_ID>`  - 8 character id that uniquely identifies the PipelineAI user.  You will see this in the upper right hand corner after you login to [PipelineAI Community Edition](https://community.cloud.pipeline.ai)</br>
![user-id](https://pipeline.ai/assets/img/user-id.png)
* `<YOUR_TAG_NAME>` - User defined tag that uniquely identifies the resource version
```
pipeline resource-upload --host community.cloud.pipeline.ai --user-id <YOUR_USER_ID> --resource-type model --resource-subtype tensorflow  --name mnist --tag <YOUR_TAG_NAME> --path ./tensorflow/mnist-v5/model
```

Actions performed:
* Compress resource source code into a tar archive.
* Create required directories and generate deployment and service resource definitions.
* Receive resource source code - or trained binary (ie. tensorflow SavedModel binary)
  from client as a tar archive then uncompress and extract on the PipelineAI server.
* Initialize training resource

# Optimize and Deploy the Model
## CLI - resource_optimize_and_deploy
You can copy the `resource_optimize_and_deploy` cli command from the Example section of the `resource-upload` command output, the command is automatically injected with your parameter values.

You will need to fill in the unique values for the following:
* `<YOUR_USER_ID>`  - 8 character id that uniquely identifies the PipelineAI user.  You will see this in the upper right hand corner after you login to [PipelineAI Community Edition](https://community.cloud.pipeline.ai)(https://community.cloud.pipeline.ai)
* `<YOUR_TAG_NAME>` - User defined tag that uniquely identifies the resource version
* `<YOUR_RESOURCE_ID>` - Id that uniquely identifies the uploaded model, resource-id is generated and returned by the `resource-upload` command
```
pipeline resource-optimize-and-deploy --host community.cloud.pipeline.ai --user-id <YOUR_USER_ID> --resource-type model --name mnist --tag <YOUR_TAG_NAME> --resource-subtype tensorflow --runtime-list \[python,tfserving,tflite\] --chip-list \[cpu,gpu\] --resource-id <YOUR_RESOURCE_ID> --kube-resource-type-list \[deploy,svc,ingress,routerules\]
```
## UI - Navigate to [PipelineAI Community Edition](https://community.cloud.pipeline.ai)
```
https://community.cloud.pipeline.ai
```

## Click `Models` in Left Nav
![Nav Models](https://pipeline.ai/assets/img/nav-models.png)

## Click `mnist`
![Select Model](https://pipeline.ai/assets/img/select-model.png)


## Click the `Optimize and Deploy` Action on Your Model
![Optimize and Deploy](https://pipeline.ai/assets/img/trained-models.png)

# Route Live Traffic to the Model
Wait a sec for your model to show up in the `Route Traffic` panel below

![Route Traffic](https://pipeline.ai/assets/img/route-traffic-2.png)

Note: You can select `Traffic Shadow` or assign `Traffic Split %` 

# Predict with UI `Invoke` Button
![Predict UI Invoke](https://pipeline.ai/assets/img/model-invoke.png)

# Predict with CLI
* `<YOUR_USER_ID>` <== You will see this when you login to PipelineAI Community Edition
* `<YOUR_TAG_NAME>`
```
pipeline predict-http-test --endpoint-url=https://community.cloud.pipeline.ai/predict/<YOUR_USER_ID>/mnist/invoke --test-request-path=./tensorflow/mnist-v5/input/predict/test_request.json --test-request-concurrency=1

### EXPECTED OUTPUT ###
...

{"variant": "mnist-<YOUR-MODEL-ID>-tensorflow-tfserving-cpu", "outputs":{"classes": [2], "probabilities": [[0.00681829359382391, 1.2452805009388612e-08, 0.5997999310493469, 0.2784779667854309, 6.614273297600448e-05, 0.10974341630935669, 0.00015215273015201092, 0.0002482662384863943, 0.004691515117883682, 2.2582455585506978e-06]]}}
...

### FORMATTED OUTPUT ###
Digit  Confidence
=====  ==========
0      0.00138249155133962
1      0.00036483019357547
2      0.59979993104934693  <-- Prediction
3      0.01074937824159860
4      0.00158193788956850
5      0.00006451825902331
6      0.00010775036207633
7      0.00010466964886290
8      0.04691515117883682   
9      0.00000471303883387
```

## Perform 100 Predictions in Parallel (Mini Load Test)
```
pipeline predict-http-test --endpoint-url=https://community.cloud.pipeline.ai/predict/<YOUR_USER_ID>/mnist/invoke --test-request-path=./tensorflow/mnist-v5/input/predict/test_request.json --test-request-concurrency=100
```

# Monitor Real-Time Prediction Dashboards

![Prediction Dashboard](http://pipeline.ai/assets/img/multi-cloud-prediction-dashboard.png)

![Prediction Dashboard](http://pipeline.ai/assets/img/request-metrics-breakdown.png)

# PipelineAI Quick Start (CPU, GPU, and TPU)
Train and Deploy your ML and AI Models in the Following Environments:
* [Hosted Community Edition](/docs/quickstart/community)
* [Docker](/docs/quickstart/docker)
* [Kubernetes](/docs/quickstart/kubernetes)
* [AWS SageMaker](/docs/quickstart/sagemaker)
