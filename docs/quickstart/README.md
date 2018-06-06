![PipelineAI Logo](http://pipeline.ai/assets/img/logo/pipelineai-split-black-258x62.png)

### Requirements
* 8GB
* 4 Cores
* Install [Docker](https://www.docker.com/community-edition#/download)
* Python 2 or 3 ([Conda](https://conda.io/docs/install/quick.html) is Preferred)
* (Windows Only) Install [PowerShell](https://github.com/PowerShell/PowerShell/tree/master/docs/installation) 

### Install PipelineAI CLI
```
pip install cli-pipeline==1.5.158 --ignore-installed --no-cache --upgrade
```
Notes: 
* This command line interface requires **Python 2 or 3** and **Docker** as detailed above in the Pre-Requisites section.
* If you're having trouble, use one of our support channels [**HERE**](/docs/troubleshooting) to let us know!
* Followed these steps described here: https://apple.stackexchange.com/questions/254380/macos-sierra-invalid-active-developer-path:  1) `sudo xcode-select --reset` (didn't work for me, but including this because it worked for others) or 2) `xcode-select --install`
* If you have any issues, you may want to create a separate virtualenv or conda environment to isolate the environments.
* You may need to run `pip uninstall -y python-dateutil` if you see an issue related to `pip._vendor.pkg_resources.ContextualVersionConflict`
* Ignore anything along these lines: `urllib3 (1.23) or chardet (3.0.4) doesn't match a supported version! RequestsDependencyWarning`
* You may also use `--user` if you're still having issues.

### PipelineAI Quick Start (CPU, GPU, and TPU)
Train and Deploy your ML and AI Models in the Following Environments:
* [Docker](/docs/quickstart/docker)
* [Kubernetes](/docs/quickstart/kubernetes)
* [AWS SageMaker](/docs/quickstart/sagemaker)

### Verify Successful PipelineAI CLI Installation
```
pipeline version

### EXPECTED OUTPUT ###
cli_version: 1.5.x    <-- MAKE SURE THIS MATCHES THE VERSION YOU INSTALLED ABOVE

default train base image: docker.io/pipelineai/train-cpu:1.5.0     
default predict base image: docker.io/pipelineai/predict-cpu:1.5.0 
```

## Sync Latest Docker Images
```
pipeline env-registry-sync --tag=1.5.0
```

### PipelineAI CLI Overview
```
pipeline

### EXPECTED OUTPUT ###
...
env-conda-activate          <-- Switch to a New Model (Updates Conda Environment)
env-registry-sync           <-- Sync with the latest Docker images

help                        <-- This List of CLI Commands

model-archive-tar           <-- Create Tar Archive for Model Server
model-archive-untar         <-- Untar Model Server Archive

model-source-get            <-- Retrieve file within Model Directory
model-source-init           <-- Initialize Model Directory to Prepare for New Model

model-source-list           <-- List Model Source Directories 
model-source-set            <-- Update file within a Model Source Directory

predict-http-test           <-- Test Model Cluster (Http-based)

predict-kube-autoscale      <-- Configure AutoScaling for Model Cluster
predict-kube-connect        <-- Create Secure Tunnel to Model Cluster 
predict-kube-describe       <-- Describe Model Cluster (Raw)
predict-kube-endpoint       <-- Retrieve Model Cluster Endpoint 
predict-kube-endpoints      <-- Retrieve All Model Cluster Endpoints
predict-kube-logs           <-- View Model Cluster Logs 
predict-kube-route          <-- Route Live Traffic  
predict-kube-routes         <-- Describe Routes
predict-kube-scale          <-- Scale Model Cluster
predict-kube-shell          <-- Shell into Model Cluster
predict-kube-start          <-- Start Model Cluster from Docker Registry
predict-kube-stop           <-- Stop Model Cluster
predict-kube-test           <-- Test Model Cluster

predict-sage-describe       <-- Describe of SageMaker Model Predict Cluster
predict-sage-route          <-- Route Live Traffic in SageMaker
predict-sage-start          <-- Start Model Cluster in SageMaker
predict-sage-stop           <-- Stop Model Cluster in SageMaker
predict-sage-test           <-- Test Model Cluster in SageMaker

predict-server-build        <-- Build Model Server
predict-server-describe     <-- Describe Model Server
predict-server-logs         <-- View Model Server Logs
predict-server-pull         <-- Pull Model Server from Docker Registry
predict-server-register     <-- Register Model Server with Docker Registry
predict-server-shell        <-- Shell into Model Server (Debugging)
predict-server-start        <-- Start Model Server
predict-server-stop         <-- Stop Model Server
predict-server-tar          <-- Tar Model Server
predict-server-test         <-- Test Model Server (Http-based)
predict-server-untar        <-- Untar Model Server Tar File

predict-stream-test         <-- Test Stream-based Model Server

predict-tensorflow-describe <-- Describe TensorFlow Model Server (saved_model_cli)

spark-kube-scale          <-- Scale Spark Cluster on Kubernetes

stream-http-consume         <-- Consume Stream Messages (REST API)
stream-http-describe        <-- Describe Stream (REST API)
stream-http-produce         <-- Produce Stream Messages (REST API)

stream-kube-consume         <-- Consume Messages from Stream
stream-kube-describe        <-- Describe Stream
stream-kube-produce         <-- Produce Messages to Stream
stream-kube-start           <-- Start Stream (Kafka, MQTT)
stream-kube-stop            <-- Stop Stream

train-kube-connect          <-- Create Secure Tunnel to Training Cluster
train-kube-describe         <-- Describe Training Cluster
train-kube-logs             <-- View Training Cluster Logs
train-kube-scale            <-- Scale Training Cluster
train-kube-shell            <-- Shell into Training Cluster
train-kube-start            <-- Start Training Cluster from Docker Registry
train-kube-stop             <-- Stop Training Cluster

train-server-build          <-- Build Training Server
train-server-logs           <-- View Training Server Logs
train-server-pull           <-- Pull Training Server from Docker Registry
train-server-register       <-- Register Training Server with Docker Registry
train-server-shell          <-- Shell into Training Server (Debugging)
train-server-start          <-- Start Training Server
train-server-stop           <-- Stop Training Server

version                     <-- View This CLI Version
```

### Questions or Support
Contact Us 24x7.  We Never Sleep!
* [Slack](https://joinslack.pipeline.ai)
* [Web](https://support.pipeline.ai)
* [YouTube](https://youtube.pipeline.ai)
* [Slideshare](https://slideshare.pipeline.ai)
* [Troubleshooting Guide](/docs/troubleshooting)
