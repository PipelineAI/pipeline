#!/bin/sh

echo '...Starting Load Test...'
kubectl create -f $PIPELINE_HOME/loadtest.ml/loadtest-rc.yaml
