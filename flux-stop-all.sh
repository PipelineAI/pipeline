#!/bin/bash

zookeeper-server-stop ~/pipeline/config/kafka/zookeeper.properties
kafka-server-stop ~/pipeline/config/kafka/server.properties
schema-registry-stop ~/pipeline/config/schema-registry/schema-registry.properties
kafka-rest-stop ~/pipeline/config/kafka-rest/kafka-rest.properties

~/zeppelin-0.5.1-spark-1.4.1-hadoop-2.6.0/bin/zeppelin-daemon.sh --config ~/pipeline/config/zeppelin stop 
~/spark-1.4.1-bin-hadoop2.6/sbin/stop-master.sh --webui-port 6060 
~/spark-1.4.1-bin-hadoop2.6/sbin/stop-slave.sh --webui-port 6061
~/spark-1.4.1-bin-hadoop2.6/sbin/stop-thriftserver.sh  
~/tachyon-0.6.4/bin/tachyon-stop.sh 
kill -9 $(cat ~/spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet/RUNNING_PID ) & rm ~/spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet/RUNNING_PID

#rstudio-server stop

#service neo4j-service stop
service redis-server stop
service cassandra stop
service elasticsearch stop
service apache2 stop
service ssh stop
service mysql stop

# TODO:  Figure out how to stop these processes
#~/logstash-1.5.2/bin/logstash agent stop
#~/kibana-4.1.1-linux-x64/bin/kibana stop
zookeeper-server-stop 
