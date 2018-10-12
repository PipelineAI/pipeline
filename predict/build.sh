#!/bin/bash -e

docker build -t 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/predict-gpu:1.5.0 -f Dockerfile.gpu .
docker build -t 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/predict-cpu:1.5.0 -f Dockerfile.cpu .

docker tag 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/predict-gpu:1.5.0 gcr.io/flux-capacitor1/pipelineai/predict-gpu:1.5.0
docker tag 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/predict-cpu:1.5.0 gcr.io/flux-capacitor1/pipelineai/predict-cpu:1.5.0

docker tag 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/predict-gpu:1.5.0 pipelineai.azurecr.io/pipelineai/predict-gpu:1.5.0
docker tag 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/predict-cpu:1.5.0 pipelineai.azurecr.io/pipelineai/predict-cpu:1.5.0

docker tag 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/predict-gpu:1.5.0 pipelineai/predict-gpu:1.5.0
docker tag 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/predict-cpu:1.5.0 pipelineai/predict-cpu:1.5.0
