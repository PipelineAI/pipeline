Based on [this](https://github.com/fission/fission.git) GitHub Repo

# Setup Fission on Cloud-based Kubernetes

## Deploy Fission
```
kubectl create -f fission/fission.yaml
kubectl create -f fission/fission-cloud.yaml
kubectl create -f fission/fission-logger.yaml
kubectl create -f fission-ui/docker/fission-ui.yaml
```

## Download `fission` within [PipelineIO Kubernetes CLI](https://github.com/fluxcapacitor/pipeline/wiki/Setup-Docker-and-Kubernetes-CLI) Docker Container
```
curl http://fission.io/linux/fission > fission && chmod +x fission && mv fission /usr/local/bin/
```

## Export Environment Variables
Wait for services to acquire a hostname or IP.
### AWS
```
export FISSION_URL=http://$(kubectl fission get svc controller -o=jsonpath='{..hostname}')
export FISSION_ROUTER=$(kubectl fission get svc router -o=jsonpath='{..hostname}')
```

### GCP
```
export FISSION_URL=http://$(kubectl get svc controller -o=jsonpath='{..ip}')
export FISSION_ROUTER=$(kubectl fission get svc router -o=jsonpath='{..ip}')
```

# Setup Fission on Local Minikube Kubernetes
## Download `fission` CLI
### Mac
```
cd /tmp
curl http://fission.io/mac/fission > fission && chmod +x fission && sudo mv fission /usr/local/bin/
```

### Windows
```
TODO:
```

## Export Environment Variables
```
export FISSION_URL=http://$(minikube ip):31313
export FISSION_ROUTER=$(minikube ip):31314
```

## Deploy Fission
```
kubectl create -f fission/fission.yaml
kubectl create -f fission/fission-nodeport.yaml
kubectl create -f fission/fission-logger.yaml
kubectl create -f fission-ui/docker/fission-ui.yaml
```
## Navigate to Fission UI
### Get Fission UI Service URL
Wait for services to acquire a hostname or IP.

```
minikube service list

### EXPECTED OUTPUT ###
|-------------|------------|------------------------------|
|  NAMESPACE  |  NAME      |              URL             |
|-------------|------------|------------------------------|
| ...         |            |                              |
| fission     | controller | http://192.168.99.100:31313  |
| fission     | etcd       | No node port                 |
| fission     | fission-ui | http://192.168.99.100:31319  | <- Fission UI
| fission     | influxdb   | http://192.168.99.100:31315  |
| fission     | poolmgr    | No node port                 |
| fission     | router     | http://192.168.99.100:31314  |
| ...         |            |                              |
|-------------|------------|------------------------------|
```

## Remove Fission
```
kubectl delete -f fission/fission.yaml
kubectl delete -f fission/fission-logger.yaml
kubectl delete -f fission-ui/docker/fission-ui.yaml
```
### Cloud Only
```
kubectl delete -f fission/fission-cloud.yaml 
```

### Minikube Only
```
kubectl delete -f fission/fission-nodeport.yaml 
```
