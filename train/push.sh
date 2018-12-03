#!/bin/bash -e

docker push 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/train-gpu:1.5.0
docker push 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/train-cpu:1.5.0

#docker push gcr.io/flux-capacitor1/pipelineai/train-gpu:1.5.0
#docker push gcr.io/flux-capacitor1/pipelineai/train-cpu:1.5.0

#docker push pipelineai.azurecr.io/pipelineai/train-gpu:1.5.0
#docker push pipelineai.azurecr.io/pipelineai/train-cpu:1.5.0

docker push pipelineai/train-gpu:1.5.0
docker push pipelineai/train-cpu:1.5.0
