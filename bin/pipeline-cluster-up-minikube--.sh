#!/bin/sh

echo '...Spark - Master...'
kubectl create -f $PIPELINE_HOME/apachespark.ml/spark-master-rc.yaml
kubectl create -f $PIPELINE_HOME/apachespark.ml/spark-master-svc.yaml

echo '...Spark - Worker...'
kubectl create -f $PIPELINE_HOME/apachespark.ml/spark-worker-rc.yaml
kubectl create -f $PIPELINE_HOME/apachespark.ml/spark-worker-svc.yaml

echo '...JupyterHub...'
kubectl create -f $PIPELINE_HOME/jupyterhub.ml/jupyterhub-rc.yaml
kubectl create -f $PIPELINE_HOME/jupyterhub.ml/jupyterhub-svc.yaml

##############
# Predictions
##############
echo '...Prediction - PMML...'
kubectl create -f $PIPELINE_HOME/prediction.ml/pmml-rc.yaml
kubectl create -f $PIPELINE_HOME/prediction.ml/pmml-svc.yaml
kubectl describe svc prediction-pmml

##############
# Dashboards
##############
echo '...Dashboard - Turbine...'
kubectl create -f $PIPELINE_HOME/dashboard.ml/turbine-rc.yaml
kubectl create -f $PIPELINE_HOME/dashboard.ml/turbine-svc.yaml
kubectl describe svc turbine

echo '...Dashboard - Hystrix...'
kubectl create -f $PIPELINE_HOME/dashboard.ml/hystrix-rc.yaml
kubectl create -f $PIPELINE_HOME/dashboard.ml/hystrix-svc.yaml

echo '...Dashboard - Weavescope...'
kubectl create -f $PIPELINE_HOME/dashboard.ml/weavescope/weavescope.yaml
kubectl describe svc weavescope-app
