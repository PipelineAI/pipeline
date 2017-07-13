#!/bin/bash

echo "model_type=$PIO_MODEL_TYPE"
echo "model_name=$PIO_MODEL_NAME"

docker build -t fluxcapacitor/deploy-train-$PIO_MODEL_TYPE-$PIO_MODEL_NAME-cpu:master \
  --build-arg model_type=$PIO_MODEL_TYPE \
  --build-arg model_name=$PIO_MODEL_NAME -f Dockerfile.train-cpu .
