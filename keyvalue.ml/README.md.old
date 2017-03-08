Based on [Redis](http://redis.io)

## Prequisites
* None

## Start Docker Container
```
docker run -itd --name=redis --privileged --net=host -p 6379:6379 -p 7379:7379 fluxcapacitor/keyvalue-redis
```

## Verify Successful Start through Logs
```
docker logs -f redis
```

## Verify Successful Start through REST API
```
http://<server-ip>:7379/PING

*** EXPECTED OUTPUT ***
...
{"PING":[true,"PONG"]}
...
```

## (Optional) Verify Successful through Docker Directly
* Bash into the Docker Container 
```
docker exec -it redis bash
```
* From within the Docker Container
```
redis-cli

*** EXPECTED OUTPUT ***
...
127.0.0.1:6379> PING
PONG
...
```

## (Optional) Build new Docker Image
```
docker build -t fluxcapacitor/keyvalue-redis .
```
