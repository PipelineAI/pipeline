## Create Kubernetes Cluster
We recommend [JenkinsX](https://jenkins-x.io/getting-started/create-cluster/) to install on AWS, Azure, Google Cloud, or On-Premise.

## Install PipelineAI on Kubernetes
### Prerequisites
* Running Kubernetes Cluster
* Python 2 or 3 (Conda with Python 3 is Preferred)

# Install [PipelineAI CLI](../README.md#install-pipelinecli)
* Click [**HERE**](../README.md#install-pipelinecli) to install the PipelineAI CLI

### AWS IAM Roles (AWS-Only)
Make sure the underlying EC2 instances for your EKS cluster contain the `AmazonEC2ContainerRegistryPowerUser` instance policy.   See [here](https://aws.amazon.com/blogs/security/easily-replace-or-attach-an-iam-role-to-an-existing-ec2-instance-by-using-the-ec2-console/) and [here](https://eksworkshop.com/logging/prereqs/) for more info.

### Choose a "PipelineAI Admin" Node from the Available Worker Nodes.
```
kubectl get node

NAME                                          STATUS    ROLES     AGE       VERSION
<node1>                                       Ready     <none>    24m       v1.10.3
<node2>                                       Ready     <none>    24m       v1.10.3
```

### Create the Cluster 
_This requires `cli-pipeline>=1.5.261`.  Click [here](https://github.com/PipelineAI/pipeline/blob/master/docs/quickstart/README.md#install-pipelinecli) to install the PipelineAI CLI._

PipelineAI CLI Args
* `admin-name`:  Designate one of the worker nodes as the "PipelineAI Admin" node
* `service-type`:  "nodeport" (ie. Local, On-Premise install) or "loadbalancer" (ie. Cloud install).  (Default `NodePort`)
* `image-registry-url`:  URL to the PipelineAI Docker images (Default 'docker.io')
```
pipeline cluster_kube_install \
  --tag 1.5.0 \
  --admin-node <node1-or-node2> \
  --ingress-type <nodeport or loadbalancer> \
  --image-registry-url docker.io
```
Notes:  
* If you see logs of `Evicted` or `Pending` nodes, you may need to increase the instance size (memory and cpu) and/or increase the capacity of your EBS volumes.  Use `kubectl describe pod <Evicted-or-Pending-pod-name>` to identify the underlying issue.


### Retrieve the Ingress IP (NodePort) or DNS Name (LoadBalancer) 
Note: the LoadBalancer DNS may take some time to propagate.
```
kubectl get svc istio-ingressgateway -o wide

NAME                   TYPE                       CLUSTER-IP      EXTERNAL-IP  
istio-ingressgateway   <NodePort/LoadBalancer>    10.100.12.101   <dns-name>
```

### Whitelist the DNS Name with PipelineAI
_Email [contact@pipeline.ai](mailto:contact@pipeline.ai) to whitelist your DNS name with PipelineAI._
