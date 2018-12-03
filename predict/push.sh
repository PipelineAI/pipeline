#!/bin/bash -e

aws ecr get-login --no-include-email | bash

docker push 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/predict-gpu:1.5.0
docker push 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/predict-cpu:1.5.0

#docker push gcr.io/flux-capacitor1/pipelineai/predict-gpu:1.5.0
#docker push gcr.io/flux-capacitor1/pipelineai/predict-cpu:1.5.0

#docker push pipelineai.azurecr.io/pipelineai/predict-gpu:1.5.0
#docker push pipelineai.azurecr.io/pipelineai/predict-cpu:1.5.0

docker push pipelineai/predict-gpu:1.5.0
docker push pipelineai/predict-cpu:1.5.0
