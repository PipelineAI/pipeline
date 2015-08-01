#!/bin/bash

. ./flux-setenv.sh

echo Stopping Kafka
kafka-server-stop 

echo Stopping Schema Registry
schema-registry-stop 

echo Stopping Kafka REST Proxy
kafka-rest-stop 

echo Stopping Apache Zeppelin
$ZEPPELIN_HOME/bin/zeppelin-daemon.sh stop 

echo Stopping Apache Spark Master
$SPARK_HOME/sbin/stop-master.sh --webui-port 6060 

echo Stopping Apache Spark Worker
$SPARK_HOME/sbin/stop-slave.sh --webui-port 6061

echo Stopping Apache Spark JDBC/ODBC Hive ThriftServer
$SPARK_HOME/sbin/stop-thriftserver.sh  

echo Stopping Tachyon
$TACHYON_HOME/bin/tachyon-stop.sh 

echo Stopping ZooKeeper
zookeeper-server-stop 

echo Stopping Spark-Notebook
kill -9 $(cat $DEV_INSTALL_HOME/spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet/RUNNING_PID ) && rm -rf $DEV_INSTALL_HOME/spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet/RUNNING_PID

#echo Stopping Cassandra
#cassandra stop

echo Stopping Apache2 Httpd
service apache2 stop

echo Stopping Ganglia
service gmetad stop
service ganglia-monitor stop

echo Stoppig SSH
service ssh stop

echo Stopping MySQL
service mysql stop

#echo Stopping Kibana
#kibana stop

#echo Stopping Logstash
#logstash agent -f $PIPELINE_HOME/config/logstash/logstash.conf stop &

#echo Stopping ElasticSearch 
#elasticsearch stop
