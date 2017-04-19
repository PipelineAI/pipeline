cd $PIPELINE_HOME

# TODO:  
# $1:  version (ie. v0.7)
# $2:  --no-cache

# package.ml
cd $PIPELINE_HOME/package.ml/ubuntu/14.04/ && sudo docker build -q -t fluxcapacitor/package-ubuntu-14.04:$1 .
cd $PIPELINE_HOME/package.ml/ubuntu/16.04/ && sudo docker build -q -t fluxcapacitor/package-ubuntu-16.04:$1 .
cd $PIPELINE_HOME/package.ml/java/openjdk/1.8/ && sudo docker build -q -t fluxcapacitor/package-java-openjdk-1.8:$1 .
cd $PIPELINE_HOME/package.ml/spark/2.0.1/ && sudo docker build -q -t fluxcapacitor/package-spark-2.0.1:$1 .
cd $PIPELINE_HOME/package.ml/spark/2.1.0/ && sudo docker build -q -t fluxcapacitor/package-spark-2.1.0:$1 .

cd $PIPELINE_HOME/package.ml/kafka/0.8/ && sudo docker build -q -t fluxcapacitor/package-kafka-0.8:$1 .
cd $PIPELINE_HOME/package.ml/kafka/0.10/ && sudo docker build -q -t fluxcapacitor/package-kafka-0.10:$1 .
cd $PIPELINE_HOME/package.ml/presto/0.167/ && sudo docker build -q -t fluxcapacitor/package-presto-0.167:$1 .
cd $PIPELINE_HOME/package.ml/apache2/ && sudo docker build -q -t fluxcapacitor/package-apache2:$1 .
cd $PIPELINE_HOME/package.ml/gpu/cuda8/16.04/ && sudo docker build -q -t fluxcapacitor/package-gpu-cuda8-16.04:$1 .
cd $PIPELINE_HOME/package.ml/tensorflow/1.0/ && sudo docker build -q -t fluxcapacitor/package-tensorflow-serving-1.0:$1 -f Dockerfile.serving .
cd $PIPELINE_HOME/package.ml/tensorflow/1.0/ && sudo docker build -q -t fluxcapacitor/package-tensorflow-serving-1.0-gpu:$1 -f Dockerfile.serving.gpu .
cd $PIPELINE_HOME/package.ml/tensorflow/1.0/ && sudo docker build -q -t fluxcapacitor/package-tensorflow-1.0:$1 .
cd $PIPELINE_HOME/package.ml/tensorflow/1.0/ && sudo docker build -q -t fluxcapacitor/package-tensorflow-1.0-gpu:$1 -f Dockerfile.gpu .

cd $PIPELINE_HOME/package.ml/tensorflow/2a48110-4d0a571/ && sudo docker build -q -t fluxcapacitor/package-tensorflow-2a48110-4d0a571-gpu:$1 -f Dockerfile.gpu .

# apachespark.ml
cd $PIPELINE_HOME/apachespark.ml/2.0.1/ && sudo docker build -q -t fluxcapacitor/apachespark-master-2.0.1:$1 -f Dockerfile.master .
cd $PIPELINE_HOME/apachespark.ml/2.0.1/ && sudo docker build -q -t fluxcapacitor/apachespark-worker-2.0.1:$1 -f Dockerfile.worker .
cd $PIPELINE_HOME/apachespark.ml/2.1.0/ && sudo docker build -q -t fluxcapacitor/apachespark-master-2.1.0:$1 -f Dockerfile.master .
cd $PIPELINE_HOME/apachespark.ml/2.1.0/ && sudo docker build -q -t fluxcapacitor/apachespark-worker-2.1.0:$1 -f Dockerfile.worker .

# cassandra.ml
cd $PIPELINE_HOME/cassandra.ml && sudo docker build -q -t fluxcapacitor/cassandra:$1 .

# clustered.ml
cd $PIPELINE_HOME/clustered.ml/tensorflow && sudo docker build -q -t fluxcapacitor/clustered-tensorflow:$1 .
cd $PIPELINE_HOME/clustered.ml/tensorflow && sudo docker build -q -t fluxcapacitor/clustered-tensorflow-gpu:$1 -f Dockerfile.gpu .

# dashboard.ml
cd $PIPELINE_HOME/dashboard.ml/hystrix && sudo docker build -q -t fluxcapacitor/dashboard-hystrix:$1 .
cd $PIPELINE_HOME/dashboard.ml/turbine && sudo docker build -q -t fluxcapacitor/dashboard-turbine:$1 .

