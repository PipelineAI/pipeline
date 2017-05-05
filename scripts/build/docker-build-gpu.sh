#!/bin/bash

cd $PIPELINE_HOME/package.ml/gpu/cuda8/16.04/ && sudo docker build $2 -t fluxcapacitor/package-gpu-cuda8-16.04:$1 .
cd $PIPELINE_HOME/package.ml/tensorflow/2a48110-4d0a571/ && sudo docker build $2 -t fluxcapacitor/package-tensorflow-2a48110-4d0a571-gpu:$1 -f Dockerfile.gpu .
cd $PIPELINE_HOME/clustered.ml/tensorflow && sudo docker build $2 -t fluxcapacitor/clustered-tensorflow-gpu:$1 -f Dockerfile.gpu .
cd $PIPELINE_HOME/gpu.ml && sudo docker build $2 -t fluxcapacitor/gpu-tensorflow-spark:$1 .
cd $PIPELINE_HOME/prediction.ml/tensorflow && sudo docker build $2 -t fluxcapacitor/prediction-tensorflow-gpu:$1 -f Dockerfile.gpu .
