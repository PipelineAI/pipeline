#!/bin/bash

# Args:  
#   $1:  version (ie. v1.3.0)
echo ""
echo "PIPELINE_HOME="$PIPELINE_HOME
echo "version="$1
echo ""

# package
cd $PIPELINE_HOME/package/ubuntu/16.04/ && pwd && sudo docker push fluxcapacitor/package-ubuntu-16.04:$1 
cd $PIPELINE_HOME/package/java/openjdk/1.8/ && pwd && sudo docker push fluxcapacitor/package-java-openjdk-1.8:$1

cd $PIPELINE_HOME/package/spark/2.1.0/ && pwd && sudo docker push fluxcapacitor/package-spark-2.1.0:$1

cd $PIPELINE_HOME/package/kafka/0.11/ && pwd && sudo docker push fluxcapacitor/package-kafka-0.11:$1
cd $PIPELINE_HOME/package/presto/0.167/ && pwd && sudo docker push fluxcapacitor/package-presto-0.167:$1
cd $PIPELINE_HOME/package/apache2/ && pwd && sudo docker push fluxcapacitor/package-apache2:$1

cd $PIPELINE_HOME/package/tensorflow/16d39e9-d690fdd/ && pwd && sudo docker push fluxcapacitor/package-tensorflow-serving-cpu:$1f Dockerfile.serving-cpu .
cd $PIPELINE_HOME/package/tensorflow/16d39e9-d690fdd/ && pwd && sudo docker push fluxcapacitor/package-tensorflow-full-cpu:$1f Dockerfile.full-cpu .
cd $PIPELINE_HOME/package/tensorflow/16d39e9-d690fdd/ && pwd && sudo docker push fluxcapacitor/package-tensorflow-cpu:$1 Dockerfile.cpu .

# apachespark
cd $PIPELINE_HOME/spark/2.1.0/ && pwd && sudo docker push fluxcapacitor/spark-master-2.1.0:$1f Dockerfile.master .
cd $PIPELINE_HOME/spark/2.1.0/ && pwd && sudo docker push fluxcapacitor/spark-worker-2.1.0:$1f Dockerfile.worker .

# cassandra
cd $PIPELINE_HOME/cassandra && pwd && sudo docker push fluxcapacitor/cassandra:$1

# dashboard
cd $PIPELINE_HOME/dashboard/hystrix && pwd && sudo docker push fluxcapacitor/dashboard-hystrix:$1
cd $PIPELINE_HOME/dashboard/turbine && pwd && sudo docker push fluxcapacitor/dashboard-turbine:$1

# elasticsearch
cd $PIPELINE_HOME/elasticsearch/2.3.0 && pwd && sudo docker push fluxcapacitor/elasticsearch-2.3.0:$1

# gpu.ml (actually, cpu)
cd $PIPELINE_HOME/gpu.ml && pwd && sudo docker push fluxcapacitor/gpu-tensorflow-spark:$1f Dockerfile.cpu .

# hdfs
cd $PIPELINE_HOME/hdfs && pwd && sudo docker push fluxcapacitor/hdfs-namenode:$1

# jupyterhub
cd $PIPELINE_HOME/jupyterhub && pwd && sudo docker push fluxcapacitor/jupyterhub:$1

# keyvalue
cd $PIPELINE_HOME/keyvalue/redis && pwd && sudo docker push fluxcapacitor/keyvalue-redis-master:$1

# kibana
cd $PIPELINE_HOME/kibana/4.5.0 && pwd && sudo docker push fluxcapacitor/kibana-4.5.0:$1

# kubernetes
cd $PIPELINE_HOME/kubernetes && pwd && sudo docker push fluxcapacitor/kubernetes:$1

# loadtest
#cd $PIPELINE_HOME/loadtest && pwd && sudo docker push fluxcapacitor/loadtest:$1

# metastore
cd $PIPELINE_HOME/metastore && pwd && sudo docker push fluxcapacitor/metastore-2.1.1:$1

# prediction
#cd $PIPELINE_HOME/prediction/jvm && pwd && sudo docker push fluxcapacitor/prediction-jvm:$1
#cd $PIPELINE_HOME/prediction/python3 && pwd && sudo docker push fluxcapacitor/prediction-python3:$1
#cd $PIPELINE_HOME/prediction/tensorflow && pwd && sudo docker push fluxcapacitor/prediction-tensorflow-cpu:$1f Dockerfile.cpu .

# presto
cd $PIPELINE_HOME/presto/master && pwd && sudo docker push fluxcapacitor/presto-master-0.167:$1
cd $PIPELINE_HOME/presto/worker && pwd && sudo docker push fluxcapacitor/presto-worker-0.167:$1
cd $PIPELINE_HOME/presto/ui && pwd && sudo docker push fluxcapacitor/presto-ui:$1

# airflow
cd $PIPELINE_HOME/airflow && pwd && sudo docker push fluxcapacitor/airflow:$1

# sql
cd $PIPELINE_HOME/sql/mysql && pwd && sudo docker push fluxcapacitor/sql-mysql-master:$1

# stream
#cd $PIPELINE_HOME/stream/kafka/0.10 && pwd && sudo docker push fluxcapacitor/stream-kafka-0.10:$1

# web
#cd $PIPELINE_HOME/web/home && pwd && sudo docker push fluxcapacitor/web-home:$1

# zeppelin
cd $PIPELINE_HOME/zeppelin && pwd && sudo docker push fluxcapacitor/zeppelin:$1

# zookeeper
#cd $PIPELINE_HOME/zookeeper && pwd && sudo docker push fluxcapacitor/zookeeper:$1
