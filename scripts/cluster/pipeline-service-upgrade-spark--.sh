#!/bin/sh

kubectl rolling-update spark-worker-2-1-0 -f $PIPELINE_HOME/apachespark.ml/spark-worker-rc-8.yaml
