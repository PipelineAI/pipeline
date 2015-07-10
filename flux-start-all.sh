nohup zookeeper-server-start /etc/kafka/zookeeper.properties &
nohup kafka-server-start /etc/kafka/server.properties & 
nohup schema-registry-start /etc/schema-registry/schema-registry.properties &
nohup kafka-rest-start /etc/kafka-rest/kafka-rest.properties &
service cassandra start
service elasticsearch start
service apache2 start
service ssh start

nohup /incubator-zeppelin/bin/zeppelin-daemon.sh start &
nohup /spark-1.4.0-bin-hadoop2.6/sbin/start-all.sh & 

# Setup Kafka
kafka-topics --zookeeper localhost:2181 --create --topic likes --partitions 1 --replication-factor 1

# Setup ElasticSearch
curl -XPUT 'http://localhost:9200/sparkafterdark/' -d '{
    "settings": {
        "number_of_shards": 1,
        "number_of_replicas": 1
    }
}'

# Setup Cassandra
cqlsh -e "DROP KEYSPACE IF EXISTS sparkafterdark;"
cqlsh -e "CREATE KEYSPACE sparkafterdark WITH REPLICATION = { 'class': 'SimpleStrategy',  'replication_factor':1};"
cqlsh -e "USE sparkafterdark; DROP TABLE IF EXISTS real_time_likes;"
cqlsh -e "USE sparkafterdark; CREATE TABLE real_time_likes (fromUserId int, toUserId int, batchTime timestamp,  PRIMARY KEY(fromUserId, toUserId));"


