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
