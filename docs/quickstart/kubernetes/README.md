## Create Kubernetes Cluster
We recommend [**Jenkins-X**](https://jenkins-x.io/getting-started/install/) to install on AWS/EKS, Azure/AKS, Google Cloud/GKE, or On-Premise.

_Note: When using AWS EKS, make sure you allocate 100GB to the root volume ephemeral storage - or you will see lots of `Evicted` pods.  The default of 20GB is not enough for the Docker images._

Here is a sample command using [Jenkins-X](https://jenkins-x.io/commands/jx_create_cluster_eks/) with AWS EKS:
```
jx create cluster eks --node-type=<instance-type> --node-volume-size=100 --verbose=true --cluster-name=pipelineai --install-dependencies=true --skip-ingress=true --skip-installation=true --nodes=1 --eksctl-log-level=5 --version=1.11
```
Notes:
* Use `--ssh-public-key=/path/to/public/key.pub` to enable ssh'ing to the worker.  
* You may also need to open port 22 on the worker node security group.

## Install PipelineAI on Kubernetes
### Prerequisites
* Running Kubernetes Cluster
* Kubectl Installed
* MacOS, Linux, or Powershell (Windows)
* Python 2 or 3 ([Miniconda with Python 3.6](https://repo.continuum.io/miniconda/) is Preferred)
* [**PipelineAI CLI**](../README.md#install-pipelinecli)
* [**Helm**](https://docs.helm.sh/using_helm/#installing-helm)
* 4 CPU
* 8GB RAM
* (Optional) Nvidia GPU

### Install [PipelineAI CLI](../README.md#install-pipelinecli)
* Click [**HERE**](../README.md#install-pipelinecli) to install the PipelineAI CLI

### Install [Helm CLI](https://docs.helm.sh/using_helm/#installing-helm)
* Click [**HERE**](https://docs.helm.sh/using_helm/#installing-helm) to install the Helm CLI or run the following command:
```
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get | bash
```

Initialize Helm (and Start Tiller)
```
helm init
```

Update to the latest Helm Charts
```
helm repo update
```

Verify Helm and Tiller are Running
```
kubectl get deploy tiller-deploy -n kube-system

### EXPECTED OUTPUT ###

NAME            DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
tiller-deploy   1         1         1            1           1h     <== MUST WAIT FOR AVAILABLE 1 (not 0)
```
Notes:
* You must wait for tiller-deploy to become AVAILABLE (1)

### AWS IAM Roles (AWS-Only)
Make sure the underlying EC2 instances for your EKS cluster contain the `AmazonEC2ContainerRegistryPowerUser` instance policy.   See [here](https://aws.amazon.com/blogs/security/easily-replace-or-attach-an-iam-role-to-an-existing-ec2-instance-by-using-the-ec2-console/) and [here](https://eksworkshop.com/logging/prereqs/) for more info.

### Create and Switch to a Namespace (or just use `default`)
```
kubectl create namespace default

kubectl config set-context $(kubectl config current-context) --namespace=default
```

### (Optional) Enable Kubernetes to Recognize GPUs
Notes:
* Currently, EKS only supports p2 and p3 [GPU instance types](https://aws.amazon.com/marketplace/pp/B07GRHFXGM)
* For GPU with EKS, YOU MUST ACCEPT THE AMI TERMS [HERE](https://aws.amazon.com/marketplace/pp/B07GRHFXGM)
* You may need to change the version above (ie. v1.12).  Run `kubectl version` and verify the `Server Version` of Kubernetes

(Optional) Apply the Nvidia GPU plugin
```
kubectl apply -f https://raw.githubusercontent.com/NVIDIA/k8s-device-plugin/v1.11/nvidia-device-plugin.yml
```
After a few minutes, verify the Nvidia GPUs within Kubernetes:
```
kubectl get nodes "-o=custom-columns=NAME:.metadata.name,MEMORY:.status.allocatable.memory,CPU:.status.allocatable.cpu,GPU:.status.allocatable.nvidia\.com/gpu"

### EXPECTED OUTPUT ###
NAME                                            MEMORY        CPU      GPU
ip-192-168-101-177.us-west-2.compute.internal   251643680Ki   4        1      <== MUST WAIT FOR GPU (1)
ip-192-168-196-254.us-west-2.compute.internal   251643680Ki   4        1
```

### Create the Cluster 
PipelineAI CLI Args
* `--image-registry-url`: Your Docker Registry URL for images created by PipelineAI (ie. ECR, DockerHub, etc)
* `--image-registry-username`: (Optional) Leave blank if your Docker Registry is managed by IAM Policies/Roles (ie. ECR)
* `--image-registry-password`: (Optional) Leave blank if your Docker Registry is managed by IAM Policies/Roles (ie. ECR)
* `--users-storage-gb`: (Optional) Size of persistent volume (Default `10Gi`)
* `--ingress-type`: (Optional) `nodeport` or `loadbalancer` (Default `nodeport`)
```
pipeline cluster-kube-install --tag 1.5.0 --chip=cpu --namespace=default --image-registry-url=<your-docker-registry-url> --image-registry-username=<optional> --image-registry-password=<optional> --users-storage-gb=10Gi --ingress-type=<nodeport or loadbalancer>
```
Notes:  
* Add `--dry-run=True` to generate and inspect the yaml generated in $HOME/.pipelineai
* Use `--chip=gpu` to install the GPU version of PipelineAI
* If you see logs of `Evicted` or `Pending` nodes, you may need to increase the instance size (memory and cpu) and/or increase the capacity of your EBS volumes.  Use `kubectl describe pod <Evicted-or-Pending-pod-name>` to identify the underlying issue.

### Retrieve the Ingress IP (NodePort) or DNS Name (LoadBalancer) 
```
kubectl get svc istio-ingressgateway -o wide

NAME                   TYPE                       CLUSTER-IP      EXTERNAL-IP  
istio-ingressgateway   <NodePort/LoadBalancer>    10.100.12.101   <dns-name>
```
Notes:
* The LoadBalancer DNS may take some time to propagate.
* For EKS, you can modify the istio loadbalancer yaml to include the `service.beta.kubernetes.io/aws-load-balancer-internal: 0.0.0.0/0` annotation to enforce internal LoadBalancer (vs. public-facing, external)
* To use SSL with your ELB, you will need to upload a certificate, add a listener to the ELB configured with SSL on load balancer port 443 targeting instance port 31380

### (Optional) Scale the Cluster/Node Group
Get the Node Group name using `eksctl`
```
eksctl get nodegroup
```

Scale the Node Group from 1 to 2 Nodes
```
eksctl scale nodegroup --name <node-group> --cluster pipelineai --nodes <num-nodes>
```

### Uninstall and Cleanup
```
pipeline cluster-kube-uninstall --tag=1.5.0 --chip=cpu
```
Notes:
* Use `--chip=gpu` for a GPU installation
