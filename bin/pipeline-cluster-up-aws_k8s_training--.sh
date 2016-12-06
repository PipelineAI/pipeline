#!/bin/sh                                                                               

echo '...MySql...'
kubectl create -f $PIPELINE_HOME/sql.ml/mysql-rc.yaml
kubectl create -f $PIPELINE_HOME/sql.ml/mysql-svc.yaml

echo '...ZooKeeper...'
kubectl create -f $PIPELINE_HOME/zookeeper.ml/zookeeper-rc.yaml
kubectl create -f $PIPELINE_HOME/zookeeper.ml/zookeeper-svc.yaml

echo '...ElasticSearch - 2.3.0...'
kubectl create -f $PIPELINE_HOME/elasticsearch.ml/elasticsearch-2-3-0-rc.yaml
kubectl create -f $PIPELINE_HOME/elasticsearch.ml/elasticsearch-2-3-0-svc.yaml

echo '...Redis...'
kubectl create -f $PIPELINE_HOME/keyvalue.ml/redis-rc.yaml
kubectl create -f $PIPELINE_HOME/keyvalue.ml/redis-svc.yaml

echo '...Cassandra...'
kubectl create -f $PIPELINE_HOME/cassandra.ml/cassandra-rc.yaml
kubectl create -f $PIPELINE_HOME/cassandra.ml/cassandra-svc.yaml

echo '...Spark - Master...'
kubectl create -f $PIPELINE_HOME/apachespark.ml/spark-master-rc.yaml
kubectl create -f $PIPELINE_HOME/apachespark.ml/spark-master-svc.yaml

echo '...Spark - Worker...'
kubectl create -f $PIPELINE_HOME/apachespark.ml/spark-worker-rc.yaml
kubectl create -f $PIPELINE_HOME/apachespark.ml/spark-worker-svc.yaml

echo '...Hive Metastore...'
kubectl create -f $PIPELINE_HOME/metastore.ml/metastore-rc.yaml
kubectl create -f $PIPELINE_HOME/metastore.ml/metastore-svc.yaml

echo '...Zeppelin...'
kubectl create -f $PIPELINE_HOME/zeppelin.ml/zeppelin-rc.yaml
kubectl create -f $PIPELINE_HOME/zeppelin.ml/zeppelin-svc.yaml

echo '...JupyterHub...'
kubectl create -f $PIPELINE_HOME/jupyterhub.ml/jupyterhub-rc.yaml
kubectl create -f $PIPELINE_HOME/jupyterhub.ml/jupyterhub-svc.yaml

echo '...Airflow Scheduler...'
kubectl create -f $PIPELINE_HOME/scheduler.ml/airflow-rc.yaml
kubectl create -f $PIPELINE_HOME/scheduler.ml/airflow-svc.yaml

echo '...Presto - Master...'
kubectl create -f $PIPELINE_HOME/presto.ml/presto-master-rc.yaml
kubectl create -f $PIPELINE_HOME/presto.ml/presto-master-svc.yaml

echo '...Presto - Worker...'
kubectl create -f $PIPELINE_HOME/presto.ml/presto-worker-rc.yaml
kubectl create -f $PIPELINE_HOME/presto.ml/presto-worker-svc.yaml

echo '...Presto - AirPal...'
kubectl create -f $PIPELINE_HOME/presto.ml/airpal-rc.yaml
kubectl create -f $PIPELINE_HOME/presto.ml/airpal-svc.yaml

echo '...Kafka - 0.8...'
kubectl create -f $PIPELINE_HOME/stream.ml/kafka-0.8-rc.yaml
kubectl create -f $PIPELINE_HOME/stream.ml/kafka-0.8-svc.yaml

echo '...Kibana - 4.5.0...'
kubectl create -f $PIPELINE_HOME/kibana.ml/kibana-4-5-0-rc.yaml
kubectl create -f $PIPELINE_HOME/kibana.ml/kibana-4-5-0-svc.yaml

echo '...Apache - Home...'
kubectl create -f $PIPELINE_HOME/web.ml/home-rc.yaml
kubectl create -f $PIPELINE_HOME/web.ml/home-svc.yaml

##############
# Dashboards
##############
echo '...Dashboard - Weavescope...'
kubectl create -f $PIPELINE_HOME/dashboard.ml/weavescope/weavescope.yaml
kubectl describe svc weavescope-app


echo '...Dashboard - Hystrix...'
kubectl create -f $PIPELINE_HOME/dashboard.ml/hystrix-rc.yaml
kubectl create -f $PIPELINE_HOME/dashboard.ml/hystrix-svc.yaml
