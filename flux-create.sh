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
#cqlsh -e "USE pipeline; DROP TABLE IF EXISTS real_time_ratings;"
cqlsh -e "USE pipeline; CREATE TABLE real_time_ratings (fromUserId int, toUserId int, rating int, batchTime timestamp, PRIMARY KEY(fromUserId, toUserId));"
#cqlsh -e "USE pipeline; DROP TABLE IF EXISTS real_time_likes;"
cqlsh -e "USE pipeline; CREATE TABLE real_time_likes (fromUserId int, toUserId int, batchTime timestamp, PRIMARY KEY(fromUserId, toUserId));"

echo ...Creating HDFS...
hdfs namenode -format

echo ...Creating Reference Data in Hive...
#spark-sql --jars $MYSQL_CONNECTOR_JAR -e 'DROP TABLE IF EXISTS gender_json_file'
spark-sql --jars $MYSQL_CONNECTOR_JAR -e 'CREATE TABLE gender_json_file(id INT, gender STRING) USING org.apache.spark.sql.json OPTIONS (path "datasets/dating/gender.json.bz2")'
#spark-sql --jars $MYSQL_CONNECTOR_JAR -e 'DROP TABLE IF EXISTS likes_parquet_file'
spark-sql --jars $MYSQL_CONNECTOR_JAR -e 'CREATE TABLE likes_parquet_file(from_user_id INT, to_user_id INT) USING org.apache.spark.sql.parquet OPTIONS (path "datasets/sparkafterdark/likes.parquet")'
