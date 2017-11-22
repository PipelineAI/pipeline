# Deploy PipelineAI

## Step 1:  Download Latest [Docker Community Edition](https://www.docker.com/community-edition) for Mac, Windows, or Linux

## Step 2:  Pull Latest Docker Image with Kubernetes CLI Installed
```
sudo docker pull pipelineai/kubernetes:master
```

## Step 3:  Start the Docker Container with Kubernetes CLI Installed
```
sudo docker run -itd --name kubernetes pipelineai/kubernetes:master
```
Make sure you include the `kubernetes:master` tag as part of the DockerHub reference above. ^^

## Step 4:  Shell into Docker Container to Setup Kubernetes Cluster
```
sudo docker exec -it kubernetes bash
```

## Step 5:  Setup PipelineAI on Kubernetes
* [Local Laptop](deploy/local.md)
* [AWS CPU](deploy/aws-cpu.md)
* [AWS GPU](deploy/aws-gpu.md)
* [Google Cloud](deploy/google.md)
* [Azure](deploy/azure.md)

Click [HERE](deploy/kubernetes-commands.md) for more Kubernetes commands.

# PipelineAI + GPUs
Click [HERE](gpu/README.md) for More Details on PipelineAI + GPUs.
