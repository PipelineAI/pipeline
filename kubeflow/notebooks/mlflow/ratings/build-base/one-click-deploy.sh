#!/bin/bash

docker build -t pipelineai/mlflow-ratings-base:2.0.0 .
docker push pipelineai/mlflow-ratings-base:2.0.0
