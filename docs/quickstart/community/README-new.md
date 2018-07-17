## Pull PipelineAI [Sample Models](https://github.com/PipelineAI/models)
```
git clone https://github.com/PipelineAI/models
```

**Change into the new `models/` directory**
```
cd models
```

## Requirements
* Install Python 2 or 3 ([Conda](https://conda.io/docs/install/quick.html) is Preferred)
* Install (Windows Only) Install [PowerShell](https://github.com/PowerShell/PowerShell/tree/master/docs/installation) 

## Install [PipelineAI CLI](../README.md#install-pipelinecli)
* Click [**HERE**](../README.md#install-pipelinecli) to install the PipelineAI CLI

## Login to Community
* Username:  community@pipeline.ai
* Password:  Password9!
```
https://community.cloud.pipeline.ai
```

## Deploy a TensorFlow Model
### Python Serving Runtime (Python Runtime)
```
pipeline resource-deploy --host=community.cloud.pipeline.ai --user-id <YOUR_USER_ID> --resource-type model --name mnist --tag <YOUR_TAG_NAME> --path ./tensorflow/mnist-v3/model/ --type tensorflow --runtime tfserving --chip cpu
```

### TensorFlow Serving Runtime (C++ Runtime)
```
pipeline resource-deploy --host=community.cloud.pipeline.ai --user-id <YOUR_USER_ID> --resource-type model --name mnist --tag <YOUR_TAG_NAME> --path ./tensorflow/mnist-v3/model/ --type tensorflow --runtime tfserving --chip cpu
```

### TensorFlow Lite Runtime (Quantized Weights, Pruned Neural Network)
```
pipeline resource-deploy --host=community.cloud.pipeline.ai --user-id <YOUR_USER_ID> --resource-type model --name mnist --tag <YOUR_TAG_NAME> --path ./tensorflow/mnist-v3/model/ --type tensorflow --runtime tflite --chip cpu
```
