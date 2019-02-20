## Create Kubernetes Cluster
We recommend [**Jenkins-X**](https://jenkins-x.io/getting-started/install/) to install on AWS/EKS, Azure/AKS, Google Cloud/GKE, or On-Premise.

_Note: When using AWS EKS, make sure you allocate 100GB to the root volume ephemeral storage - or you will see lots of `Evicted` pods.  The default of 20GB is not enough to store the Docker images on each node._

Here is a sample command using [Jenkins-X](https://jenkins-x.io/commands/jx_create_cluster_eks/) with AWS EKS:
```
jx create cluster eks --node-type=<instance-type> --node-volume-size=100 --verbose=true --cluster-name=pipelineai --install-dependencies=true --skip-ingress=true --skip-installation=true --nodes=1 --eksctl-log-level=5
```
Notes:
* Use `--ssh-public-key=/path/to/public/key.pub` to ssh into the worker.  You may also need to open port 22 on the worker node security group.

## Install PipelineAI on Kubernetes
### Prerequisites
* Running Kubernetes Cluster
* Python 2 or 3 ([Miniconda with Python 3.6](https://repo.continuum.io/miniconda/) is Preferred)
* [**PipelineAI CLI**]()
* [**Helm**](https://docs.helm.sh/using_helm/#installing-helm)

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
tiller-deploy   1         1         1            1           1h
```

### AWS IAM Roles (AWS-Only)
Make sure the underlying EC2 instances for your EKS cluster contain the `AmazonEC2ContainerRegistryPowerUser` instance policy.   See [here](https://aws.amazon.com/blogs/security/easily-replace-or-attach-an-iam-role-to-an-existing-ec2-instance-by-using-the-ec2-console/) and [here](https://eksworkshop.com/logging/prereqs/) for more info.

### Switch to `default` Namespace
```
kubectl config set-context $(kubectl config current-context) --namespace=default
```

### Create the Cluster 
PipelineAI CLI Args
* `--image-registry-url`: Your Docker Registry URL for images created by PipelineAI (ie. ECR, DockerHub, etc)
* `--image-registry-username`: (Optional) Leave blank if your Docker Registry is managed by IAM Policies/Roles (ie. ECR)
* `--image-registry-password`: (Optional) Leave blank if your Docker Registry is managed by IAM Policies/Roles (ie. ECR)
* `--ingress-type`:  (Optional) "nodeport" or "loadbalancer" (Default `nodeport`)
```
pipeline cluster-kube-install --tag 1.5.0 --ingress-type=<nodeport or loadbalancer> --image-registry-url=<your-docker-registry-url> --image-registry-username=<optional> --image-registry-password=<optional> 
```
Notes:  
* If you see logs of `Evicted` or `Pending` nodes, you may need to increase the instance size (memory and cpu) and/or increase the capacity of your EBS volumes.  Use `kubectl describe pod <Evicted-or-Pending-pod-name>` to identify the underlying issue.

### Retrieve the Ingress IP (NodePort) or DNS Name (LoadBalancer) 

Notes: 
* The LoadBalancer DNS may take some time to propagate.
* For EKS, we've applied the `service.beta.kubernetes.io/aws-load-balancer-internal: 0.0.0.0/0` annotation to enforce internal  LoadBalancer vs. external LoadBalancer.

```
kubectl get svc istio-ingressgateway -o wide

NAME                   TYPE                       CLUSTER-IP      EXTERNAL-IP  
istio-ingressgateway   <NodePort/LoadBalancer>    10.100.12.101   <dns-name>
```

Notes:
* To use SSL with your ELB, you will need to upload a certificate, add a listener to the ELB configured with SSL on load balancer port 443 targeting instance port 31380

### Whitelist the DNS Name with PipelineAI
Email [**contact@pipeline.ai**](mailto:contact@pipeline.ai) to whitelist your DNS name with PipelineAI.

### Scale the Cluster/Node Group (AWS EKS Example is Provided)
Get the Node Group name
```
eksctl get nodegroup
```

Scale the Node Group from 1 to 2 Nodes
```
eksctl scale nodegroup --name <node-group> --cluster pipelineai --nodes <num-nodes>
```

### Uninstall and Cleanup
```
pipeline cluster-kube-uninstall --tag 1.5.0
```
