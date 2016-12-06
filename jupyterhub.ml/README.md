
## Prequisites

## Start Docker Container
* Note: For live Spark Cluster, use `SPARK_MASTER="spark://<spark-master-host>:<spark-master-port>"`
* See `run` script for more details on the `-e` ENV vars passed here
```
docker run -itd --name=jupyterhub --net=host -e SPARK_MASTER="local[*]" -e SPARK_SUBMIT_ARGS="--conf spark.cores.max=1 --conf spark.executor.memory=1g --packages com.amazonaws:aws-java-sdk:1.7.4,org.apache.hadoop:hadoop-aws:2.7.1 --jars /root/lib/jpmml-sparkml-package-1.0-SNAPSHOT.jar --py-files /root/lib/jpmml.py" -p 8754:8754 fluxcapacitor/jupyterhub
```

## Verify Successful Start through Logs
```
docker logs -f jupyterhub

### EXPECTED OUTPUT ###
...
[I 2016-09-07 16:29:07.389 JupyterHub app:968] Starting proxy @ http://*:8754/
16:29:07.482 - info: [ConfigProxy] Proxying http://*:8754 to http://127.0.0.1:8081
16:29:07.484 - info: [ConfigProxy] Proxy API at http://127.0.0.1:8755/api/routes
[I 2016-09-07 16:29:07.493 JupyterHub app:1254] JupyterHub is now running at http://127.0.0.1:8754/
...
```

## (Optional) Verify Successful Start through Bash
* Bash into the Docker Container 
```
docker exec -it jupyterhub bash
```
* From within the Docker Container
```
ps -aef | grep jupyterhub

*** EXPECTED OUTPUT ***
...
root         1     0  0 16:29 ?        00:00:00 /usr/bin/python3 /usr/local/bin/jupyterhub --config=jupyterhub_config.py
...
```

## (Optional) Build new Docker Image
* From the directory that contains the jupyterhub.ml `Dockerfile`, run the following command:
```
docker build -t fluxcapacitor/jupyterhub .
```
