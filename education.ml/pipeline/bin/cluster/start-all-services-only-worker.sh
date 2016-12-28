#!/bin/bash

cd $PIPELINE_HOME

stop-all-services.sh

#echo '...Starting ElasticSearch...'
#nohup elasticsearch -Des.insecure.allow.root=true -p $ELASTICSEARCH_HOME/RUNNING_PID --path.conf $CONFIG_HOME/elasticsearch &

#echo '...Starting Logstash...'
#nohup logstash -f $LOGSTASH_HOME/logstash.conf &

#echo '...Starting SSH...'
#service ssh start

#echo '...Starting Ganglia...'
#service ganglia-monitor start
#service gmetad start

#echo '...Starting Apache2 Httpd...'
#service apache2 start

#echo '...Starting MySQL...'
#service mysql start

#echo '...Starting Cassandra...'
#nohup cassandra

#echo '...Starting Hive Metastore...'
#nohup hive --service metastore &

#echo '...Starting ZooKeeper...'
#nohup zookeeper-server-start $CONFIG_HOME/kafka/zookeeper.properties &

#echo '...Starting Redis...'
#nohup redis-server &

#echo '...Starting Webdis...'
#nohup webdis $WEBDIS_HOME/webdis.json &

#echo '...Starting Kafka...'
#nohup kafka-server-start $CONFIG_HOME/kafka/server.properties &

#echo '...Starting Zeppelin...'
#nohup $ZEPPELIN_HOME/bin/zeppelin-daemon.sh start

#echo '...Starting Spark Master...'
#nohup $SPARK_HOME/sbin/start-master.sh --webui-port 6060 -h 0.0.0.0 

echo '...Starting Spark Worker Pointing to Common Spark Master...'
nohup $SPARK_HOME/sbin/start-slave.sh --cores 8 --memory 48g --webui-port 6061 -h 0.0.0.0 spark://$1:7077
echo '...tail -f /root/pipeline/logs/spark/spark--org.apache.spark.deploy.worker.Worker-1-$HOSTNAME.out...'
tail -f /root/pipeline/logs/spark/spark--org.apache.spark.deploy.worker.Worker-1-$HOSTNAME.out

#echo '...Starting Spark External Shuffle Service...'
#nohup $SPARK_HOME/sbin/start-shuffle-service.sh

#echo '...Starting Spark History Server...'
#nohup $SPARK_HOME/sbin/start-history-server.sh &

#echo '...Starting Flink...'
#nohup start-local.sh &

#echo '...Starting Kibana...'
#nohup kibana &

#echo '...Starting Jupyter Notebook Server (via pipeline-pyspark.sh)...'
#nohup pipeline-pyspark.sh & 

#echo '...Starting NiFi...'
#nohup nifi.sh start &

#echo '...Starting Airflow...'
#nohup airflow webserver &

#echo '...Starting Presto...'
#nohup launcher --data-dir=$WORK_HOME/presto --launcher-log-file=$LOGS_HOME/presto/launcher.log --server-log-file=$LOGS_HOME/presto/presto.log start

#echo '...Starting Kafka Schema Registry...'
# Starting this at the end due to race conditions with other kafka components
#nohup schema-registry-start $CONFIG_HOME/schema-registry/schema-registry.properties &

#echo '...Starting Kafka REST Proxy...'
#nohup kafka-rest-start $CONFIG_HOME/kafka-rest/kafka-rest.properties &

#echo '...Starting Titan...'
#nodetool enablethrift
#nohup $TITAN_HOME/bin/gremlin-server.sh $TITAN_HOME/conf/gremlin-server/gremlin-server-rest-modern.yaml &
