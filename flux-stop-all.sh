#!/bin/bash 

keeper-server-stop /etc/kafka/zookeeper.properties
kafka-server-stop /etc/kafka/server.properties
schema-registry-stop /etc/schema-registry/schema-registry.properties
kafka-rest-stop /etc/kafka-rest/kafka-rest.properties
service cassandra stop
service elasticsearch stop
service apache2 stop
service ssh stop

/incubator-zeppelin/bin/zeppelin-daemon.sh stop
/spark-1.4.0-bin-hadoop2.6/sbin/stop-all.sh
service ssh stop
