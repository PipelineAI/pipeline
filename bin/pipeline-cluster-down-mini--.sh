#!/bin/sh                                                                               

echo '...MySql...'
kubectl delete rc mysql-master
kubectl delete svc mysql-master

echo '...ZooKeeper...'
kubectl delete rc zookeeper
kubectl delete svc zookeeper

echo '...Redis...'
kubectl delete rc redis-master
kubectl delete svc redis-master

echo '...Elasticsearch 2.3.0...'
kubectl delete rc elasticsearch-2-3-0
kubectl delete svc elasticsearch-2-3-0

echo '...Cassandra...'
kubectl delete rc cassandra
kubectl delete svc cassandra

echo '...Spark Master...'
kubectl delete rc spark-master-2-0-1
kubectl delete svc spark-master-2-0-1

echo '...Spark Worker...'
kubectl delete rc spark-worker-2-0-1
kubectl delete svc spark-worker-2-0-1

echo '...Hive Metastore...'
kubectl delete rc metastore-1-2-1
kubectl delete svc metastore-1-2-1

echo '...Zeppelin...'
kubectl delete rc zeppelin-master
kubectl delete svc zeppelin-master

echo '...JupyterHub...'
kubectl delete rc jupyterhub-master
kubectl delete svc jupyterhub-master

echo '...Airflow Scheduler...'
kubectl delete rc airflow
kubectl delete svc airflow

echo '...Presto Master...'
kubectl delete rc presto-master
kubectl delete svc presto-master

echo '...Presto Worker...'
kubectl delete rc presto-worker
kubectl delete svc presto-worker

echo '...Presto AirPal...'
kubectl delete rc airpal
kubectl delete svc airpal

echo '...Kafka - 0.8...'
kubectl delete rc kafka-0-8
kubectl delete svc kafka-0-8

echo '...Kibana - 4.5.0...'
kubectl delete rc kibana-4-5-0
kubectl delete svc kibana-4-5-0 

echo '...Apache - Home...'
kubectl delete rc web-home 
kubectl delete svc web-home

##############
# Predictions
##############
echo '...Prediction - PMML...'                                                          
kubectl delete rc prediction-pmml                                                       
kubectl delete svc prediction-pmml
                                                                                        
echo '...Prediction - Codegen...'                                                       
kubectl delete rc prediction-codegen
kubectl delete svc prediction-codegen                                                    
                                                                                        
echo '...Prediction - Cache...'                                                         
kubectl delete rc prediction-cache
kubectl delete svc prediction-cache
 
echo '...Prediction - Tensorflow...'                                                    
kubectl delete rc prediction-tensorflow
kubectl delete svc prediction-tensorflow

##############
# Dashboards
##############
echo '...Dashboard - Turbine...'
kubectl delete rc turbine
kubectl delete svc turbine

echo '...Dashboard - Hystrix...'
kubectl delete rc hystrix
kubectl delete svc hystrix

echo '...Dashboard - Weavescope...'
kubectl delete rc weavescope-app
kubectl delete ds weavescope-probe
kubectl delete svc weavescope-app
