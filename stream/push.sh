#!/bin/bash -e

aws ecr get-login --no-include-email | bash

#docker push 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/stream-gpu:1.5.0
#docker push 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/stream-cpu:1.5.0

#docker push gcr.io/flux-capacitor1/pipelineai/stream-gpu:1.5.0
#docker push gcr.io/flux-capacitor1/pipelineai/stream-cpu:1.5.0

#docker push pipelineai.azurecr.io/pipelineai/stream-gpu:1.5.0
#docker push pipelineai.azurecr.io/pipelineai/stream-cpu:1.5.0

docker push pipelineai/stream-gpu:1.5.0
docker push pipelineai/stream-cpu:1.5.0
