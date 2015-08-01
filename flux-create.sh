#!/bin/bash

. ./flux-setenv.sh

echo Setting Up Kafka Topics
kafka-topics --zookeeper localhost:2181 --create --topic ratings --partitions 1 --replication-factor 1
kafka-topics --zookeeper localhost:2181 --create --topic likes --partitions 1 --replication-factor 1

echo Setting Up ElasticSearch Indexes
curl -XPUT 'http://localhost:9200/pipeline/' -d '{
    "settings": {
        "number_of_shards": 1,
        "number_of_replicas": 0
    }
}'

echo Setting Up Cassandra Keyspaces Column Families and Tables
cqlsh -e "DROP KEYSPACE IF EXISTS pipeline;"
cqlsh -e "CREATE KEYSPACE pipeline WITH REPLICATION = { 'class': 'SimpleStrategy',  'replication_factor':1};"
cqlsh -e "USE pipeline; DROP TABLE IF EXISTS real_time_ratings;"
cqlsh -e "USE pipeline; CREATE TABLE real_time_ratings (fromUserId int, toUserId int, rating, int,  batchTime timestamp, PRIMARY KEY(fromUserId, toUserId, rating));"

cqlsh -e "USE pipeline; DROP TABLE IF EXISTS real_time_likes;"
cqlsh -e "USE pipeline; CREATE TABLE real_time_likes (fromUserId int, toUserId int, batchTime timestamp,  PRIMARY KEY(fromUserId, toUserId));"

echo Setting up HDFS
hdfs namenode -format

echo Setting Up Hive Historical Real-Time Likes Data from Parquet 
spark-sql -e 'DROP TABLE IF EXISTS likes_parquet_file'
spark-sql -e 'CREATE TABLE likes_parquet_file(from_user_id INT, to_user_id INT) USING org.apache.spark.sql.parquet OPTIONS (path "datasets/sparkafterdark/likes.parquet")'

echo Setting Up Hive Ratings Data from BZip2 JSON into Hive Tables
spark-sql -e 'DROP TABLE IF EXISTS ratings_json_file'
spark-sql -e 'CREATE TABLE ratings_json_file(fromUserId INT, toUserId INT, rating INT) USING org.apache.spark.sql.json OPTIONS (path "datasets/dating/ratings.json.bz2")'

echo Setting Up Gender Data from BZip2 JSON into Hive Tables
spark-sql -e 'DROP TABLE IF EXISTS gender_json_file'
spark-sql -e 'CREATE TABLE gender_json_file(id INT, gender STRING) USING org.apache.spark.sql.json OPTIONS (path "datasets/dating/gender.json.bz2")'
