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

## Step 3: Setup [PipelineAI](https://github.com/fluxcapacitor/pipeline/wiki/PipelineIO-on-Local-Minikube) on Local Minikube
