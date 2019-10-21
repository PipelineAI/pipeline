#!/bin/bash -e

docker build -t antjebarth/doppelganger-train:1.0.0 .
docker push antjebarth/doppelganger-train:1.0.0

kubectl create -f doppelganger-train-deploy.yaml

