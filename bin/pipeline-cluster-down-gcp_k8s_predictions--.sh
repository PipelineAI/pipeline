#!/bin/sh                                                                               

echo '...Prediction - PMML...'                                                          
kubectl delete rc prediction-pmml                                                       
kubectl delete svc prediction-pmml
                                                                                        
echo '...Prediction - Codegen...'                                                       
kubectl delete rc prediction-codegen
kubectl delete svc prediction-codegen                                                    
                                                                                        
echo '...Prediction - KeyValue...'                                                         
kubectl delete rc prediction-keyvalue
kubectl delete svc prediction-keyvalue
 
echo '...Prediction - Tensorflow...'                            
kubectl delete rc prediction-tensorflow
kubectl delete svc prediction-tensorflow

echo '...Dashboard - Tubine...'
kubectl delete rc turbine
kubectl delete svc turbine

echo '...Dashboard - Weavescope...'
kubectl delete rc weavescope-app
kubectl delete ds weavescope-probe
kubectl delete svc weavescope-app
