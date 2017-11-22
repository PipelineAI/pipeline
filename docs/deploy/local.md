## Step 1: Setup [`kubectl`](http://kubernetes.io/docs/user-guide/prereqs/)
Follow [**THESE**](https://kubernetes.io/docs/tasks/tools/install-kubectl/) instructions to setup the Kubernetes CLI.

### Verify Kubernetes CLI Installation
```
kubectl config view

### EXPECTED OUTPUT (OR SIMILAR) ###
apiVersion: v1
clusters: []
contexts: []
current-context: ""
kind: Config
preferences: {}
users: []
```

## Step 2: Setup Minikube Locally
Follow [**THESE**](https://github.com/kubernetes/minikube/releases/) instructions to setup Minikube in your environment.

### Start Minikube
Reset Any Existing Minikube VMs
```
minikube delete
```

### Start New Minikube VM
```
minikube start --disk-size=100g --memory=8192 --kubernetes-version=v1.8.0

## EXPECTED OUTPUT
Starting local Kubernetes v1.8.0 cluster...
Starting VM...
Moving files into cluster...
Downloading localkube binary
...
Starting local Kubernetes cluster...
Starting VM...
SSH-ing files into VM...
Setting up certs...
Starting cluster components...
Connecting to cluster...
Setting up kubeconfig...
Kubectl is now configured to use the cluster.
```

### (Optional) Configure Minikube to [Use Existing Docker Daemon](https://github.com/kubernetes/minikube/blob/master/docs/reusing_the_docker_daemon.md)
```
eval $(minikube docker-env)
```

## Step 3: Setup [PipelineAI]on Local Minikube
### Install JupyterHub
```
echo '...JupyterHub...'
kubectl create -f https://raw.githubusercontent.com/fluxcapacitor/pipeline/v1.2.0/jupyterhub.ml/jupyterhub-deploy.yaml
kubectl create -f https://raw.githubusercontent.com/fluxcapacitor/pipeline/v1.2.0/jupyterhub.ml/jupyterhub-svc.yaml
```
**^^^ IGNORE `runtime.Unknown` ERROR ^^^**

**This may take a _long_ time as Docker images are downloaded from DockerHub.**

### Model Predictions
If you want to predict with PipelineAI Local, you will need to deploy the prediction services which will likely not fit on your local laptop.

You can try to follow the instructions for the Distributed, Cloud-Based versions of PipelineAI to test [predictions](https://github.com/fluxcapacitor/pipeline/wiki/Setup-Pipeline-on-Kubernetes#model-predictions).

### Get All Nodes
```
kubectl get nodes

### EXPECTED OUTPUT ###
NAME       STATUS    AGE
minikube   Ready     32m
```

### Get Minikube IP 
```
minikube ip

### EXPECTED OUTPUT ###
192.168.99.100
```

### Get All Services
```
minikube service list

## EXPECTED OUTPUT
|-------------|----------------------|---------------------------|
|  NAMESPACE  |         NAME         |              URL          |
|-------------|----------------------|---------------------------|
| default     | jupyterhub           | http://192.168.99.100:... | <- Jupyter 
|             |                      | http://192.168.99.100:... | <- Tensorboard
|             |                      |                           |
| kube-system | kubernetes-dashboard | http://192.168.99.100:... | <- Dashboard
|-------------|----------------------|---------------------------|
```

### Open Kubernetes Dashboard
* Navigate to the `kubernetes-dashboard` IP/Port from Table [Above](Pipeline-Mini#list-service-ipsports)
```
http://<ip-address>:<port>
```
![Kubernetes Dashboard](https://s3.amazonaws.com/fluxcapacitor.com/img/kubernetes-dashboard-mini-2.png)

### Open Jupyter Notebook
* Navigate to the `juptyerhub` IP/Port from Table [Above](Pipeline-Mini#list-service-ipsports)
```
http://<ip-address>:<port>
```
![JupyterHub](https://s3.amazonaws.com/fluxcapacitor.com/img/jupyterhub-login-mini.png)
```
username:  <-- ANYTHING YOU WANT
password:  <-- LEAVE EMPTY!
```
