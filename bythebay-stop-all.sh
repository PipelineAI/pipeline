#!/bin/bash

echo Stopping Kafka
kafka-server-stop ~/pipeline/config/kafka/server.properties

echo Stopping Schema Registry
schema-registry-stop ~/pipeline/config/schema-registry/schema-registry.properties

echo Stopping Kafka REST Proxy
kafka-rest-stop ~/pipeline/config/kafka-rest/kafka-rest.properties

echo Stopping Apache Zeppelin
~/zeppelin-0.5.1-spark-1.4.1-hadoop-2.6.0/bin/zeppelin-daemon.sh stop 

echo Stopping Apache Spark Master
~/spark-1.4.1-bin-fluxcapacitor/sbin/stop-master.sh --webui-port 6060 

echo Stopping Apache Spark Worker
~/spark-1.4.1-bin-fluxcapacitor/sbin/stop-slave.sh --webui-port 6061

echo Stopping Apache Spark JDBC/ODBC Hive ThriftServer
~/spark-1.4.1-bin-fluxcapacitor/sbin/stop-thriftserver.sh  

#echo Stopping Tachyon
#~/tachyon-0.7.0/bin/tachyon-stop.sh 

echo Stopping ZooKeeper
zookeeper-server-stop ~/pipeline/config/kafka/zookeeper.properties

echo Stopping Spark-Notebook
kill -9 $(cat ~/spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet/RUNNING_PID ) && rm -rf ~/spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet/RUNNING_PID

#Stopping Neo4j
#service neo4j-service stop

#echo Stopping Redis Server
#service redis-server stop

echo Stopping Cassandra
service cassandra stop

#echo Stopping Apache2 Httpd
#service apache2 stop

#echo Stopping Ganglia
#service gmetad stop
#service ganglia-monitor stop

echo Stoppig SSH
service ssh stop

echo Stopping MySQL
service mysql stop

#echo Stopping Kibana
#~/kibana-4.1.1-linux-x64/bin/kibana stop

#echo Stopping Logstash
#~/logstash-1.5.2/bin/logstash agent stop

echo Stopping ElasticSearch 
service elasticsearch stop
