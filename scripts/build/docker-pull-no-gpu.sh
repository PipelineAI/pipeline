cd $PIPELINE_HOME

# TODO:  
# $1:  version (ie. v1.2.0)

# package.ml
cd $PIPELINE_HOME/package.ml/ubuntu/14.04/ && sudo docker pull fluxcapacitor/package-ubuntu-14.04
cd $PIPELINE_HOME/package.ml/ubuntu/16.04/ && sudo docker pull fluxcapacitor/package-ubuntu-16.04
cd $PIPELINE_HOME/package.ml/java/openjdk/1.8/ && sudo docker pull fluxcapacitor/package-java-openjdk-1.8
cd $PIPELINE_HOME/package.ml/spark/2.1.0/ && sudo docker pull fluxcapacitor/package-spark-2.1.0

cd $PIPELINE_HOME/package.ml/kafka/0.10/ && sudo docker pull fluxcapacitor/package-kafka-0.10
cd $PIPELINE_HOME/package.ml/presto/0.167/ && sudo docker pull fluxcapacitor/package-presto-0.167
cd $PIPELINE_HOME/package.ml/apache2/ && sudo docker pull fluxcapacitor/package-apache2
#cd $PIPELINE_HOME/package.ml/gpu/cuda8/16.04/ && sudo docker pull fluxcapacitor/package-gpu-cuda8-16.04

cd $PIPELINE_HOME/package.ml/tensorflow/2a48110-4d0a571/ && sudo docker pull fluxcapacitor/package-tensorflow-2a48110-4d0a571
cd $PIPELINE_HOME/package.ml/tensorflow/2a48110-4d0a571/ && sudo docker pull fluxcapacitor/package-tensorflow-2a48110-4d0a571-no-avx
#cd $PIPELINE_HOME/package.ml/tensorflow/2a48110-4d0a571/ && sudo docker pull fluxcapacitor/package-tensorflow-2a48110-4d0a571-gpu
#cd $PIPELINE_HOME/package.ml/tensorflow/2a48110-4d0a571/ && sudo docker pull fluxcapacitor/package-tensorflow-2a48110-4d0a571-gpu-no-avx

# apachespark.ml
cd $PIPELINE_HOME/apachespark.ml/2.1.0/ && sudo docker pull fluxcapacitor/apachespark-master-2.1.0
cd $PIPELINE_HOME/apachespark.ml/2.1.0/ && sudo docker pull fluxcapacitor/apachespark-worker-2.1.0

# cassandra.ml
cd $PIPELINE_HOME/cassandra.ml && sudo docker pull fluxcapacitor/cassandra

# clustered.ml
cd $PIPELINE_HOME/clustered.ml/tensorflow && sudo docker pull fluxcapacitor/clustered-tensorflow
#cd $PIPELINE_HOME/clustered.ml/tensorflow && sudo docker pull fluxcapacitor/clustered-tensorflow-gpu

# dashboard.ml
cd $PIPELINE_HOME/dashboard.ml/hystrix && sudo docker pull fluxcapacitor/dashboard-hystrix
cd $PIPELINE_HOME/dashboard.ml/turbine && sudo docker pull fluxcapacitor/dashboard-turbine

# elasticsearch.ml
cd $PIPELINE_HOME/elasticsearch.ml/2.3.0 && sudo docker pull fluxcapacitor/elasticsearch-2.3.0

# gpu.ml
cd $PIPELINE_HOME/gpu.ml && sudo docker pull fluxcapacitor/gpu-tensorflow-spark

# hdfs.ml
cd $PIPELINE_HOME/hdfs.ml && sudo docker pull fluxcapacitor/hdfs-namenode

# jupyterhub.ml
cd $PIPELINE_HOME/jupyterhub.ml && sudo docker pull fluxcapacitor/jupyterhub

# keyvalue.ml
cd $PIPELINE_HOME/keyvalue.ml/redis && sudo docker pull fluxcapacitor/keyvalue-redis-master

# kibana.ml
cd $PIPELINE_HOME/kibana.ml/4.5.0 && sudo docker pull fluxcapacitor/kibana-4.5.0

# kubernetes.ml
cd $PIPELINE_HOME/kubernetes.ml && sudo docker pull fluxcapacitor/kubernetes

# loadtest.ml
cd $PIPELINE_HOME/loadtest.ml && sudo docker pull fluxcapacitor/loadtest

# metastore.ml
cd $PIPELINE_HOME/metastore.ml && sudo docker pull fluxcapacitor/metastore-2.1.1

# prediction.ml
cd $PIPELINE_HOME/prediction.ml/jvm && sudo docker pull fluxcapacitor/prediction-jvm
cd $PIPELINE_HOME/prediction.ml/python3 && sudo docker pull fluxcapacitor/prediction-python3
cd $PIPELINE_HOME/prediction.ml/tensorflow && sudo docker pull fluxcapacitor/prediction-tensorflow
cd $PIPELINE_HOME/prediction.ml/tensorflow && sudo docker pull fluxcapacitor/prediction-tensorflow-no-avx
#cd $PIPELINE_HOME/prediction.ml/tensorflow && sudo docker pull fluxcapacitor/prediction-tensorflow-gpu
#cd $PIPELINE_HOME/prediction.ml/tensorflow && sudo docker pull fluxcapacitor/prediction-tensorflow-gpu-no-avx

# presto.ml
cd $PIPELINE_HOME/presto.ml/master && sudo docker pull fluxcapacitor/presto-master-0.167
cd $PIPELINE_HOME/presto.ml/worker && sudo docker pull fluxcapacitor/presto-worker-0.167
cd $PIPELINE_HOME/presto.ml/ui && sudo docker pull fluxcapacitor/presto-ui

# scheduler.ml
cd $PIPELINE_HOME/scheduler.ml/airflow && sudo docker pull fluxcapacitor/scheduler-airflow

# sql.ml
cd $PIPELINE_HOME/sql.ml/mysql && sudo docker pull fluxcapacitor/sql-mysql-master

# stream.ml
cd $PIPELINE_HOME/stream.ml/kafka/0.10 && sudo docker pull fluxcapacitor/stream-kafka-0.10

# web.ml
cd $PIPELINE_HOME/web.ml/home && sudo docker pull fluxcapacitor/web-home

# zeppelin.ml
cd $PIPELINE_HOME/zeppelin.ml && sudo docker pull fluxcapacitor/zeppelin

# zookeeper.ml
cd $PIPELINE_HOME/zookeeper.ml && sudo docker pull fluxcapacitor/zookeeper
