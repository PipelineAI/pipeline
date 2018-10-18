![PipelineAI Logo](http://pipeline.ai/assets/img/logo/pipelineai-logo.png)

_Note:  These Instructions are a Work in Progress..._

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

# Train Models
* [TensorFlow](#train-a-tensorflow-model)
* [Scikit-Learn](#train-a-scikit-learn-model)
* [PyTorch](#train-a-pytorch-model)
* [Xgboost](#train-an-xgboost-model)

# Train a TensorFlow Model
## Inspect Model Directory
```
ls -l ./tensorflow/mnist-v1/model

### EXPECTED OUTPUT ###
...
pipeline_conda_environment.yml     <-- Required. Sets up the conda environment
pipeline_condarc                   <-- Required, but Empty is OK. Configure Conda proxy servers (.condarc)
pipeline_setup.sh                  <-- Required, but Empty is OK.  Init script performed upon Docker build
pipeline_train.py                  <-- Required. `main()` is required. Pass args with `--train-args`
...
```

## Build Training Server

Arguments between `[` `]` are optional 
```
pipeline train-server-build --model-name=mnist --model-tag=v1tensorflow --model-type=tensorflow --model-path=./tensorflow/mnist-v1/model
```
Notes: 
* If you change the model (`pipeline_train.py`), you'll need to re-run `pipeline train-server-build ...`
* `--model-path` must be relative to the current ./models directory (cloned from https://github.com/PipelineAI/models)
* Add `--http-proxy=...` and `--https-proxy=...` if you see `CondaHTTPError: HTTP 000 CONNECTION FAILED for url`
* For GPU-based models, make sure you specify `--model-chip=gpu`
* If you have issues, see the comprehensive [**Troubleshooting**](/docs/troubleshooting/README.md) section below.

## Start Training Server
```
pipeline train-server-start --memory-limit=2G --model-name=mnist --model-tag=v1tensorflow --input-host-path=./tensorflow/mnist-v1/model/ --output-host-path=./tensorflow/mnist-v1/model/ --training-runs-host-path=./tensorflow/mnist-v1/model/ --train-args="--train_epochs=2 --batch_size=100 --model_dir=. --data_dir=. --export_dir=./pipeline_tfserving/"
```
Notes: 
* You may need to clear the `--model_dir` before you start, otherwise the training job may not start
* `--train-args` are within the Docker container relative to `.`
* _Ignore the following warning: `WARNING: Your kernel does not support swap limit capabilities or the cgroup is not mounted. Memory limited without swap.`_
* If you change the model (`pipeline_train.py`), you need to re-run `pipeline train-server-build ...`
* Increase `--memory-limit` if you have issues with `Allocated Memory`
* You should run `pipeline train-server-stop --model-name=mnist --model-tag=v1` if you see `Error response from daemon: Conflict. The container name "/train-mnist-v1" is already in use by container. You have to remove (or rename) that container to be able to reuse that name.`
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.

Show Training Logs
```
pipeline train-server-logs --model-name=mnist --model-tag=v1tensorflow
```

## View Trained Model Output (Locally)
_Make sure you pressed `Ctrl-C` to exit out of the logs._
```
ls -l ./tensorflow/mnist-v1/model/pipeline_tfserving/

### EXPECTED OUTPUT ###
...
0/           <-- 1st training run
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

## Stop Training Server
```
pipeline train-server-stop --model-name=mnist --model-tag=v1tensorflow
```

# Train a Scikit-Learn Model
## Inspect Model Directory
```
ls -l ./scikit/mnist/model

### EXPECTED OUTPUT ###
...
pipeline_conda_environment.yaml     <-- Required. Sets up the conda environment
pipeline_condarc                   <-- Required, but Empty is OK. Configure Conda proxy servers (.condarc)
pipeline_setup.sh                  <-- Required, but Empty is OK.  Init script performed upon Docker build
pipeline_train.py                  <-- Required. `main()` is required. Pass args with `--train-args`
...
```

## View Training Code
```
cat ./scikit/mnist/model/pipeline_train.py
```

## Build Training Server
```
pipeline train-server-build --model-name=mnist --model-tag=v1scikit --model-type=scikit --model-path=./scikit/mnist/model
```
Notes:  
* `--model-path` must be relative.  
* Add `--http-proxy=...` and `--https-proxy=...` if you see `CondaHTTPError: HTTP 000 CONNECTION FAILED for url`
* For GPU-based models, make sure you specify `--model-chip=gpu`

* If you have issues, see the comprehensive [**Troubleshooting**](/docs/troubleshooting/README.md) section below.

## Start Training Server
```
pipeline train-server-start --model-name=mnist --model-tag=v1scikit --input-host-path=./scikit/mnist/model/ --output-host-path=./scikit/mnist/model/ --training-runs-host-path=./scikit/mnist/model/ --train-args="" --start-cmd-extra-args='--shm-size=512m'
```
Notes:
* Ignore the following warning: `WARNING: Your kernel does not support swap limit capabilities or the cgroup is not mounted. Memory limited without swap.`
* If you are creating your own model from the scikit example above - and not parallelizing Scikit-learn during training time - you may not need the `--start-cmd-extra-args` argument unless you're seeing [THIS](https://stackoverflow.com/questions/44664900/oserror-errno-28-no-space-left-on-device-docker-but-i-have-space) issue with "No space left on device Docker"
* For GPU-based models, make sure you specify `--start-cmd=nvidia-docker` - and make sure you have `nvidia-docker` installed!

## View the Training Logs
```
pipeline train-server-logs --model-name=mnist --model-tag=v1scikit

### EXPECTED OUTPUT ###

Pickled model to "/opt/ml/output/model.pkl"   <-- This docker-internal path maps to --output-host-path above
```

_Press `Ctrl-C` to exit out of the logs._

## View Trained Model Output (Locally)
* Make sure you pressed `Ctrl-C` to exit out of the logs.
```
ls -l ./scikit/mnist/model/

### EXPECTED OUTPUT ###
...
model.pkl   <-- Pickled Model File
...
```

# Train a PyTorch Model
## Inspect Model Directory
```
ls -l ./pytorch/mnist-v1/model

### EXPECTED OUTPUT ###
...
pipeline_conda_environment.yml     <-- Required. Sets up the conda environment
pipeline_condarc                   <-- Required, but Empty is OK. Configure Conda proxy servers (.condarc)
pipeline_setup.sh                  <-- Required, but Empty is OK.  Init script performed upon Docker build
pipeline_train.py                  <-- Required. `main()` is required. Pass args with `--train-args`
...
```

## View Training Code
```
cat ./pytorch/mnist-v1/model/pipeline_train.py
```

## Build Training Server
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline train-server-build --model-name=mnist --model-tag=v1pytorch --model-type=pytorch --model-path=./pytorch/mnist-v1/model/
```
Notes:
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
* `--model-path` must be relative.  
* Add `--http-proxy=...` and `--https-proxy=...` if you see `CondaHTTPError: HTTP 000 CONNECTION FAILED for url`
* For GPU-based models, make sure you specify `--model-chip=gpu` - and make sure you have `nvidia-docker` installed!
* If you have issues, see the comprehensive [**Troubleshooting**](/docs/troubleshooting/README.md) section below.

## Start Training Server
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline train-server-start --model-name=mnist --model-tag=v1pytorch --input-host-path=./pytorch/mnist-v1/model/ --output-host-path=./pytorch/mnist-v1/model/ --training-runs-host-path=./pytorch/mnist-v1/model/ --train-args=""
```
Notes:
* Ignore the following warning: `WARNING: Your kernel does not support swap limit capabilities or the cgroup is not mounted. Memory limited without swap.`
* For GPU-based models, make sure you specify `--start-cmd=nvidia-docker` - and make sure you have `nvidia-docker` installed!

## View the Training Logs
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline train-server-logs --model-name=mnist --model-tag=v1pytorch

### EXPECTED OUTPUT ###

Pickled model to "/opt/ml/output/model.pth"   <-- This docker-internal path maps to --output-host-path above
```

_Press `Ctrl-C` to exit out of the logs._

## View Trained Model Output (Locally)
_Make sure you pressed `Ctrl-C` to exit out of the logs._
```
ls -l ./pytorch/mnist-v1/model/

### EXPECTED OUTPUT ###
...
model.pth   <-- Trained Model File
...
```

# Train an Xgboost Model
## Inspect Model Directory
```
ls -l ./xgboost/mnist-v1/model

### EXPECTED OUTPUT ###
...
pipeline_conda_environment.yml     <-- Required. Sets up the conda environment
pipeline_condarc                   <-- Required, but Empty is OK. Configure Conda proxy servers (.condarc)
pipeline_setup.sh                  <-- Required, but Empty is OK.  Init script performed upon Docker build
pipeline_train.py                  <-- Required. `main()` is required. Pass args with `--train-args`
...
```

## View Training Code
```
cat ./xgboost/mnist-v1/model/pipeline_train.py
```

## Build Training Server
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline train-server-build --model-name=mnist --model-tag=v1xgboost --model-type=xgboost --model-path=./xgboost/mnist-v1/model/
```
Notes:
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
* `--model-path` must be relative.  
* Add `--http-proxy=...` and `--https-proxy=...` if you see `CondaHTTPError: HTTP 000 CONNECTION FAILED for url`
* For GPU-based models, make sure you specify `--model-chip=gpu` - and make sure you have `nvidia-docker` installed!
* If you have issues, see the comprehensive [**Troubleshooting**](/docs/troubleshooting/README.md) section below.

## Start Training Server
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline train-server-start --model-name=mnist --model-tag=v1xgboost --input-host-path=./xgboost/mnist-v1/model/ --output-host-path=./xgboost/mnist-v1/model/ --training-runs-host-path=./xgboost/mnist-v1/model/ --train-args=""
```
Notes:
* Ignore the following warning: `WARNING: Your kernel does not support swap limit capabilities or the cgroup is not mounted. Memory limited without swap.`
* For GPU-based models, make sure you specify `--start-cmd=nvidia-docker` - and make sure you have `nvidia-docker` installed!

## View the Training Logs
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
```
pipeline train-server-logs --model-name=mnist --model-tag=v1xgboost

### EXPECTED OUTPUT ###

Pickled model to "/opt/ml/output/model.pth"   <-- This docker-internal path maps to --output-host-path above
```

_Press `Ctrl-C` to exit out of the logs._

## View Trained Model Output (Locally)
_Make sure you pressed `Ctrl-C` to exit out of the logs._
```
ls -l ./xgboost/mnist-v1/model/

### EXPECTED OUTPUT ###
...
model.pth   <-- Trained Model File
...
```

## PipelineAI Quick Start (CPU, GPU, and TPU)
Train and Deploy your ML and AI Models in the Following Environments:
* [Hosted Community Edition](/docs/quickstart/community)
* [Docker](/docs/quickstart/docker)
* [Kubernetes](/docs/quickstart/kubernetes)
* [AWS SageMaker](/docs/quickstart/sagemaker)
