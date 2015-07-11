#!/bin/bash

zookeeper-server-stop /etc/kafka/zookeeper.properties
kafka-server-stop /etc/kafka/server.properties
schema-registry-stop /etc/schema-registry/schema-registry.properties
kafka-rest-stop /etc/kafka-rest/kafka-rest.properties

~/incubator-zeppelin/bin/zeppelin-daemon.sh stop
~/spark-1.4.0-bin-hadoop2.6/sbin/stop-master.sh --webui-port 6060 &
~/spark-1.4.0-bin-hadoop2.6/sbin/stop-slave.sh --webui-port 6061 &
~/spark-1.4.0-bin-hadoop2.6/sbin/stop-thriftserver.sh &

service cassandra stop
service elasticsearch stop
service apache2 stop
service ssh stop
