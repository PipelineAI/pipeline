cd $PIPELINE_HOME

# TODO:  
# $1:  version (ie. v1.2.0)

# package.ml
sudo docker push fluxcapacitor/package-gpu-cuda8-16.04:$1

sudo docker push fluxcapacitor/package-tensorflow-072355e-a44fd6d-gpu:$1
sudo docker push fluxcapacitor/package-tensorflow-072355e-a44fd6d-gpu-no-avx:$1

sudo docker push fluxcapacitor/gpu-tensorflow-spark:$1
sudo docker push fluxcapacitor/gpu-tensorflow-spark-no-avx:$1

# clustered.ml
sudo docker push fluxcapacitor/clustered-tensorflow-gpu:$1

# prediction.ml
sudo docker push fluxcapacitor/prediction-tensorflow-gpu:$1
sudo docker push fluxcapacitor/prediction-tensorflow-gpu-no-avx:$1
