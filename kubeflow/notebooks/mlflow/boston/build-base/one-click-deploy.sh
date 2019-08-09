#!/bin/bash

docker build -t pipelineai/mlflow-boston-base:2.0.0 .
docker push pipelineai/mlflow-boston-base:2.0.0
