# Setup PipelineAI 

## Step 1:  [Download Docker](https://www.docker.com/community-edition) v1.17+ for Mac, Windows, or Linux
Note:  Docker must be at least v1.17

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
* [Local Laptop](kubernetes-local.md)
* [AWS CPU](kubernetes-aws-cpu.md)
* [AWS GPU](kubernetes-aws-gpu.md)
* [Google Cloud](kubernetes-gcp.md)
* [Azure](kubernetes-azure.md)

Click [HERE](kubernetes-commands.md) for more Kubernetes commands.
