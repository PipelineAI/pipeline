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
* `<YOUR_USER_ID>`  - 8 character id that uniquely identifies the PipelineAI user.  You will see the UserId in the upper right hand corner of the Settings tab after you login to [PipelineAI Community Edition](https://community.cloud.pipeline.ai)

![user-id](https://pipeline.ai/assets/img/user-id.png)

* `<YOUR_MODEL_NAME>` - User defined model name that uniquely identifies the model
* `<YOUR_TAG_NAME>` - User defined tag that uniquely identifies the model version
```
pipeline resource-upload --host community.cloud.pipeline.ai --user-id <YOUR_USER_ID> --resource-type model --resource-subtype tensorflow  --name <YOUR_MODEL_NAME> --tag <YOUR_TAG_NAME> --path ./tensorflow/mnist-v1/model
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


### Click `Projects` in the Top Nav
![![Nav Projects](https://pipeline.ai/assets/img/nav-projects.png)](https://community.cloud.pipeline.ai)

### Click `mnist`
![Select Project](https://pipeline.ai/assets/img/select-model.png)

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
