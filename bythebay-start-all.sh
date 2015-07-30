#!/bin/bash

echo Starting ElasticSearch
service elasticsearch start

#echo Starting Logstash
#nohup ~/logstash-1.5.2/bin/logstash agent -f ~/pipeline/config/logstash/logstash.conf &

echo Starting SSH
service ssh start

#echo Ganglia
#service ganglia-monitor start 
#service gmetad start

echo Starting Apache2 Httpd
service apache2 start

echo Starting MySQL
service mysql start

#echo Starting Redis
#service redis-server start

#echo Starting Neo4j
#service neo4j-service start

echo Starting Cassandra
service cassandra start

echo Starting ZooKeeper
nohup zookeeper-server-start ~/pipeline/config/kafka/zookeeper.properties &

echo Starting Kafka
nohup kafka-server-start ~/pipeline/config/kafka/server.properties &

echo Starting Apache Zeppelin
nohup ~/zeppelin-0.5.1-spark-1.4.1-hadoop-2.6.0/bin/zeppelin-daemon.sh start

echo Starting Apache Spark Master
nohup ~/spark-1.4.1-bin-fluxcapacitor/sbin/start-master.sh --webui-port 6060 -i 127.0.0.1 -h 127.0.0.1 

echo Starting Apache Spark Worker
nohup ~/spark-1.4.1-bin-fluxcapacitor/sbin/start-slave.sh --webui-port 6061 spark://127.0.0.1:7077 

# Spark ThriftServer
## MySql must be started - and the password set - before ThriftServer will startup
## Starting the ThriftServer will create a dummy derby.log and metastore_db per https://github.com/apache/spark/pull/6314
## The actual Hive metastore defined in conf/hive-site.xml is still used, however.
echo Starting Apache Spark JDBC/ODBC Hive ThriftServer
nohup ~/spark-1.4.1-bin-fluxcapacitor/sbin/start-thriftserver.sh --master spark://127.0.0.1:7077

#echo Starting Tachyon
#nohup ~/tachyon-0.7.0/bin/tachyon format
#nohup ~/tachyon-0.7.0/bin/tachyon-start.sh local   

echo Starting Spark-Notebook
nohup ~/spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet/bin/spark-notebook -Dconfig.file=/root/pipeline/config/spark-notebook/application-pipeline.conf &

#echo Starting Kibana
#nohup ~/kibana-4.1.1-linux-x64/bin/kibana &

# Starting this at the end due to race conditions with other kafka components
echo Starting Kafka Schema Registry
nohup schema-registry-start ~/pipeline/config/schema-registry/schema-registry.properties &

echo Starting Kafka REST Proxy
nohup kafka-rest-start ~/pipeline/config/kafka-rest/kafka-rest.properties &

#echo Starting Hystrix Sample Webapp
#cd ~/Hystrix/hystrix-examples-webapp
#../gradlew jettyRun &

#echo Starting Hystrix Dashboard
#cd ~/Hystrix/hystrix-dashboard
#../gradlew jettyRun &
#cd ~
