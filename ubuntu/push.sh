#!/bin/bash -e

# This is a case where we *do* neet to update the public docker repo
# 1. We want people to use this container - to more-easily adopt our platform
# 2. But we still push to our private repo in order to spin up instances more-quickly
#    and to not rely on DockerHub (SLOOOW)
# 3. Only our private scripts in product/ should use the private repo
#    and only for the performance reasons described above
docker push 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/ubuntu-16.04-gpu:1.5.0
docker push 954636985443.dkr.ecr.us-west-2.amazonaws.com/pipelineai/ubuntu-16.04-cpu:1.5.0

docker push gcr.io/flux-capacitor1/pipelineai/ubuntu-16.04-gpu:1.5.0
docker push gcr.io/flux-capacitor1/pipelineai/ubuntu-16.04-cpu:1.5.0

docker push pipelineai.azurecr.io/pipelineai/ubuntu-16.04-gpu:1.5.0
docker push pipelineai.azurecr.io/pipelineai/ubuntu-16.04-cpu:1.5.0

docker push pipelineai/ubuntu-16.04-gpu:1.5.0
docker push pipelineai/ubuntu-16.04-cpu:1.5.0
