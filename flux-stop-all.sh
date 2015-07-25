#!/bin/bash

echo Stopping ZooKeeper
zookeeper-server-stop ~/pipeline/config/kafka/zookeeper.properties
echo Stopping Kafka
kafka-server-stop ~/pipeline/config/kafka/server.properties
echo Stopping Schema Registry
schema-registry-stop ~/pipeline/config/schema-registry/schema-registry.properties
echo Stopping Kafka REST Proxy
kafka-rest-stop ~/pipeline/config/kafka-rest/kafka-rest.properties

echo Stopping Apache Zeppelin
~/zeppelin-0.5.1-spark-1.4.1-hadoop-2.6.0/bin/zeppelin-daemon.sh --config ~/pipeline/config/zeppelin stop 
echo Stopping Apache Spark Master
~/spark-1.4.1-bin-hadoop2.6/sbin/stop-master.sh --webui-port 6060 
echo Stopping Apache Spark Worker
~/spark-1.4.1-bin-hadoop2.6/sbin/stop-slave.sh --webui-port 6061
echo Stopping Apache Spark JDBC/ODBC Hive ThriftServer
~/spark-1.4.1-bin-hadoop2.6/sbin/stop-thriftserver.sh  
echo Stopping Tachyon
~/tachyon-0.6.4/bin/tachyon-stop.sh 
echo Stopping Spark-Notebook
kill -9 $(cat ~/spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet/RUNNING_PID ) & rm ~/spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet/RUNNING_PID

# Stopping RStudio
#rstudio-server stop

# Stopping Neo4j
#service neo4j-service stop
# Stopping Redis Server
service redis-server stop
# Stopping Cassandra
service cassandra stop
# Stopping ElasticSearch
service elasticsearch stop
# Stopping Apache2
service apache2 stop
# Stopping SSH
service ssh stop
# Stopping MySQL
service mysql stop

# TODO:  Figure out how to stop these processes
#echo Stopping Logstash
#~/logstash-1.5.2/bin/logstash agent stop
#echo Stopping Kibana
#~/kibana-4.1.1-linux-x64/bin/kibana stop

echo Stopping ZooKeeper (again)
zookeeper-server-stop 
