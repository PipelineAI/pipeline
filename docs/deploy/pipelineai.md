# YOU MUST BE RUNNING WITHIN THE FOLLOWING DOCKER CONTAINER: 

## Step 0: [Docker and the Kubernetes CLI](Setup-Docker-and-Kubernetes-CLI) 

### Pre-requisites
* You are setup and running with the [Docker and the Kubernetes CLI](Setup-Docker-and-Kubernetes-CLI) mentioned above ^^^ 
* You are **inside** the running `fluxcapacitor/kubernetes:<suggested-tag>` Docker Container from the previous pre-req (this page will provide the <suggested-tag>)
* You have created a Kubernetes Cluster with [AWS](Setup-Pipeline-AWS), [Google Cloud](Setup-Pipeline-Google), or [Azure](Setup-Pipeline-Azure), or [Local](Setup-Pipeline-Mini) Laptop.

### Verify Environment
Verify `kubectl` is Installed
```
which kubectl
```

### Verify Context
```
kubectl config current-context

### EXPECTED OUTPUT ###
# <your-cluster-name>
```

## Step 1: Create PipelineIO Cluster
```
export PIO_VERSION=v1.2.0

wget -O - https://raw.githubusercontent.com/fluxcapacitor/pipeline/$PIO_VERSION/scripts/cluster/deploy | PIO_COMMAND=create bash

wget -O - https://raw.githubusercontent.com/fluxcapacitor/pipeline/$PIO_VERSION/scripts/cluster/svc | PIO_COMMAND=create bash
```
* **These ^^^ may take some time as the Docker images download from DockerHub.**

## Step 2: Verify Successful PipelineIO Deployment
```
kubectl get pod
```
```
kubectl get deploy 
```
```
kubectl get svc 
```

### Wait for All Pipeline Services to Start
```
kubectl get svc -w
```
## Step 3: Navigate to Jupyter Notebook and Start Coding!
### Get the Jupyter Service IP or Hostname
```
kubectl describe svc jupyterhub

### EXPECTED OUTPUT ###
Name:			jupyterhub
Namespace:		default
Labels:			<none>
Selector:		name=jupyterhub
Type:			LoadBalancer
IP:			100.67.118.143
LoadBalancer Ingress:	<jupyter-external-service-ip-or-hostname>      <-- COPY THIS!
Port:			http	80/TCP
NodePort:		http	31594/TCP
Endpoints:		100.96.1.8:8754
Session Affinity:	None
Events:
  FirstSeen	LastSeen	Count	From			SubObjectPath	Type		Reason			Message
  ---------	--------	-----	----			-------------	--------	------			-------
  10m		10m		1	{service-controller }			Normal		CreatingLoadBalancer	Creating load balancer
  10m		10m		1	{service-controller }			Normal		CreatedLoadBalancer	Created load balancer
```
### Navigate to Jupyter Notebook
```
http://<jupyter-external-service-ip-or-hostname>
```
* **Note:  Use any `username`.  No `password` is required.**

## (Optional) Setup a DNS Subdomain for Each Pipeline Service using Route53, GoDaddy, etc.
* List all services from Kubernetes
```
kubectl get svc
```

* Describe each service (ie. `jupyterhub`) to see the entire IP or Hostname
```
kubectl describe svc jupyterhub

### EXPECTED OUTPUT ###
# Name:			jupyterhub
# Namespace:		default
# Labels:		<none>
# Selector:		name=jupyterhub
# Type:			LoadBalancer
# IP:			...
# LoadBalancer Ingress:	<jupyter-external-service-ip-or-hostname>    <-- COPY THIS!
# Port:			http	80/TCP
# NodePort:		http	32120/TCP
# Endpoints:		...:8754
# Session Affinity:	None
# No events.
```

* Set the `<service-name>.<your-cluster-name-with-fully-qualified-DNS-name>` = `<jupyter-external-service-ip-or-hostname>`

## Model Predictions
Follow similar steps above to find the external `model-server-url` for the [Prediction Service](http://pipeline.io/model_deploy/) you are using in your cluster (ie. Scikit-Learn, R, Spark, TensorFlow, etc).

For example, to find the `model-server-url` in your cluster for Scikit-Learn, you would do the following:
```
kubectl get svc -o wide | grep scikit

### EXPECTED OUTPUT ###
prediction-scikit          <internal-ip>   <external-ip-or-hostname>    80:30380/TCP,8080:32290/TCP                                                                                             23d       app=prediction-scikit
```

Copy/paste the `<external-ip-or-hostname>` for the Scikit-Learn [Prediction Service](http://pipeline.io/model_deploy/) `model-server-url` in your cluster.

## [More Kubernetes Commands](Kubernetes-Commands)

## Undeploy PipelineIO Services
```
export PIO_VERSION=v1.2.0 && wget -O - https://raw.githubusercontent.com/fluxcapacitor/pipeline/$PIO_VERSION/scripts/cluster/deploy | PIO_COMMAND=delete bash
```
