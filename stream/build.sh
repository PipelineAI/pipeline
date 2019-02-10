#!/bin/bash -e

aws ecr get-login --no-include-email | bash

#docker build -t 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/stream-gpu:1.5.0 -f Dockerfile.gpu .
#docker build -t 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/stream-cpu:1.5.0 -f Dockerfile.cpu .

docker build -t pipelineai/stream-gpu:1.5.0 -f Dockerfile.gpu .
docker build -t pipelineai/stream-cpu:1.5.0 -f Dockerfile.cpu .

#docker tag 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/stream-gpu:1.5.0 pipelineai/stream-gpu:1.5.0
#docker tag 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/stream-cpu:1.5.0 pipelineai/stream-cpu:1.5.0

#docker tag 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/stream-gpu:1.5.0 gcr.io/flux-capacitor1/pipelineai/stream-gpu:1.5.0
#docker tag 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/stream-gpu:1.5.0 gcr.io/flux-capacitor1/pipelineai/stream-cpu:1.5.0

#docker tag 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/stream-gpu:1.5.0 pipelineai.azurecr.io/pipelineai/stream-gpu:1.5.0
#docker tag 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/stream-cpu:1.5.0 pipelineai.azurecr.io/pipelineai/stream-cpu:1.5.0

