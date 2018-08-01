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
