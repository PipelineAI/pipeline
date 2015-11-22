#!/bin/bash

echo '...**** YOU MUST START ALL SERVICES BEFORE RUNNING THIS SCRIPT ****...'
echo '...**** IGNORE ANY ERRORS RELATED TO THINGS THAT ALREADY EXIST.  THIS IS OK. ****...'

echo '...Creating Kafka Topics...'
kafka-topics --zookeeper localhost:2181 --create --topic ratings --partitions 1 --replication-factor 1
#kafka-topics --zookeeper localhost:2181 --create --topic likes --partitions 1 --replication-factor 1

echo '...Formatting Tachyon (if needed)...'
tachyon format -s

echo '...Creating ElasticSearch Indexes...'
#curl -XDELETE 'http://localhost:9200/fluxcapacitor'
curl -XPUT 'http://localhost:9200/fluxcapacitor/' -d '{
    "settings": {
        "number_of_shards": 1,
        "number_of_replicas": 0
    }
}'

echo '...Creating Cassandra Keyspaces Column Families and Tables...'
#cqlsh -e "DROP KEYSPACE IF EXISTS fluxcapacitor;"
cqlsh -e "CREATE KEYSPACE fluxcapacitor WITH REPLICATION = { 'class': 'SimpleStrategy',  'replication_factor':1};"
#cqlsh -e "USE fluxcapacitor; DROP TABLE IF EXISTS ratings;"
cqlsh -e "USE fluxcapacitor; CREATE TABLE ratings (fromuserid int, touserid int, rating int, batchtime bigint, PRIMARY KEY(fromuserid, touserid));"

#echo '...Creating and Formatting Docker-local HDFS...'
#hdfs namenode -format

echo '...Creating Reference Data in Hive...'
#spark-sql --jars $MYSQL_CONNECTOR_JAR -e 'DROP TABLE IF EXISTS genders'
spark-sql --jars $MYSQL_CONNECTOR_JAR -e 'CREATE TABLE genders(id INT, gender STRING) USING org.apache.spark.sql.json OPTIONS (path "datasets/dating/genders.json.bz2")'
#spark-sql --jars $MYSQL_CONNECTOR_JAR -e 'DROP TABLE IF EXISTS ratings'
spark-sql --jars $MYSQL_CONNECTOR_JAR -e 'CREATE TABLE ratings(fromuserid INT, touserid INT, rating INT) USING org.apache.spark.sql.json OPTIONS (path "datasets/dating/ratings.json.bz2")'
