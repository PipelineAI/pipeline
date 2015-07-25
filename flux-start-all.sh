#!/bin/bash

service ssh start
service cassandra start
service elasticsearch start
service apache2 start
service mysql start
service redis-server start
#service neo4j-service start

nohup zookeeper-server-start ~/pipeline/config/kafka/zookeeper.properties &
nohup kafka-server-start ~/pipeline/config/kafka/server.properties &

nohup ~/zeppelin-0.5.1-spark-1.4.1-hadoop-2.6.0/bin/zeppelin-daemon.sh --config ~/pipeline/config/zeppelin start
nohup ~/spark-1.4.1-bin-hadoop2.6/sbin/start-master.sh --webui-port 6060 
nohup ~/spark-1.4.1-bin-hadoop2.6/sbin/start-slave.sh --webui-port 6061 spark://$HOSTNAME:7077 
# Spark ThriftServer:  MySql must be started - and the password set - before ThriftServer will startup
nohup ~/spark-1.4.1-bin-hadoop2.6/sbin/start-thriftserver.sh --master spark://$HOSTNAME:7077
nohup ~/tachyon-0.6.4/bin/tachyon format -s
nohup ~/tachyon-0.6.4/bin/tachyon-start.sh local 
nohup ~/spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet/bin/spark-notebook -Dconfig.file=/root/pipeline/config/spark-notebook/application-pipeline.conf &
#nohup rstudio-server start &
nohup ~/kibana-4.1.1-linux-x64/bin/kibana &
nohup ~/logstash-1.5.2/bin/logstash agent -f ~/pipeline/config/logstash/logstash.conf &

# Starting this at the end due to race conditions with other kafka components
nohup schema-registry-start ~/pipeline/config/schema-registry/schema-registry.properties &
nohup kafka-rest-start ~/pipeline/config/kafka-rest/kafka-rest.properties &
