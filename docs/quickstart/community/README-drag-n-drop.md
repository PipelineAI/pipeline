![PipelineAI Logo](http://pipeline.ai/assets/img/logo/pipelineai-logo.png)

## Pull All Sample PipelineAI Models from GitHub (SLOW)
```
git clone https://github.com/PipelineAI/models
```

Change into the new `./models/tensorflow/mnist-v5/` directory
```
cd ./models/tensorflow/mnist-v5/model/
```

# Install [PipelineAI CLI](../README.md#install-pipelinecli)
* Install Python 2 or 3 ([Conda](https://conda.io/docs/install/quick.html) is Preferred)
* Install (Windows Only) Install [PowerShell](https://github.com/PowerShell/PowerShell/tree/master/docs/installation) 
* Click [**HERE**](../README.md#install-pipelinecli) to install the PipelineAI CLI

# Deploy Model with Drag n' Drop

## Package the Model for Upload
```
pipeline model-archive-tar --model-name=mnist --model-tag=<YOURTAGNAME> --model-path tensorflow/mnist-v5/model
```

## Navigate to PipelineAI Community Edition
```
https://community.cloud.pipeline.ai/admin/app/select/model
```

## Click Plus Button
![Click Plus Button](https://pipeline.ai/assets/img/click-plus-button.png)

## Set Model Metadata
![Model Metadata](https://pipeline.ai/assets/img/model-metadata.png)

## Select Upload Archive
![Upload Archive](https://pipeline.ai/assets/img/upload-archive.png)

## Drag n' Drop Your Model
![Drag n' Drop](https://pipeline.ai/assets/img/drag-and-drop-model.png)

## Click Add Button
![Click Add Button](https://pipeline.ai/assets/img/click-add-button.png)

## Click NEXT After Your Model Uploads


# Optimize and Deploy the Model
```
https://community.cloud.pipeline.ai
```

## Click `Models` in Left Nav

## Click `mnist`

## Find Your Model and Click `Optimize and Deploy`

# Route Traffic to the Model (Traffic Shadow/Mirror or Traffic Split)
Wait a sec for your model to show up in the `Route Traffic` panel below

Select either the `Traffic Shadow` checkbox or select a `Traffic Split %` 

# Predict with CLI
* Before proceeding, make sure you hit `Ctrl-C` after viewing the logs in the previous step.
```
pipeline predict-http-test --endpoint-url=https://community.cloud.pipeline.ai/predict/<YOUR-MODEL-ID>/invoke --test-request-path=./tensorflow/mnist-v5/input/predict/test_request.json --test-request-concurrency=1

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
pipeline predict-http-test --endpoint-url=https://community.cloud.pipeline.ai/predict/<YOUR-MODEL-ID>/invoke --test-request-path=./tensorflow/mnist-v5/input/predict/test_request.json --test-request-concurrency=100
```

# Monitor Real-Time Prediction Dashboards

![Prediction Dashboard](http://pipeline.ai/assets/img/multi-cloud-prediction-dashboard.png)

![Prediction Dashboard](http://pipeline.ai/assets/img/request-metrics-breakdown.png)

# PipelineAI Quick Start (CPU, GPU, and TPU)
Train and Deploy your ML and AI Models in the Following Environments:
* [Community](/docs/quickstart/community)
* [Docker](/docs/quickstart/docker)
* [Kubernetes](/docs/quickstart/kubernetes)
* [AWS SageMaker](/docs/quickstart/sagemaker)

