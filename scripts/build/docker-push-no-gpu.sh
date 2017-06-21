cd $PIPELINE_HOME

# TODO:  
# $1:  version (ie. v1.2.0)


# package.ml
cd $PIPELINE_HOME/package.ml/ubuntu/14.04/ && pwd && sudo docker push fluxcapacitor/package-ubuntu-14.04:$1
cd $PIPELINE_HOME/package.ml/ubuntu/16.04/ && pwd && sudo docker push fluxcapacitor/package-ubuntu-16.04:$1
cd $PIPELINE_HOME/package.ml/java/openjdk/1.8/ && pwd && sudo docker push fluxcapacitor/package-java-openjdk-1.8:$1
cd $PIPELINE_HOME/package.ml/spark/2.1.0/ && pwd && sudo docker push fluxcapacitor/package-spark-2.1.0:$1

cd $PIPELINE_HOME/package.ml/kafka/0.10/ && pwd && sudo docker push fluxcapacitor/package-kafka-0.10:$1
cd $PIPELINE_HOME/package.ml/presto/0.167/ && pwd && sudo docker push fluxcapacitor/package-presto-0.167:$1
cd $PIPELINE_HOME/package.ml/apache2/ && pwd && sudo docker push fluxcapacitor/package-apache2:$1
#cd $PIPELINE_HOME/package.ml/gpu/cuda8/16.04/ && pwd && sudo docker push fluxcapacitor/package-gpu-cuda8-16.04:$1

cd $PIPELINE_HOME/package.ml/tensorflow/7a7fe93-4c0052d/ && pwd && sudo docker push fluxcapacitor/package-tensorflow-full-cpu:$1
cd $PIPELINE_HOME/package.ml/tensorflow/7a7fe93-4c0052d/ && pwd && sudo docker push fluxcapacitor/package-tensorflow-cpu:$1

# apachespark.ml
cd $PIPELINE_HOME/apachespark.ml/2.1.0/ && pwd && sudo docker push fluxcapacitor/apachespark-master-2.1.0:$1
cd $PIPELINE_HOME/apachespark.ml/2.1.0/ && pwd && sudo docker push fluxcapacitor/apachespark-worker-2.1.0:$1

# cassandra.ml
cd $PIPELINE_HOME/cassandra.ml && pwd && sudo docker push fluxcapacitor/cassandra:$1

# clustered.ml
#cd $PIPELINE_HOME/clustered.ml/tensorflow && pwd && sudo docker push fluxcapacitor/clustered-tensorflow:$1
#cd $PIPELINE_HOME/clustered.ml/tensorflow && pwd && sudo docker push fluxcapacitor/clustered-tensorflow-gpu

# dashboard.ml
cd $PIPELINE_HOME/dashboard.ml/hystrix && pwd && sudo docker push fluxcapacitor/dashboard-hystrix:$1
cd $PIPELINE_HOME/dashboard.ml/turbine && pwd && sudo docker push fluxcapacitor/dashboard-turbine:$1

# elasticsearch.ml
cd $PIPELINE_HOME/elasticsearch.ml/2.3.0 && pwd && sudo docker push fluxcapacitor/elasticsearch-2.3.0:$1

# gpu.ml
cd $PIPELINE_HOME/gpu.ml && pwd && sudo docker push fluxcapacitor/gpu-tensorflow-spark-cpu:$1

# hdfs.ml
cd $PIPELINE_HOME/hdfs.ml && pwd && sudo docker push fluxcapacitor/hdfs-namenode:$1

# jupyterhub.ml
cd $PIPELINE_HOME/jupyterhub.ml && pwd && sudo docker push fluxcapacitor/jupyterhub:$1

# keyvalue.ml
cd $PIPELINE_HOME/keyvalue.ml/redis && pwd && sudo docker push fluxcapacitor/keyvalue-redis-master:$1

# kibana.ml
cd $PIPELINE_HOME/kibana.ml/4.5.0 && pwd && sudo docker push fluxcapacitor/kibana-4.5.0:$1

# kubernetes.ml
cd $PIPELINE_HOME/kubernetes.ml && pwd && sudo docker push fluxcapacitor/kubernetes:$1

# loadtest.ml
cd $PIPELINE_HOME/loadtest.ml && pwd && sudo docker push fluxcapacitor/loadtest:$1

# metastore.ml
cd $PIPELINE_HOME/metastore.ml && pwd && sudo docker push fluxcapacitor/metastore-2.1.1:$1

# prediction.ml
cd $PIPELINE_HOME/prediction.ml/jvm && pwd && sudo docker push fluxcapacitor/prediction-jvm:$1
cd $PIPELINE_HOME/prediction.ml/python3 && pwd && sudo docker push fluxcapacitor/prediction-python3:$1
cd $PIPELINE_HOME/prediction.ml/tensorflow && pwd && sudo docker push fluxcapacitor/prediction-tensorflow-cpu:$1
#cd $PIPELINE_HOME/prediction.ml/tensorflow && pwd && sudo docker push fluxcapacitor/prediction-tensorflow-gpu:$1

# presto.ml
cd $PIPELINE_HOME/presto.ml/master && pwd && sudo docker push fluxcapacitor/presto-master-0.167:$1
cd $PIPELINE_HOME/presto.ml/worker && pwd && sudo docker push fluxcapacitor/presto-worker-0.167:$1
cd $PIPELINE_HOME/presto.ml/ui && pwd && sudo docker push fluxcapacitor/presto-ui:$1

# scheduler.ml
cd $PIPELINE_HOME/scheduler.ml/airflow && pwd && sudo docker push fluxcapacitor/scheduler-airflow:$1

# sql.ml
cd $PIPELINE_HOME/sql.ml/mysql && pwd && sudo docker push fluxcapacitor/sql-mysql-master:$1

# stream.ml
cd $PIPELINE_HOME/stream.ml/kafka/0.10 && pwd && sudo docker push fluxcapacitor/stream-kafka-0.10:$1

# web.ml
cd $PIPELINE_HOME/web.ml/home && pwd && sudo docker push fluxcapacitor/web-home:$1

# zeppelin.ml
cd $PIPELINE_HOME/zeppelin.ml && pwd && sudo docker push fluxcapacitor/zeppelin:$1

# zookeeper.ml
cd $PIPELINE_HOME/zookeeper.ml && pwd && sudo docker push fluxcapacitor/zookeeper:$1
