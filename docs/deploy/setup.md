## Step 1:  Download Latest [Docker Community Edition](https://www.docker.com/community-edition) for Mac, Windows, or Linux

## Step 2:  Start the PipelineAI CLI Docker Container
```
docker run --privileged -v /var/run/docker.sock:/var/run/docker.sock --name cli-pipeline -itd pipelineai/cli-pipeline:master
```
Make sure you include the `cli-pipeline:master` tag as part of the DockerHub reference above. ^^

## Step 3:  Shell into Docker Container to Setup Kubernetes Cluster
```
sudo docker exec -it cli-pipeline bash
```

## Step 5:  Setup PipelineAI on AWS, Google Cloud, or Azure
* [AWS CPU](aws-cpu.md)
* [AWS GPU](aws-gpu.md)
* [Google Cloud](google.md)
* [Azure](azure.md)
