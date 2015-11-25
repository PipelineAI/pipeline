#!/bin/bash

echo '...**** YOU MUST START ALL SERVICES BEFORE RUNNING THIS SCRIPT ****...'
echo '...**** IGNORE ANY ERRORS RELATED TO THINGS THAT ALREADY EXIST.  THIS IS OK. ****...'

echo '...Creating Example Kafka Topics...'
kafka-topics --zookeeper localhost:2181 --create --topic ratings --partitions 1 --replication-factor 1
#kafka-topics --zookeeper localhost:2181 --create --topic likes --partitions 1 --replication-factor 1

#echo '...Formatting Tachyon (if needed)...'
#tachyon format -s

echo '...Creating Example ElasticSearch Indexes...'
#curl -XDELETE 'http://localhost:9200/advancedspark'
curl -XPUT 'http://localhost:9200/advancedspark/' -d '{
    "settings": {
        "number_of_shards": 1,
        "number_of_replicas": 0
    }
}'

echo '...Creating Example Cassandra Keyspaces, Column Families/Tables...'
#cqlsh -e "DROP KEYSPACE IF EXISTS advancedspark;"
cqlsh -e "CREATE KEYSPACE advancedspark WITH REPLICATION = { 'class': 'SimpleStrategy',  'replication_factor':1};"
#cqlsh -e "USE advancedspark; DROP TABLE IF EXISTS ratings;"
cqlsh -e "USE advancedspark; CREATE TABLE ratings (userid int, itemid int, rating int, batchtime bigint, PRIMARY KEY(userid, itemid));"

#echo '...Creating and Formatting Docker-local HDFS...'
#hdfs namenode -format

echo '...Creating Example Hive Tables...'
#spark-sql --jars $MYSQL_CONNECTOR_JAR -e 'DROP TABLE IF EXISTS dating_genders'
spark-sql --jars $MYSQL_CONNECTOR_JAR -e 'CREATE TABLE dating_genders(id INT, gender STRING) USING org.apache.spark.sql.json OPTIONS (path "datasets/dating/genders.json.bz2")'
#spark-sql --jars $MYSQL_CONNECTOR_JAR -e 'DROP TABLE IF EXISTS dating_ratings'
spark-sql --jars $MYSQL_CONNECTOR_JAR -e 'CREATE TABLE dating_ratings(userid INT, itemid INT, rating INT) USING org.apache.spark.sql.json OPTIONS (path "datasets/dating/ratings.json.bz2")'
