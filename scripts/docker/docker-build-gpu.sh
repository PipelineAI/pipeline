#!/bin/bash

# Args:
#   $1:  version (ie. v1.2.0)
#   $2:  --no-cache

echo ""
echo "PIPELINE_HOME="$PIPELINE_HOME
echo "version="$1
echo ""

cd $PIPELINE_HOME/package.ml/gpu/cuda8/16.04/ && pwd && sudo docker build $2 -q -t fluxcapacitor/package-gpu-cuda8-16.04:$1 .

cd $PIPELINE_HOME/package.ml/tensorflow/072355e-a44fd6d/ && pwd && sudo docker build $2 -q -t fluxcapacitor/package-tensorflow-072355e-a44fd6d-gpu:$1 -f Dockerfile.gpu .
cd $PIPELINE_HOME/package.ml/tensorflow/072355e-a44fd6d/ && pwd && sudo docker build $2 -q -t fluxcapacitor/package-tensorflow-072355e-a44fd6d-gpu-no-avx:$1 -f Dockerfile.gpu-no-avx .

#cd $PIPELINE_HOME/package.ml/tensorflow/7fda1bb-6c096a4/ && pwd && sudo docker build $2 -q -t fluxcapacitor/package-tensorflow-7fda1bb-6c096a4-gpu:$1 -f Dockerfile.gpu .
#cd $PIPELINE_HOME/package.ml/tensorflow/7fda1bb-6c096a4/ && pwd && sudo docker build $2 -q -t fluxcapacitor/package-tensorflow-7fda1bb-6c096a4-gpu-no-avx:$1 -f Dockerfile.gpu-no-avx .

cd $PIPELINE_HOME/clustered.ml/tensorflow && pwd && sudo docker build $2 -q -t fluxcapacitor/clustered-tensorflow-gpu:$1 -f Dockerfile.gpu .

cd $PIPELINE_HOME/gpu.ml && pwd && sudo docker build $2 -q -t fluxcapacitor/gpu-tensorflow-spark:$1 -f Dockerfile.gpu .
cd $PIPELINE_HOME/gpu.ml && pwd && sudo docker build $2 -q -t fluxcapacitor/gpu-tensorflow-spark-no-avx:$1 -f Dockerfile.gpu-no-avx .

cd $PIPELINE_HOME/prediction.ml/tensorflow && pwd && sudo docker build $2 -q -t fluxcapacitor/prediction-tensorflow-gpu:$1 -f Dockerfile.gpu .
cd $PIPELINE_HOME/prediction.ml/tensorflow && pwd && sudo docker build $2 -q -t fluxcapacitor/prediction-tensorflow-gpu-no-avx:$1 -f Dockerfile.gpu-no-avx .
