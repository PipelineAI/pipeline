#!/bin/bash -e

#aws ecr get-login --no-include-email | bash

#docker push 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/ubuntu-16.04-gpu:1.5.0
#docker push 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/ubuntu-16.04-cpu:1.5.0

docker push pipelineai/ubuntu-16.04-gpu:1.5.0
docker push pipelineai/ubuntu-16.04-cpu:1.5.0

#docker push gcr.io/flux-capacitor1/pipelineai/ubuntu-16.04-gpu:1.5.0
#docker push gcr.io/flux-capacitor1/pipelineai/ubuntu-16.04-cpu:1.5.0

#docker push pipelineai.azurecr.io/pipelineai/ubuntu-16.04-gpu:1.5.0
#docker push pipelineai.azurecr.io/pipelineai/ubuntu-16.04-cpu:1.5.0
