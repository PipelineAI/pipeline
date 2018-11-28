#!/bin/bash

# Note:  You need to be up 1 directory when you run this!
docker build -t pipelineai/predict-base:master -f Dockerfile.base .
