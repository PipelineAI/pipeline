![PipelineAI Logo](http://pipeline.ai/assets/img/logo/pipelineai-logo.png)

### Requirements
* Python 3 ([Conda](https://conda.io/docs/install/quick.html) is Preferred)
* (Windows Only) Install [PowerShell](https://github.com/PowerShell/PowerShell/tree/master/docs/installation) 

### (Optional) Uninstall Any Existing PipelineAI CLI
```
pip uninstall cli-pipeline
```

### Install PipelineAI CLI
[![PyPI PipelineAI CLI](https://badge.fury.io/py/cli-pipeline@2x.png)](https://pypi.python.org/pypi/cli-pipeline/)
```
pip install cli-pipeline==1.5.316 --default-timeout=120 --ignore-installed --no-cache --upgrade
```
Notes: 
* You may need to uninstall any existing `cli-pipeline` using `pip uninstall cli-pipeline`
* You may also use `--user` if you're still having issues.
* This command line interface requires **Python 2 or 3** and **Docker** as detailed above in the Pre-Requisites section.
* If you're having trouble, use one of our support channels [**HERE**](/docs/troubleshooting) to let us know!
* Followed these steps described here: https://apple.stackexchange.com/questions/254380/macos-sierra-invalid-active-developer-path:  1) `sudo xcode-select --reset` (didn't work for me, but including this because it worked for others) or 2) `xcode-select --install`
* If you have any issues, you may want to create a separate virtualenv or conda environment to isolate the environments.
* You may need to increase `--default-timeout=120` to avoid `ReadTimeoutError: HTTPSConnectionPool(host='files.pythonhosted.org', port=443): Read timed out.`
* You may need to run `pip uninstall -y python-dateutil` if you see an issue related to `pip._vendor.pkg_resources.ContextualVersionConflict`
* Ignore anything along these lines: `urllib3 (1.23) or chardet (3.0.4) doesn't match a supported version! RequestsDependencyWarning`

### Verify Successful PipelineAI CLI Installation
```
pipeline version

### EXPECTED OUTPUT ###
cli_version: 1.5.x    <-- MAKE SURE THIS MATCHES THE VERSION YOU INSTALLED ABOVE

default train base image: docker.io/pipelineai/train-cpu:1.5.0     
default predict base image: docker.io/pipelineai/predict-cpu:1.5.0 
```

### PipelineAI Quick Start (CPU, GPU, and TPU)
Train and Deploy your ML and AI Models in the Following Environments:
* [Hosted Community Edition](/docs/quickstart/community)
* [Docker](/docs/quickstart/docker)
* [Kubernetes](/docs/quickstart/kubernetes)
* [AWS SageMaker](/docs/quickstart/sagemaker)

### PipelineAI CLI Overview
```
pipeline

### EXPECTED OUTPUT ###
...
env-kube-activate            <-- Switch Kubernetes Clusters
env-conda-activate           <-- Switch Conda Environments
env-registry-sync            <-- Sync with the latest Docker images

help                         <-- This List of CLI Commands

model-archive-tar            <-- Create Tar Archive for Model Server
model-archive-untar          <-- Untar Model Server Archive

predict-http-test            <-- Test Model Cluster (Http-based)

predict-kube-autoscale       <-- Configure AutoScaling for Model Cluster
predict-kube-connect         <-- Create Secure Tunnel to Model Cluster 
predict-kube-describe        <-- Describe Model Cluster (Raw)
predict-kube-endpoint        <-- Retrieve Model Cluster Endpoint 
predict-kube-endpoints       <-- Retrieve All Model Cluster Endpoints
predict-kube-logs            <-- View Model Cluster Logs 
predict-kube-route           <-- Route Live Traffic  
predict-kube-routes          <-- Describe Routes
predict-kube-scale           <-- Scale Model Cluster
predict-kube-shell           <-- Shell into Model Cluster
predict-kube-start           <-- Start Model Cluster from Docker Registry
predict-kube-stop            <-- Stop Model Cluster
predict-kube-test            <-- Test Model Cluster

predict-sage-describe        <-- Describe of SageMaker Model Predict Cluster
predict-sage-route           <-- Route Live Traffic in SageMaker
predict-sage-start           <-- Start Model Cluster in SageMaker
predict-sage-stop            <-- Stop Model Cluster in SageMaker
predict-sage-test            <-- Test Model Cluster in SageMaker

predict-server-build         <-- Build Model Server
predict-server-describe      <-- Describe Model Server
predict-server-logs          <-- View Model Server Logs
predict-server-pull          <-- Pull Model Server from Docker Registry
predict-server-register      <-- Register Model Server with Docker Registry
predict-server-shell         <-- Shell into Model Server (Debugging)
predict-server-start         <-- Start Model Server
predict-server-stop          <-- Stop Model Server
predict-server-tar           <-- Tar Model Server
predict-server-test          <-- Test Model Server (Http-based)
predict-server-untar         <-- Untar Model Server Tar File

predict-stream-test          <-- Test Stream-based Model Server

resource-upload              <-- Add Model to PipelineAI Cluster
resource-optimize-and-deploy <-- Optimize and Deploy Model to PipelineAI Cluster
resource-routes-get          <-- Retrieve Current Model Server Routes
resource-routes-set          <-- Set Model Server Routes

stream-http-consume          <-- Consume Stream Messages (REST API)

stream-kube-consume          <-- Consume Messages from Stream
stream-kube-produce          <-- Produce Messages to Stream

train-kube-connect           <-- Create Secure Tunnel to Training Cluster
train-kube-describe          <-- Describe Training Cluster
train-kube-logs              <-- View Training Cluster Logs
train-kube-scale             <-- Scale Training Cluster
train-kube-shell             <-- Shell into Training Cluster
train-kube-start             <-- Start Training Cluster from Docker Registry
train-kube-stop              <-- Stop Training Cluster

train-server-build           <-- Build Training Server
train-server-logs            <-- View Training Server Logs
train-server-pull            <-- Pull Training Server from Docker Registry
train-server-register        <-- Register Training Server with Docker Registry
train-server-shell           <-- Shell into Training Server (Debugging)
train-server-start           <-- Start Training Server
train-server-stop            <-- Stop Training Server

version                      <-- View This CLI Version
```

# Having Issues?  Contact Us Anytime... We're Always Awake.
* Slack:  https://joinslack.pipeline.ai
* Email:  [help@pipeline.ai](mailto:help@pipeline.ai)
* Web:  https://support.pipeline.ai
* YouTube:  https://youtube.pipeline.ai
* Slideshare:  https://slideshare.pipeline.ai
* [Troubleshooting Guide](/docs/troubleshooting)
