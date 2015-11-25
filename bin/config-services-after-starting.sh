#!/bin/bash

echo '...**** YOU MUST START ALL SERVICES BEFORE RUNNING THIS SCRIPT ****...'
echo '...**** IGNORE ANY ERRORS RELATED TO THINGS THAT ALREADY EXIST.  THIS IS OK. ****...'

echo '...Creating Example Kafka Topics...'
kafka-topics --zookeeper localhost:2181 --delete --topic item_ratings
kafka-topics --zookeeper localhost:2181 --create --topic item_ratings --partitions 1 --replication-factor 1

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
cqlsh -e "DROP KEYSPACE IF EXISTS advancedspark;"
cqlsh -e "CREATE KEYSPACE advancedspark WITH REPLICATION = {'class': 'SimpleStrategy',  'replication_factor':1};"
cqlsh -e "USE advancedspark; DROP TABLE IF EXISTS item_ratings;"
cqlsh -e "USE advancedspark; CREATE TABLE item_ratings(userId int, itemId int, rating int, timestamp bigint, PRIMARY KEY(userId, itemId));"

#echo '...Creating and Formatting Docker-local HDFS...'
#hdfs namenode -format

echo '...Creating Example Hive Tables...'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'DROP TABLE IF EXISTS movies'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'CREATE TABLE movies(movieId INT, title STRING, genres STRING) USING com.databricks.spark.csv OPTIONS (path "'$DATASETS_HOME'/movielens/ml-latest/movies.csv.bz2")'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'DROP TABLE IF EXISTS movie_ratings'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'CREATE TABLE movie_ratings(userId INT, movieId INT, rating INT, timestamp INT) USING com.databricks.spark.csv OPTIONS (path "'$DATASETS_HOME'/movielens/ml-latest/ratings.csv.bz2")'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'DROP TABLE IF EXISTS movie_tags' 
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'CREATE TABLE movie_tags(userId INT, movieId INT, tags STRING, timestamp INT) USING com.databricks.spark.csv OPTIONS (path "'$DATASETS_HOME'/movielens/ml-latest/tags.csv.bz2")'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'DROP TABLE IF EXISTS movie_links'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'CREATE TABLE movie_links(movieId INT, imdbId INT, tbmdId INT) USING com.databricks.spark.csv OPTIONS (path "'$DATASETS_HOME'/movielens/ml-latest/links.csv.bz2")'
