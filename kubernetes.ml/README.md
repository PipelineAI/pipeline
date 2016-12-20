## Prerequisites
* Running Kuberentes Cluster
* Follow [these](https://github.com/fluxcapacitor/pipeline/tree/master/kubernetes.ml#setup-a-new-kubernetes-cluster) instructions to create a Kuberentes Cluster on AWS

## Install Kubernetes Client
### OPTION 1:  Install Kubernetes Client through Docker
* Download Docker v1.12+ for Mac or Windows
* **Note:  MUST BE VERSION 1.12+**
```
https://www.docker.com/products/docker
```
```
docker run -itd --name=kubernetes --privileged --net=host fluxcapacitor/kubernetes
```
```
docker exec -it kubernetes bash
```

### OPTION 2:  Install Kubernetes Client on Local Laptop
* OS X
```
export KUBERNETES_VERSION=1.4.1

curl -O https://storage.googleapis.com/kubernetes-release/release/v${KUBERNETES_VERSION}/bin/darwin/amd64/kubectl

chmod a+x kubectl

sudo mv kubectl /usr/local/bin/kubectl
```

* Linux
```
export KUBERNETES_VERSION=1.4.1

curl -O https://storage.googleapis.com/kubernetes-release/release/v${KUBERNETES_VERSION}/bin/linux/amd64/kubectl

chmod a+x kubectl

sudo cp kubectl /usr/local/bin/kubectl
```

* Windows

**See OPTION 1 above.  You must use Docker!**

## Configure Kubernetes Client
* At this point, you should have `kubectl` installed and configured 
* You are either inside the Docker image (OPTION 1 above) or have `kubectl` on your PATH (OPTION 2 above).
* The following should return a path including the `kubectl` script
```
which kubectl
```

### Setup User
* You will need `KUBERNETES_CLUSTER_IP` from the Kubernetes Cluster (See Prerequisites)
```
kubectl config set-cluster aws_k8s_training --insecure-skip-tls-verify=true --server=https://<KUBERNETES_CLUSTER_IP>
```

### Setup User
* You will need `KUBERNETES_ADMIN_PASSWORD` from the Kubernetes Cluster (See Prerequisites)
```
kubectl config set-credentials aws_k8s_training --username=admin --password=<KUBERNETES_ADMIN_PASSWORD>
```

### Create Namespace
* Use a unique `USERNAME`
```
kubectl create namespace <USERNAME>
```

### Setup Context
```
kubectl config set-context aws_k8s_training --cluster=aws_k8s_training --user=aws_k8s_training --namespace=<USERNAME>
```

### Switch to Newly-Created Context
```
kubectl config use-context aws_k8s_training
```

### Verify Context
```
kubectl config current-context
```

### View Kuberentes Client Config
```
kubectl config view
```

## Explore Kubernetes Cluster
### View Physical Cloud Instances
```
kubectl get nodes
```

### View Cluster Info and Admin URLs
```
kubectl cluster-info
```

### View Kubernetes Dashboard
```
https://<KUBERNETES-CLUSTER-IP>/api/v1/proxy/namespaces/kube-system/services/kubernetes-dashboard/#/workload
```

## Kubernetes Fu
### Bash into Live Docker Container
* This assumes 1 Docker Container per Pod
```
kubectl get pod
```
```i
kubectl exec <pod-name> -it -- bash -il
```

### Scale ReplicationController through Command Line
```
kubectl get rc
```
```
kubectl scale --replicas=4 rc <rc-name>
```

### Scale ReplicationController through Kubernetes Admin UI (Weave Scope)
```
kubectl get svc
```
```
https://<KUBERNETES-ADMIN-UI-WEAVESCOPE-HOST-IP>
```

### Rolling Update of ReplicationController
```
kubectl get rc
```
```
kubectl rolling-update <rc-name> -f <new-rc-descriptor>.yaml
```

### Auto-scale Spark Worker using CPU
```
kubectl get rc
```
```
kubectl autoscale rc <rc> --max=4 --cpu-percent=50
```

## Complete Example
![PipelineIO](http://pipeline.io/images/pipeline-io-logo-shadow-210x186.png)

### 100% Open Source, Continuously Train + Deploy Spark ML + Tensorflow AI Pipelines
* Click [here](https://github.com/fluxcapacitor/pipeline-training/wiki) for a complete Spark, Zeppelin, Jupyter, Kafka, and TensorFlow environment using Kubernetes and Netflix Open Source!

## Setup a New Kubernetes Cluster

### Note:  If you already have a Kuberentes Cluster (ie. at a Training Workshop), skip these steps!

### Start and Shell into the Docker Container with Kubernetes Admin CLI Installed
```
docker run -itd --name=kubernetes-admin --privileged --net=host fluxcapacitor/kubernetes-admin bash
```
```
docker exec -it kubernetes-admin bash
```

### Setup AWS Credentials Within Kuberentes Admin Docker Container
```
aws configure
```

### Configure Kubernetes to Create a New Cluster on AWS
* Update `NUM_NODES`, `NODE_SIZE`, and autoscale config params accordingly
```
export KUBERNETES_PROVIDER=aws
export KUBE_AWS_ZONE=us-west-2b
export AWS_S3_REGION=us-west-2
export AWS_S3_BUCKET=<s3-bucket>
export KUBE_AWS_INSTANCE_PREFIX=k8s-<YOUR_USERNAME>
export KUBE_ENABLE_CLUSTER_MONITORING=influxdb
export KUBE_ENABLE_NODE_LOGGING=true
export KUBE_LOGGING_DESTINATION=elasticsearch
export KUBE_ENABLE_CLUSTER_DNS=true
export NUM_NODES=1
export MASTER_SIZE=m3.medium
export NODE_SIZE=r3.2xlarge
export KUBE_ENABLE_CLUSTER_AUTOSCALER=true
export KUBE_AUTOSCALER_MIN_NODES=1
export KUBE_AUTOSCALER_MAX_NODES=2
export KUBE_TARGET_NODE_UTILIZATION=0.85
```

### Create the Kubernetes Cluster on AWS (~10-15 mins)
```
$KUBERNETES_HOME/cluster/kube-up.sh
```

### Return to Configure Kubernetes Client
* Return [here](https://github.com/fluxcapacitor/pipeline/tree/master/kubernetes.ml#install-kubernetes-client)
