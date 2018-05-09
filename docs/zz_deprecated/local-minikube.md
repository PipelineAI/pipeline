## Step 1: Setup Kubernetes CLI (`kubectl`)
Follow [THESE](https://kubernetes.io/docs/tasks/tools/install-kubectl/) instructions to setup the Kubernetes CLI.

## Step 2: Verify Kubernetes CLI Installation
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

## Step 3: Setup Minikube Locally
Follow [THESE](https://github.com/kubernetes/minikube/releases/) instructions to setup Minikube in your environment.

Remove Any Existing Minikube VMs
```
minikube delete
```

Start New Minikube VM
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

## Step 4: Verify Minikube Installation
View Configuration
```
kubectl config view

### EXPECTED OUTPUT (OR SIMILAR) ###
apiVersion: v1
clusters: 
- cluster:
    certificate-authority: /Users/cfregly/.minikube/ca.crt
    server: https://192.168.99.100:8443
  name: minikube
contexts: 
- context:
    cluster: minikube
    user: minikube
  name: minikube
current-context: minikube
kind: Config
preferences: {}
- name: minikube
  user:
    client-certificate: /Users/cfregly/.minikube/client.crt
    client-key: /Users/cfregly/.minikube/client.key
```

View All Nodes
```
kubectl get nodes

### EXPECTED OUTPUT ###
NAME       STATUS    AGE
minikube   Ready     32m
```

View Minikube IP Address 
```
minikube ip

### EXPECTED OUTPUT ###
192.168.99.100
```

Get All Services
```
minikube service list

## EXPECTED OUTPUT
|-------------|----------------------|---------------------------|
|  NAMESPACE  |         NAME         |              URL          |
|-------------|----------------------|---------------------------|
| kube-system | kubernetes-dashboard | http://192.168.99.100:... | <- Dashboard
|-------------|----------------------|---------------------------|
```

Open Kubernetes Dashboard
* Navigate to the `kubernetes-dashboard` IP/Port from the table above
```
http://<ip-address>:<port>
```

## Step 5: Train and Serve ML/AI Models with PipelineAI
Follow [THESE](https://github.com/PipelineAI/pipeline/) instructions to train and serve models with PipelineAI.

## Step 6: (Optional) Configure Minikube to Use [Default Local Docker Registry](https://github.com/kubernetes/minikube/blob/master/docs/reusing_the_docker_daemon.md)
_This will make building and deploying new Docker images much faster on a local machine._
```
eval $(minikube docker-env)

docker ps

### EXPECTED OUTPUT ###
...
e326c964b54a        gcr.io/google_containers/pause-amd64:3.0               "/pause"                 2 minutes ago       Up 2 minutes                            k8s_POD_jupyterhub-5f86f64bb7-jwncp_default_43f487db-cfc8-11e7-b5d6-08002759b1cb_0
7d0151d66dd5        gcr.io/google_containers/k8s-dns-sidecar-amd64         "/sidecar --v=2 --lo…"   4 minutes ago       Up 4 minutes                            k8s_sidecar_kube-dns-6fc954457d-vkkv5_kube-system_e8d7da36-cfc7-11e7-b5d6-08002759b1cb_0
c3b91ec1a94f        gcr.io/google_containers/k8s-dns-dnsmasq-nanny-amd64   "/dnsmasq-nanny -v=2…"   4 minutes ago       Up 4 minutes                            k8s_dnsmasq_kube-dns-6fc954457d-vkkv5_kube-system_e8d7da36-cfc7-11e7-b5d6-08
...
```
