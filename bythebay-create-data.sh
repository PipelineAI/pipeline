#!/bin/bash

echo Setting Up Kafka Topics
kafka-topics --zookeeper localhost:2181 --create --topic likes --partitions 1 --replication-factor 1

echo Setting Up ElasticSearch Indexes
curl -XPUT 'http://localhost:9200/sparkafterdark/' -d '{
    "settings": {
        "number_of_shards": 1,
        "number_of_replicas": 0
    }
}'

echo Setting Up Cassandra Keyspaces Column Families and Tables
cqlsh -e "DROP KEYSPACE IF EXISTS sparkafterdark;"
cqlsh -e "CREATE KEYSPACE sparkafterdark WITH REPLICATION = { 'class': 'SimpleStrategy',  'replication_factor':1};"
cqlsh -e "USE sparkafterdark; DROP TABLE IF EXISTS real_time_likes;"
cqlsh -e "USE sparkafterdark; CREATE TABLE real_time_likes (fromUserId int, toUserId int, batchTime timestamp,  PRIMARY KEY(fromUserId, toUserId));"

echo Setting Up Historical Real-Time Likes Data from Parquet into Hive Tables
~/spark-1.4.1-bin-fluxcapacitor/bin/spark-sql -e 'DROP TABLE IF EXISTS likes_parquet'
~/spark-1.4.1-bin-fluxcapacitor/bin/spark-sql -e 'CREATE TABLE likes_parquet(from_user_id INT, to_user_id INT) USING org.apache.spark.sql.parquet OPTIONS (path "/root/pipeline/datasets/sparkafterdark/likes.parquet")'

echo Setting Up Ratings Data from BZip2 JSON into Hive Tables
~/spark-1.4.1-bin-fluxcapacitor/bin/spark-sql -e 'DROP TABLE IF EXISTS ratings_json'
~/spark-1.4.1-bin-fluxcapacitor/bin/spark-sql -e 'CREATE TABLE ratings_json(fromUserId INT, toUserId INT, rating INT) USING org.apache.spark.sql.json OPTIONS (path "/root/pipeline/datasets/dating/ratings.json.bz2")'

echo Setting Up Gender Data from BZip2 JSON into Hive Tables
~/spark-1.4.1-bin-fluxcapacitor/bin/spark-sql -e 'DROP TABLE IF EXISTS gender_json'
~/spark-1.4.1-bin-fluxcapacitor/bin/spark-sql -e 'CREATE TABLE gender_json(id INT, gender STRING) USING org.apache.spark.sql.json OPTIONS (path "/root/pipeline/datasets/dating/gender.json.bz2")'
