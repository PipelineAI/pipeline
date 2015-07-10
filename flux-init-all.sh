#!/bin/bash

# Setup Kafka
#kafka-topics --zookeeper localhost:2181 --delete --topic likes
kafka-topics --zookeeper localhost:2181 --create --topic likes --partitions 1 --replication-factor 1

# Setup ElasticSearch
curl -XPUT 'http://localhost:9200/sparkafterdark/' -d '{
    "settings": {
        "number_of_shards": 1,
        "number_of_replicas": 0
    }
}'

# Setup Cassandra
cqlsh -e "DROP KEYSPACE IF EXISTS sparkafterdark;"
cqlsh -e "CREATE KEYSPACE sparkafterdark WITH REPLICATION = { 'class': 'SimpleStrategy',  'replication_factor':1};"
cqlsh -e "USE sparkafterdark; DROP TABLE IF EXISTS real_time_likes;"
cqlsh -e "USE sparkafterdark; CREATE TABLE real_time_likes (fromUserId int, toUserId int, batchTime timestamp,  PRIMARY KEY(fromUserId, toUserId));"
