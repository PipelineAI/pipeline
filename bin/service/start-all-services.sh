#!/bin/bash

cd $PIPELINE_HOME

echo '...Starting ElasticSearch...'
nohup elasticsearch -Des.insecure.allow.root=true -p $ELASTICSEARCH_HOME/RUNNING_PID --path.conf $CONFIG_HOME/elasticsearch &

echo '...Starting Logstash...'
export JAVA_OPTS_BACKUP=$JAVA_OPTS
unset JAVA_OPTS
nohup logstash -f $LOGSTASH_HOME/logstash.conf &
export JAVA_OPTS=$JAVA_OPTS_BACKUP
unset JAVA_OPTS_BACKUP

echo '...Starting SSH...'
service ssh start

echo '...Starting Ganglia...'
service ganglia-monitor start
service gmetad start

echo '...Starting Apache2 Httpd...'
service apache2 start

echo '...Starting MySQL...'
service mysql start

echo '...Starting Cassandra...'
nohup cassandra

echo '...Starting Hive Metastore...'
nohup hive --service metastore &

echo '...Starting ZooKeeper...'
nohup zookeeper-server-start $CONFLUENT_HOME/etc/kafka/zookeeper.properties &

echo '...Starting Redis...'
#nohup redis-server $REDIS_HOME/redis.conf &
nohup redis-server &

echo '...Starting Webdis...'
nohup webdis $WEBDIS_HOME/webdis.json &

echo '...Starting Confluent Kafka...'
nohup kafka-server-start $CONFLUENT_HOME/etc/kafka/server.properties &

echo '...Starting Zeppelin...'
nohup $ZEPPELIN_HOME/bin/zeppelin-daemon.sh start

echo '...Starting Spark Master...'
nohup $SPARK_HOME/sbin/start-master.sh --webui-port 6060 -h 0.0.0.0 

echo '...Starting Spark Worker...'
nohup $SPARK_HOME/sbin/start-slave.sh --cores 8 --memory 48g --webui-port 6061 -h 0.0.0.0 spark://127.0.0.1:7077

#echo '...Starting Spark External Shuffle Service...'
#nohup $SPARK_HOME/sbin/start-shuffle-service.sh

#echo '...Starting Spark History Server...'
#nohup $SPARK_HOME/sbin/start-history-server.sh &

#echo '...Starting Flink...'
nohup start-local.sh &

echo '...Starting Kibana...'
nohup kibana &

echo '...Starting Jupyter Notebook Server...'
# Note:  We are using pipeline-pyspark-shell.sh to pick up the --repositories, --jars, --packages of the rest of the environment 
PYSPARK_DRIVER_PYTHON="jupyter" PYSPARK_DRIVER_PYTHON_OPTS="notebook --config=$CONFIG_HOME/jupyter/jupyter_notebook_config.py" nohup pipeline-pyspark-shell.sh &

echo '...Starting Jupyter Hub Server...'
nohup jupyterhub -f $CONFIG_HOME/jupyter/jupyterhub_config.py &

#echo '...Starting NiFi...'
#nohup nifi.sh start &

#echo '...Starting Airflow...'
nohup airflow webserver &

#echo '...Starting Presto...'
nohup launcher --data-dir=$WORK_HOME/presto --launcher-log-file=$LOGS_HOME/presto/launcher.log --server-log-file=$LOGS_HOME/presto/presto.log start

echo '...Starting Kafka Schema Registry...'
sleep 5
# Starting this at the end - and with a sleep - due to race conditions with other kafka components
nohup schema-registry-start $CONFLUENT_HOME/etc/schema-registry/schema-registry.properties &

echo '...Starting Kafka REST Proxy...'
nohup kafka-rest-start $CONFLUENT_HOME/etc/kafka-rest/kafka-rest.properties &

#echo '...Starting Titan...'
#nodetool enablethrift
#nohup $TITAN_HOME/bin/gremlin-server.sh $TITAN_HOME/conf/gremlin-server/gremlin-server-rest-modern.yaml &

echo '...Starting Jenkins...'
service jenkins start

echo '...Starting Dynomite...'
dynomite -d -c $DYNOMITE_HOME/conf/dynomite.yml

#echo '...Starting Serving Serices...'
#$MYAPPS_HOME/serving/start-serving-services.sh

########################################################
# Be careful what starts up here.
# This same script is used upon initial startup -
#   before the create-datasources.sh script runs
########################################################

#echo '...Starting Kafka-Cassandra Spark Streaming Demo...'
#cd ~/pipeline
#$MYAPPS_HOME/spark/streaming/start-streaming-ratings-kafka-cassandra.sh

#echo '...Starting Kafka-Cassandra Incremental MatrixFactorization Spark Streaming Demo...'
#cd ~/pipeline
#$MYAPPS_HOME/spark/streaming/start-streaming-ratings-train-mf-incremental.sh

#echo '...Starting Flask-based Recommendation/Prediction Service...'
#cd $MYAPPS_HOME/serving/flask/
#nohup $MYAPPS_HOME/serving/flask/start-flask-recommendation-service.sh &

#echo '...Starting Finagle-based Recommendation/Prediction Service...'
#cd $MYAPPS_HOME/serving/finagle/
#nohup $MYAPPS_HOME/serving/finagle/start-finagle-recommendation-service.sh &

#echo '...Starting Model Watcher Service...'
#cd $MYAPPS_HOME/serving/watcher/
#nohup $MYAPPS_HOME/serving/watcher/start-model-watcher-service.sh &