# education.ml
#cd $PIPELINE_HOME/education.ml && sudo docker build -q -t fluxcapacitor/pipeline:$1 .
#cd $PIPELINE_HOME/education.ml/serving && sudo docker build -q -t fluxcapacitor/education-serving:$1 .

# elasticsearch.ml
cd $PIPELINE_HOME/elasticsearch.ml/2.3.0 && sudo docker build -q -t fluxcapacitor/elasticsearch-2.3.0:$1 .

# gpu.ml
cd $PIPELINE_HOME/gpu.ml && sudo docker build -q -t fluxcapacitor/gpu-tensorflow-spark:$1 .

# hdfs.ml
cd $PIPELINE_HOME/hdfs.ml && sudo docker build -q -t fluxcapacitor/hdfs-namenode:$1 .

# jupyterhub.ml
cd $PIPELINE_HOME/jupyterhub.ml && sudo docker build -q -t fluxcapacitor/jupyterhub:$1 .

# keyvalue.ml
cd $PIPELINE_HOME/keyvalue.ml/redis && sudo docker build -q -t fluxcapacitor/keyvalue-redis-master:$1 .

# kibana.ml
cd $PIPELINE_HOME/kibana.ml/4.5.0 && sudo docker build -q -t fluxcapacitor/kibana-4.5.0:$1 .

# kubernetes.ml
cd $PIPELINE_HOME/kubernetes.ml && sudo docker build -q -t fluxcapacitor/kubernetes:$1 .

# loadtest.ml
cd $PIPELINE_HOME/loadtest.ml && sudo docker build -q -t fluxcapacitor/loadtest:$1 .

# metastore.ml
cd $PIPELINE_HOME/metastore.ml && sudo docker build -q -t fluxcapacitor/metastore-2.1.1:$1 .

# prediction.ml
cd $PIPELINE_HOME/prediction.ml/java && sudo docker build -q -t fluxcapacitor/prediction-java:$1 .
cd $PIPELINE_HOME/prediction.ml/keyvalue && sudo docker build -q -t fluxcapacitor/prediction-keyvalue:$1 .
cd $PIPELINE_HOME/prediction.ml/pmml && sudo docker build -q -t fluxcapacitor/prediction-pmml:$1 .
cd $PIPELINE_HOME/prediction.ml/python && sudo docker build -q -t fluxcapacitor/prediction-python:$1 .
cd $PIPELINE_HOME/prediction.ml/spark && sudo docker build -q -t fluxcapacitor/prediction-spark:$1 .
cd $PIPELINE_HOME/prediction.ml/tensorflow && sudo docker build -q -t fluxcapacitor/prediction-tensorflow:$1 .
cd $PIPELINE_HOME/prediction.ml/tensorflow && sudo docker build -q -t fluxcapacitor/prediction-tensorflow-gpu:$1 -f Dockerfile.gpu .

# presto.ml
cd $PIPELINE_HOME/presto.ml/presto-master && sudo docker build -q -t fluxcapacitor/presto-master-0.167:$1 .
cd $PIPELINE_HOME/presto.ml/presto-worker && sudo docker build -q -t fluxcapacitor/presto-worker-0.167:$1 .
cd $PIPELINE_HOME/presto.ml/presto-ui && sudo docker build -q -t fluxcapacitor/presto-ui:$1 .

# scheduler.m l
cd $PIPELINE_HOME/scheduler.ml/airflow && sudo docker build -q -t fluxcapacitor/scheduler-airflow:$1 .

# sql.ml
cd $PIPELINE_HOME/sql.ml/mysql && sudo docker build -q -t fluxcapacitor/sql-mysql-master:$1 .

# stream.ml
cd $PIPELINE_HOME/stream.ml/kafka/0.8 && sudo docker build -q -t fluxcapacitor/stream-kafka-0.8:$1 .
cd $PIPELINE_HOME/stream.ml/kafka/0.10 && sudo docker build -q -t fluxcapacitor/stream-kafka-0.10:$1 .

# web.ml
cd $PIPELINE_HOME/web.ml/home && sudo docker build -q -t fluxcapacitor/web-home:$1 .
cd $PIPELINE_HOME/web.ml/sparkafterdark && sudo docker build -q -t fluxcapacitor/web-sparkafterdark:$1 .

# zeppelin.ml
cd $PIPELINE_HOME/zeppelin.ml && sudo docker build -q -t fluxcapacitor/zeppelin:$1 .

# zookeeper.ml
cd $PIPELINE_HOME/zookeeper.ml && sudo docker build -q -t fluxcapacitor/zookeeper:$1 .
