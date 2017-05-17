cd $PIPELINE_HOME

# TODO:  
# $1:  version (ie. v1.2.0)

# package.ml
cd $PIPELINE_HOME/package.ml/gpu/cuda8/16.04/ && sudo docker pull fluxcapacitor/package-gpu-cuda8-16.04

cd $PIPELINE_HOME/package.ml/tensorflow/2a48110-4d0a571/ && sudo docker pull fluxcapacitor/package-tensorflow-2a48110-4d0a571-gpu
cd $PIPELINE_HOME/package.ml/tensorflow/2a48110-4d0a571/ && sudo docker pull fluxcapacitor/package-tensorflow-2a48110-4d0a571-gpu-no-avx

# clustered.ml
cd $PIPELINE_HOME/clustered.ml/tensorflow && sudo docker pull fluxcapacitor/clustered-tensorflow-gpu

# prediction.ml
cd $PIPELINE_HOME/prediction.ml/tensorflow && sudo docker pull fluxcapacitor/prediction-tensorflow-gpu
cd $PIPELINE_HOME/prediction.ml/tensorflow && sudo docker pull fluxcapacitor/prediction-tensorflow-gpu-no-avx
