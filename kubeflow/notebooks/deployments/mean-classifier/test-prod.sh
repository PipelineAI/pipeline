#!/bin/bash -e

curl https://community.cloud.pipeline.ai/seldon/kubeflow/mean_classifier/api/v0.1/predictions -d '{"data":{"ndarray":[[1]]}}' -H "Content-Type: application/json"

