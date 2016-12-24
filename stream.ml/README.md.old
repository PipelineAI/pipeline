## Prequisites
* Available ZooKeeper Cluster (ie. https://github.com/fluxcapacitor/zookeeper.ml)
* ZOOKEEPER_HOST_AND_PORT_LIST=zookeeper-server-1:2181,zookeeper-server-2:2181

## Start Docker Container
* ZOOKEEPER_HOST_AND_PORT_LIST=comma-sep list of ZooKeeper host:port pairs 
(ie. zookeeper-server-1:2181,zookeeper-server-2:2181)
* TOPIC_NAME=topic to create (ie. item_ratings, dogs_and_cats)
* NUM_PARTITIONS=number of partitions for the topic (ie. 1 or 3) 
* NUM_REPLICAS=number of replicas for each partition (ie. 1 or 3) 
```
docker run -itd --name=kafka --privileged --net=host -p 9092:9092 -p 8082:8082 -p 8081:8081 -e ZOOKEEPER_HOST_AND_PORT_LIST=<zookeeper-host-and-port-list> -e TOPIC_NAME=<topic-name> -e NUM_PARTITIONS=<num-partitions> -e NUM_REPLICAS=<num-replicas> fluxcapacitor/stream-kafka-0.10
```

## Verify Successful Start through Logs
```
docker logs -f kafka
```

## Verify Successful Start through Kafka REST API
```
http://<server-ip>:8082/topics

*** EXPECTED OUTPUT ***
["__consumer_offsets","_schemas","<topic-name>"]   <-- Your topic-name from the `docker run` command above
```
## (Optional) Verify Successful through Kafka Directly
* Bash into the Docker Container 
```
docker exec -it kafka bash
```
* From within the Docker Container
```
root@docker# confluent-3.0.0/bin/kafka-topics --zookeeper $ZOOKEEPER_HOST_AND_PORT_LIST --list

*** EXPECTED OUTPUT ***
__consumer_offsets
_schemas
<topic-name>     <-- Your topic-name from the `docker run` command above
```

## (Optional) Build new Docker Image
```
cd kafka/0.10

docker build -t fluxcapacitor/stream-kafka-0.10 .
```
