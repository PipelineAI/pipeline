# Amazon Web Services (AWS) Elastic Container Service (ECS) Runbook

## [Build and Test](https://github.com/PipelineAI/predict) Your Model as a Docker Image

## Push Docker Image to Docker Registry (ie. DockerHub)
```
docker images   <-- Find your image
```
```
docker login    <-- Login to DockerHub
```
```
docker push <docker-registry-url>/<docker-org>/<docker-image-name>:<docker-image-tag>
```

## Setup ECS Cluster through AWS CLI 
Configure Docker Image, RAM, and CPU

![AWS ECS Task Definition](http://pipeline.ai/assets/img/predict-aws-ecs-task-definition.png) 

Configure Ports and Environment Variables

![AWS ECS Task Definition Details](http://pipeline.ai/assets/img/predict-aws-ecs-task-definition-details.png)
