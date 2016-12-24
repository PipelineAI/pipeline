## Prequisites
* None

## Start Docker Container
```
docker run -itd --name=zookeeper --privileged --net=host -p 2181:2181 fluxcapacitor/zookeeper
```

## Verify Successful Start through Logs
```
docker logs -f zookeeper
```

## Verify Successful through ZooKeeper Directly
* Bash into the Docker Container 
```
docker exec -it zookeeper bash
```
* From within the Docker Container
### ZooKeeper
```
confluent-3.0.0/bin/zookeeper-shell 127.0.0.1:2181

### EXAMPLE OUTPUT ###
...
Connecting to 127.0.0.1:2181
Welcome to ZooKeeper!
JLine support is disabled  <-- IGNORE THIS WARNING

WATCHER::

WatchedEvent state:SyncConnected type:None path:null
```
* Type `quit` to quit
```
quit
```

## (Optional) Build new Docker Image
```
docker build -t fluxcapacitor/zookeeper .
```
