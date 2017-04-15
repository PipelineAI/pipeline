# Spark Master
## Prequisites
* None

## Start Docker Container
```
docker run -itd --name=spark-master-2.0.1 --privileged --net=host -p 6060:6060 -p 6066:6066 -p 7077:7077 -p 4040-4059:4040-4059 fluxcapacitor/apachespark-master-2.0.1
```

## Verify Successful Start through Logs
```
docker logs -f spark-master-2.0.1
```

## Verify Successful Start through Admin UI
```
http://<server-ip>:6060
```

## (Verify Successful Start through Bash Directly
* Bash into the Docker Container 
```
docker exec -it spark-master-2.0.1 bash
```
* From within the Docker Container
```
TODO: 
```

## (Optional) Build new Docker Image
```
docker build -t fluxcapacitor/apachespark-master-2.0.1 -f Dockerfile.master .
```

# Spark Worker
## Prequisites
* Spark Master Running at spark://[spark-master-host]:[spark-master-port]
* SPARK_MASTER_HOST=[spark-master-host]
* SPARK_MASTER_PORT=[spark-master-port]

## Start Docker Container
* SPARK_WORKER_CORES=[worker-cores] 
* SPARK_WORKER_MEMORY=[worker-ram]
* SPARK_MASTER=[master-url]

Note: For live Spark Cluster, use `SPARK_MASTER="spark://<spark-master-host>:<spark-master-port>"`
```
docker run -itd --name=spark-worker-2.0.1 --privileged --net=host -p 6061:6061 -e SPARK_MASTER="local[*]" -e SPARK_WORKER_CORES=<spark-worker-cores> -e SPARK_WORKER_MEMORY=<spark-worker-memory> fluxcapacitor/apachespark-worker-2.0.1
```

## Verify Successful Start through Logs
```
docker logs -f spark-worker-2.0.1 
```

## Verify Successful Start through Bash Directly
* Bash into the Docker Container 
```
docker exec -it spark-worker-2.0.1 bash
```

## (Optional) Build new Docker Image
```
docker build -t fluxcapacitor/apachespark-worker-2.0.1 -f Dockerfile.worker .
```
