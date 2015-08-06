#!/bin/bash

echo ...Creating Kafka Topics...
kafka-topics --zookeeper localhost:2181 --create --topic ratings --partitions 1 --replication-factor 1
kafka-topics --zookeeper localhost:2181 --create --topic likes --partitions 1 --replication-factor 1

echo Creating ElasticSearch Indexes
curl -XPUT 'http://localhost:9200/pipeline/' -d '{
    "settings": {
        "number_of_shards": 1,
        "number_of_replicas": 0
    }
}'

echo ...Creating Cassandra Keyspaces Column Families and Tables...
#cqlsh -e "DROP KEYSPACE IF EXISTS pipeline;"
cqlsh -e "CREATE KEYSPACE pipeline WITH REPLICATION = { 'class': 'SimpleStrategy',  'replication_factor':1};"
#cqlsh -e "USE pipeline; DROP TABLE IF EXISTS ratings;"
cqlsh -e "USE pipeline; CREATE TABLE ratings (fromUserId int, toUserId int, rating int, batchTime bigint, PRIMARY KEY(fromUserId, toUserId));"
#cqlsh -e "USE pipeline; DROP TABLE IF EXISTS likes;"
cqlsh -e "USE pipeline; CREATE TABLE likes (fromUserId int, toUserId int, batchTime bigint, PRIMARY KEY(fromUserId, toUserId));"

echo ...Creating HDFS...
hdfs namenode -format

echo ...Creating Reference Data in Hive...
#spark-sql --jars $MYSQL_CONNECTOR_JAR -e 'DROP TABLE IF EXISTS gender'
spark-sql --jars $MYSQL_CONNECTOR_JAR -e 'CREATE TABLE gender(id INT, gender STRING) USING org.apache.spark.sql.json OPTIONS (path "datasets/dating/gender.json.bz2")'
