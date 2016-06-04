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
cqlsh -e "DROP KEYSPACE IF EXISTS advancedspark; CREATE KEYSPACE advancedspark WITH REPLICATION = {'class': 'SimpleStrategy',  'replication_factor':1}; USE advancedspark; DROP TABLE IF EXISTS item_ratings; USE advancedspark; CREATE TABLE item_ratings(userId int, itemId int, rating float, timestamp bigint, geocity text, PRIMARY KEY(userId, itemId)); USE advancedspark;COPY item_ratings (userId, itemId, rating, timestamp, geocity) FROM '/root/pipeline/datasets/graph/item-ratings-geo.csv';"

echo '...Flushing Redis...'
redis-cli FLUSHALL

#echo '...Creating Docker-local HDFS...'
#hdfs namenode -format

echo '...Creating Example Hive Tables...'
echo '...**** TABLES CREATED WITH A NON-HIVE-SUPPORTED SerDe LIKE com.databricks.spark.csv WILL NOT BE QUERYABLE BY HIVE ****...'
spark-sql --repositories $SPARK_REPOSITORIES --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e 'DROP TABLE IF EXISTS movies; CREATE TABLE movies(movieId INT, title STRING, genres STRING) USING com.databricks.spark.csv OPTIONS (path "'$DATASETS_HOME'/movielens/ml-latest/movies.csv.bz2", header "true"); DROP TABLE IF EXISTS movie_ratings; CREATE TABLE movie_ratings(userId INT, movieId INT, rating FLOAT, timestamp INT) USING com.databricks.spark.csv OPTIONS (path "'$DATASETS_HOME'/movielens/ml-latest/ratings.csv.bz2", header "true"); DROP TABLE IF EXISTS movie_tags; CREATE TABLE movie_tags(userId INT, movieId INT, tags STRING, timestamp INT) USING com.databricks.spark.csv OPTIONS (path "'$DATASETS_HOME'/movielens/ml-latest/tags.csv.bz2", header "true"); DROP TABLE IF EXISTS movie_links; CREATE TABLE movie_links(movieId INT, imdbId INT, tbmdId INT) USING com.databricks.spark.csv OPTIONS (path "'$DATASETS_HOME'/movielens/ml-latest/links.csv.bz2", header "true"); DROP TABLE IF EXISTS dating_genders; CREATE TABLE dating_genders(userId INT, gender STRING) USING com.databricks.spark.csv OPTIONS (path "'$DATASETS_HOME'/dating/genders.csv.bz2", header "true"); DROP TABLE IF EXISTS dating_ratings; CREATE TABLE dating_ratings(fromUserId INT, toUserId INT, rating INT) USING com.databricks.spark.csv OPTIONS (path "'$DATASETS_HOME'/dating/ratings.csv.bz2", header "true");'

# Hive and Presto Friendly Versions of Hive Tables
# Note:  We have to copy the files before LOADing into Hive, otherwise they get slurped into HDFS 
#          and no-longer accessible on the local file system.  
#        (The EXTERNAL keyword does not affect this behavior.)
cp $DATASETS_HOME/movielens/ml-latest/movies.csv $DATASETS_HOME/movielens/ml-latest/movies-hive-friendly.csv
spark-sql --repositories $SPARK_REPOSITORIES --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e "DROP TABLE IF EXISTS movies_hive_friendly; CREATE EXTERNAL TABLE movies_hive_friendly (id INT, title STRING, tags STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE; LOAD DATA INPATH '/root/pipeline/datasets/movielens/ml-latest/movies-hive-friendly.csv' OVERWRITE INTO TABLE movies_hive_friendly;"

cp $DATASETS_HOME/movielens/ml-latest/ratings.csv $DATASETS_HOME/movielens/ml-latest/ratings-hive-friendly.csv
spark-sql --repositories $SPARK_REPOSITORIES --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES -e "DROP TABLE IF EXISTS ratings_hive_friendly; CREATE EXTERNAL TABLE ratings_hive_friendly (userId INT, itemId INT, rating INT, timestamp INT) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE; LOAD DATA INPATH '/root/pipeline/datasets/movielens/ml-latest/ratings-hive-friendly.csv' OVERWRITE INTO TABLE ratings_hive_friendly;"
