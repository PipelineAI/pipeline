#!/bin/bash

zookeeper-server-stop ~/pipeline/config/kafka/zookeeper.properties
kafka-server-stop ~/pipeline/config/kafka/server.properties
schema-registry-stop ~/pipeline/config/schema-registry/schema-registry.properties
kafka-rest-stop ~/pipeline/config/kafka-rest/kafka-rest.properties

~/incubator-zeppelin/bin/zeppelin-daemon.sh stop
~/spark-1.4.0-bin-hadoop2.6/sbin/stop-master.sh --webui-port 6060 &
~/spark-1.4.0-bin-hadoop2.6/sbin/stop-slave.sh --webui-port 6061 spark://$HOSTNAME:7077 &
~/spark-1.4.0-bin-hadoop2.6/sbin/stop-thriftserver.sh &

service cassandra stop
service elasticsearch stop
service apache2 stop
service ssh stop
