cd $PIPELINE_HOME

# TODO:  
# $1:  version (ie. v1.2.0)

# package.ml
cd $PIPELINE_HOME/package.ml/gpu/cuda8/16.04/ && pwd && sudo docker push fluxcapacitor/package-gpu-cuda8-16.04:$1

cd $PIPELINE_HOME/package.ml/tensorflow/7a7fe93-4c0052d/ && pwd && sudo docker push fluxcapacitor/package-tensorflow-full-gpu:$1
cd $PIPELINE_HOME/package.ml/tensorflow/7a7fe93-4c0052d/ && pwd && sudo docker push fluxcapacitor/package-tensorflow-gpu:$1

cd $PIPELINE_HOME/gpu.ml && pwd && sudo docker push fluxcapacitor/gpu-tensorflow-spark:$1

# clustered.ml
#sudo docker push fluxcapacitor/clustered-tensorflow-gpu:$1

# prediction.ml
cd $PIPELINE_HOME/prediction.ml/tensorflow && pwd && sudo docker push fluxcapacitor/prediction-tensorflow-gpu:$1
