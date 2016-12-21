## Prequisites
* Kubernetes [Cluster](https://github.com/fluxcapacitor/kubernetes.ml)

## Parameter Server 0
### Start Docker Container
```          
docker run -itd --name=clustered-tensorflow-ps0 --net=host -e PS_HOSTS="clustered-tensorflow-ps0:2222,clustered-tensorflow-ps1:2222" -e WORKER_HOSTS="clustered-tensorflow-worker0:2222,clustered-tensorflow-worker1:2222" -e JOB_NAME="ps" -e TASK_INDEX="0" -p 2222:2222 fluxcapacitor/clustered-tensorflow
```

### Verify Successful Start through Logs
```
docker logs -f clustered-tensorflow-ps0

### EXPECTED OUTPUT ###
...
TODO
...
```

## Parameter Server 1
### Start Docker Container
```          
docker run -itd --name=clustered-tensorflow-ps1 --net=host -e PS_HOSTS="clustered-tensorflow-ps0:2222,clustered-tensorflow-ps1:2222" -e WORKER_HOSTS="clustered-tensorflow-worker0:2222,clustered-tensorflow-worker1:2222" -e JOB_NAME="ps" -e TASK_INDEX="1" -p 2222:2222 fluxcapacitor/clustered-tensorflow
```

### Verify Successful Start through Logs
```
docker logs -f clustered-tensorflow-ps1

### EXPECTED OUTPUT ###
...
TODO
...
```

## Worker 0
### Start Docker Container
```          
docker run -itd --name=clustered-tensorflow-worker0 --net=host -e PS_HOSTS="clustered-tensorflow-ps0:2222,clustered-tensorflow-ps1:2222" -e WORKER_HOSTS="clustered-tensorflow-worker0:2222,clustered-tensorflow-worker1:2222" -e JOB_NAME="worker" -e TASK_INDEX="0" -p 2222:2222 fluxcapacitor/clustered-tensorflow
```

### Verify Successful Start through Logs
```
docker logs -f clustered-tensorflow-worker0

### EXPECTED OUTPUT ###
...
TODO
...
```

## Worker 1
### Start Docker Container
```          
docker run -itd --name=clustered-tensorflow-worker1 --net=host -e PS_HOSTS="clustered-tensorflow-ps0:2222,clustered-tensorflow-ps1:2222" -e WORKER_HOSTS="clustered-tensorflow-worker0:2222,clustered-tensorflow-worker1:2222" -e JOB_NAME="worker" -e TASK_INDEX="1" -p 2222:2222 fluxcapacitor/clustered-tensorflow
```

### Verify Successful Start through Logs
```
docker logs -f clustered-tensorflow-worker1

### EXPECTED OUTPUT ###
...
TODO
...
```

## JupyterHub Master
### TODO:
