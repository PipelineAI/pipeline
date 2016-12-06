#!/bin/sh

echo '...Prediction - PMML...'
kubectl create -f $PIPELINE_HOME/prediction.ml/pmml-rc.yaml
kubectl create -f $PIPELINE_HOME/prediction.ml/pmml-svc.yaml
kubectl describe svc prediction-pmml

echo '...Prediction - Codegen...'                                                       
kubectl create -f $PIPELINE_HOME/prediction.ml/codegen-rc.yaml                                      
kubectl create -f $PIPELINE_HOME/prediction.ml/codegen-svc.yaml
kubectl describe svc prediction-codegen                                                  
                                                                                        
echo '...Prediction - Tensorflow...'                                                    
kubectl create -f $PIPELINE_HOME/prediction.ml/tensorflow-rc.yaml                                   
kubectl create -f $PIPELINE_HOME/prediction.ml/tensorflow-svc.yaml
kubectl describe svc prediction-tensorflow                                                  
                                                                                        
echo '...Prediction - Cache...'
kubectl create -f $PIPELINE_HOME/prediction.ml/cache-rc.yaml                                        
kubectl create -f $PIPELINE_HOME/prediction.ml/cache-svc.yaml
kubectl describe svc prediction-cache   

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
