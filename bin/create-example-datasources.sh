#!/bin/bash

cd $PIPELINE_HOME

echo '...(Re)-create Data Stores including Cassandra, Redis, ElasticSearch, Hive Metastore...'

echo '...**** YOU MUST START ALL SERVICES BEFORE RUNNING THIS SCRIPT ****...'
echo '...**** IGNORE ANY ERRORS RELATED TO THINGS THAT ALREADY EXIST.  THIS IS OK. ****...'

echo '...Creating Example Kafka Topics...'
kafka-topics --zookeeper localhost:2181 --delete --topic item_ratings
kafka-topics --zookeeper localhost:2181 --create --topic item_ratings --partitions 1 --replication-factor 1

echo '...Creating Example ElasticSearch Indexes...'
curl -XDELETE 'http://localhost:9200/advancedspark'
curl -XPUT 'http://localhost:9200/advancedspark/' -d '{
    "settings": {
        "number_of_shards": 1,
        "number_of_replicas": 0
    }
}'

echo '...Creating Example Cassandra Keyspaces, Column Families/Tables...'
echo '...**** MAKE SURE NO SPARK JOBS ARE RUNNING BEFORE STARTING THIS SCRIPT ****...'
echo '...**** SPECIFICALLY, ANY JOB USING THE advancedspark COLUMN FAMILY OR advancedspark.item_ratings TABLE WILL FAIL AFTER RUNNING THIS SCRIPT ****...' 
cqlsh -e "DROP KEYSPACE IF EXISTS advancedspark;"
cqlsh -e "CREATE KEYSPACE advancedspark WITH REPLICATION = {'class': 'SimpleStrategy',  'replication_factor':1};"
cqlsh -e "USE advancedspark; DROP TABLE IF EXISTS item_ratings;"
cqlsh -e "USE advancedspark; CREATE TABLE item_ratings(userId int, itemId int, rating int, geo_city text, timestamp bigint, PRIMARY KEY(userId, itemId));"

echo '...Flushing Redis...'
redis-cli FLUSHALL

#echo '...Creating Docker-local HDFS...'
#hdfs namenode -format

echo '...Creating Example Hive Tables...'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'DROP TABLE IF EXISTS movies'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'CREATE TABLE movies(movieId INT, title STRING, genres STRING) USING com.databricks.spark.csv OPTIONS (path "'$DATASETS_HOME'/movielens/ml-latest/movies.csv.bz2", header "true")'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'DROP TABLE IF EXISTS movie_ratings'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'CREATE TABLE movie_ratings(userId INT, movieId INT, rating FLOAT, timestamp INT) USING com.databricks.spark.csv OPTIONS (path "'$DATASETS_HOME'/movielens/ml-latest/ratings.csv.bz2", header "true")'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'DROP TABLE IF EXISTS movie_tags'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'CREATE TABLE movie_tags(userId INT, movieId INT, tags STRING, timestamp INT) USING com.databricks.spark.csv OPTIONS (path "'$DATASETS_HOME'/movielens/ml-latest/tags.csv.bz2", header "true")'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'DROP TABLE IF EXISTS movie_links'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'CREATE TABLE movie_links(movieId INT, imdbId INT, tbmdId INT) USING com.databricks.spark.csv OPTIONS (path "'$DATASETS_HOME'/movielens/ml-latest/links.csv.bz2", header "true")'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'DROP TABLE IF EXISTS dating_genders'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'CREATE TABLE dating_genders(userId INT, gender STRING) USING com.databricks.spark.csv OPTIONS (path "'$DATASETS_HOME'/dating/genders.csv.bz2", header "true")'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'DROP TABLE IF EXISTS dating_ratings'
spark-sql --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'CREATE TABLE dating_ratings(fromUserId INT, toUserId INT, rating INT) USING com.databricks.spark.csv OPTIONS (path "'$DATASETS_HOME'/dating/ratings.csv.bz2", header "true")'
