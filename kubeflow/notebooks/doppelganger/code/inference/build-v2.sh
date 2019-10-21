#!/bin/bash -e

docker build -t antjebarth/doppelganger-predict:2.0.0 -f Dockerfile-v2 .
docker push antjebarth/doppelganger-predict:2.0.0

#kubectl create -f doppelganger-predict-deploy-ab-test.yaml
