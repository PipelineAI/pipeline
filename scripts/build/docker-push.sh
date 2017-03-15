cd $PIPELINE_HOME

# package.ml
sudo docker push fluxcapacitor/package-ubuntu-14.04
sudo docker push fluxcapacitor/package-anaconda-4.1.11
sudo docker push fluxcapacitor/package-java-openjdk-1.8
sudo docker push fluxcapacitor/package-java-oracle-1.8
sudo docker push fluxcapacitor/package-spark-2.1.0

sudo docker push fluxcapacitor/package-kafka-0.8
sudo docker push fluxcapacitor/package-kafka-0.10
sudo docker push fluxcapacitor/package-presto-0.167
sudo docker push fluxcapacitor/package-apache2

# apachespark.ml
sudo docker push fluxcapacitor/apachespark-master-2.1.0
sudo docker push fluxcapacitor/apachespark-worker-2.1.0

# cassandra.ml
sudo docker push fluxcapacitor/cassandra

# clustered.ml
sudo docker push fluxcapacitor/clustered-tensorflow

# dashboard.ml
sudo docker push fluxcapacitor/dashboard-hystrix
sudo docker push fluxcapacitor/dashboard-turbine

# elasticsearch.ml
sudo docker push fluxcapacitor/elasticsearch-2.3.0

# gpu.ml
sudo docker push fluxcapacitor/gpu-tensorflow

# hdfs.ml
sudo docker push fluxcapacitor/hdfs

# jupyterhub.ml
sudo docker push fluxcapacitor/jupyterhub

# keyvalue.ml
sudo docker push fluxcapacitor/keyvalue-redis

# kibana.ml
sudo docker push fluxcapacitor/kibana-4.5.0

# kubernetes.ml
sudo docker push fluxcapacitor/kubernetes
sudo docker push fluxcapacitor/kubernetes-admin

# loadtest.ml
sudo docker push fluxcapacitor/loadtest

# metastore.ml
sudo docker push fluxcapacitor/metastore-1.2.1

# prediction.ml
sudo docker push fluxcapacitor/prediction-codegen
sudo docker push fluxcapacitor/prediction-keyvalue
sudo docker push fluxcapacitor/prediction-pmml
sudo docker push fluxcapacitor/prediction-tensorflow
sudo docker push fluxcapacitor/prediction-python

# presto.ml
sudo docker push fluxcapacitor/presto-master-0.167
sudo docker push fluxcapacitor/presto-worker-0.167

# scheduler.ml
sudo docker push fluxcapacitor/scheduler-airflow

# sql.ml
sudo docker push fluxcapacitor/sql-mysql

# web.ml
sudo docker push fluxcapacitor/web-home

# zeppelin.ml
sudo docker push fluxcapacitor/zeppelin

# zookeeper.ml
sudo docker push fluxcapacitor/zookeeper
