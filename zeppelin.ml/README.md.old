## Prequisites
* None

## Start Docker Container
Note:  For live Spark Cluster, use `SPARK_MASTER="spark://<spark-master-host>:<spark-master-port>"`
```
docker run -itd --name=zeppelin --privileged --net=host -e SPARK_MASTER="local[*]" -e GITHUB_REPO_OWNER_URL="https://github.com/fluxcapacitor" -e GITHUB_REPO_NAME="source.ml" -e GITHUB_RESET_REVISION="HEAD" -e GITHUB_CLONE_BRANCH="master" -e GITHUB_CHECKOUT_BRANCH="master" -p 3123:3123 -p 3124:3124 fluxcapacitor/zeppelin
```

## Verify Successful Start through Logs
```
docker logs -f zeppelin
```

## Verify Successful Start through UI
```
http://<server-ip>:3123
```

## (Optional) Verify Successful Start through Docker
* Bash into the Docker Container 
```
docker exec -it zeppelin bash
```
* From within the Docker Container
```
$ZEPPELIN_HOME/bin/zeppelin-daemon.sh status

*** EXPECTED OUTPUT ***
...
[OK]
```

## (Optional) Build new Docker Image
```
docker build -t fluxcapacitor/zeppelin .
```
