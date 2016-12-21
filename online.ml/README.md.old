## Dependencies
* Available Streaming Source (ie. Kafka @ https://github.com/fluxcapacitor/stream.ml/)

## Start Docker Container (Consumer)
* STREAM_HOST_AND_PORT_LIST: comma-sep list of host:port pairs for the streaming source cluster (ie. List of Kafka Brokers)
* TOPIC_NAME: topic or queue within the streaming source (ie. Kafka Topic or Kinesis Stream)
* GROUP_NAME: consumer group to use for distributed stream consumers if support by streaming source (ie. Kafka group_id)
* NOTEBOOK: ??
* LEARNING_RATE: step size of gradient
* DECAY_RATE:  favor newer over older training samples given a rate of decay
* RANDOM_SEED: seed of randomly-generated initial weights
* BATCH_SIZE:  training batch size
* BATCH_SHUFFLE: true = shuffle each batch of data before processing
```
docker run -itd --name=online-tensorflow-consumer --privileged --net=host -e STREAM_HOST_AND_PORT_LIST=<stream-host-and-port-list> -e TOPIC_NAME=<stream-topic> -e GROUP_NAME=<group-name> -e LEARNING_RATE=<learning-rate> -e DECAY_RATE=<decay-rate> -e RANDOM_SEED=<random-seed> -e BATCH_SIZE=<batch-size> -e BATCH_SHUFFLE=<true|false> fluxcapacitor/online-tensorflow-consumer-0.10
```

## Verify Successful Start through Logs
```
docker logs -f online-tensorflow-consumer
```

## (Optional) Verify through Kafka Directly
* Bash into the Docker Container 
```
docker exec -it online-tensorflow-consumer bash
```
* From within the Docker Container
```
root@docker# ps -aef | grep python

*** EXPECTED OUTPUT ***
TODO:  
```

## (Optional) Build New Docker Image
```
cd tensorflow/0.10/consumer

docker build -t fluxcapacitor/online-tensorflow-consumer-0.10 .
```

## Start Docker Container (Producer)
* STREAM_HOST_AND_PORT_LIST: comma-sep list of host:port pairs for the streaming source cluster (ie. List of Kafka Brokers)
* TOPIC_NAME: topic or queue within the streaming source (ie. Kafka Topic or Kinesis Stream)
* GROUP_NAME: consumer group to use for distributed stream consumers if support by streaming source (ie. Kafka group_id)
* IMAGE_PATHS_GLOB_STRING:  glob string of images to push to kafka (ie. `/root/datasets/dogs_and_cats/*/*.jpg`)
```
docker run -itd --name=online-tensorflow-producer --privileged --net=host -e STREAM_HOST_AND_PORT_LIST=<stream-host-and-port-list> -e TOPIC_NAME=<stream-topic> -e IMAGE_PATHS_GLOB_STRING=<image-paths-glob-string> fluxcapacitor/online-tensorflow-producer-0.10
```

## Verify Successful Startup through Logs
```
docker logs -f online-tensorflow-producer
```
* Also, at this point, you should see data showing up in consumers of your Kafka topic

## (Optional) Build New Docker Image
```
cd tensorflow/0.10/producer

docker build -t fluxcapacitor/online-tensorflow-producer-0.10 .
```

