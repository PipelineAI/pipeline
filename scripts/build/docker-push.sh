cd $PIPELINE_HOME

# $1: tag
 
# package.ml
sudo docker push fluxcapacitor/package-ubuntu-14.04:$1
sudo docker push fluxcapacitor/package-ubuntu-16.04:$1
sudo docker push fluxcapacitor/package-java-openjdk-1.8:$1
sudo docker push fluxcapacitor/package-spark-2.1.0:$1
sudo docker push fluxcapacitor/package-kafka-0.10:$1
sudo docker push fluxcapacitor/package-presto-0.167:$1
sudo docker push fluxcapacitor/package-apache2:$1
sudo docker push fluxcapacitor/package-gpu-cuda8-16.04:$1
sudo docker push fluxcapacitor/package-tensorflow-1.0:$1
#sudo docker push fluxcapacitor/package-tensorflow-2a48110-4d0a571:$1
sudo docker push fluxcapacitor/package-tensorflow-2a48110-4d0a571-gpu:$1

# apachespark.ml
sudo docker push fluxcapacitor/apachespark-master-2.1.0:$1
sudo docker push fluxcapacitor/apachespark-worker-2.1.0:$1

# cassandra.ml
sudo docker push fluxcapacitor/cassandra:$1

# clustered.ml
sudo docker push fluxcapacitor/clustered-tensorflow:$1
sudo docker push fluxcapacitor/clustered-tensorflow-gpu:$1

# dashboard.ml
sudo docker push fluxcapacitor/dashboard-hystrix:$1
sudo docker push fluxcapacitor/dashboard-turbine:$1

# elasticsearch.ml
sudo docker push fluxcapacitor/elasticsearch-2.3.0:$1

# gpu.ml
sudo docker push fluxcapacitor/gpu-tensorflow-spark:$1

# hdfs.ml
sudo docker push fluxcapacitor/hdfs-namenode:$1

# jupyterhub.ml
sudo docker push fluxcapacitor/jupyterhub:$1

# keyvalue.ml
sudo docker push fluxcapacitor/keyvalue-redis-master:$1

# kibana.ml
sudo docker push fluxcapacitor/kibana-4.5.0:$1

# kubernetes.ml
sudo docker push fluxcapacitor/kubernetes:$1

# loadtest.ml
sudo docker push fluxcapacitor/loadtest:$1

# metastore.ml
sudo docker push fluxcapacitor/metastore-2.1.1:$1

# prediction.ml
sudo docker push fluxcapacitor/prediction-jvm:$1
sudo docker push fluxcapacitor/prediction-python3:$1
sudo docker push fluxcapacitor/prediction-tensorflow:$1
sudo docker push fluxcapacitor/prediction-tensorflow-gpu:$1

# presto.ml
sudo docker push fluxcapacitor/presto-master-0.167:$1
sudo docker push fluxcapacitor/presto-worker-0.167:$1
sudo docker push fluxcapacitor/presto-ui:$1

# scheduler.ml
sudo docker push fluxcapacitor/scheduler-airflow:$1

# sql.ml
sudo docker push fluxcapacitor/sql-mysql-master:$1

# stream.ml
sudo docker push fluxcapacitor/stream-kafka-0.10:$1

# web.ml
sudo docker push fluxcapacitor/web-home:$1

# zeppelin.ml
sudo docker push fluxcapacitor/zeppelin:$1

# zookeeper.ml
sudo docker push fluxcapacitor/zookeeper:$1
