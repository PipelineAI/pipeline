#!/bin/sh

echo '...Prediction - PMML...'
kubectl create -f $PIPELINE_HOME/prediction.ml/pmml-rc.yaml
kubectl create -f $PIPELINE_HOME/prediction.ml/pmml-svc.yaml
kubectl describe svc prediction-pmml

echo '...Prediction - Codegen...'                                                       
kubectl create -f $PIPELINE_HOME/prediction.ml/codegen-rc.yaml                                      
kubectl create -f $PIPELINE_HOME/prediction.ml/codegen-svc.yaml
kubectl describe svc prediction-codegen                                                  

echo '...Prediction - KeyValue...'
kubectl create -f $PIPELINE_HOME/prediction.ml/keyvalue-rc.yaml
kubectl create -f $PIPELINE_HOME/prediction.ml/keyvalue-svc.yaml
kubectl describe svc prediction-keyvalue
                                                                                        
echo '...Prediction - Tensorflow...'                                                    
kubectl create -f $PIPELINE_HOME/prediction.ml/tensorflow-rc.yaml                                   
kubectl create -f $PIPELINE_HOME/prediction.ml/tensorflow-svc.yaml
kubectl describe svc prediction-tensorflow                                                  
                                                                                        
##############
# Dashboards
##############
echo '...Dashboard - Weavescope...'
kubectl create -f $PIPELINE_HOME/dashboard.ml/weavescope/weavescope.yaml
kubectl describe svc weavescope-app

echo '...Dashboard - Turbine...'
kubectl create -f $PIPELINE_HOME/dashboard.ml/turbine-rc.yaml
kubectl create -f $PIPELINE_HOME/dashboard.ml/turbine-svc.yaml
kubectl describe svc turbine
