#!/bin/bash

service cassandra start
service ssh start
nohup zookeeper-server-start /etc/kafka/zookeeper.properties &
nohup kafka-server-start /etc/kafka/server.properties &
nohup schema-registry-start /etc/schema-registry/schema-registry.properties &
nohup kafka-rest-start /etc/kafka-rest/kafka-rest.properties &
service elasticsearch start
service apache2 start

nohup /incubator-zeppelin/bin/zeppelin-daemon.sh start &
nohup /spark-1.4.0-bin-hadoop2.6/sbin/start-all.sh &

