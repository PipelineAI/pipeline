#!/bin/bash -e
docker build -f Dockerfile -t pipelineai/pytorch-distributed-mnist:2.0.0 .
docker push pipelineai/pytorch-distributed-mnist:2.0.0
