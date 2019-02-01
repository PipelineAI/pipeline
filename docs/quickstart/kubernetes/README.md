## Create Kubernetes Cluster
We recommend [JenkinsX](https://jenkins-x.io/getting-started/create-cluster/) to install on AWS, Azure, Google Cloud, or On-Premise.

## Install PipelineAI on Kubernetes
### Prerequisites
* Running Kubernetes Cluster
* Python 2 or 3 (Conda with Python 3 is Preferred)
* [PipelineAI CLI]()
* [Helm](https://docs.helm.sh/using_helm/#installing-helm)

### Install [PipelineAI CLI](../README.md#install-pipelinecli)
* Click [**HERE**](../README.md#install-pipelinecli) to install the PipelineAI CLI

### Install [Helm CLI](https://docs.helm.sh/using_helm/#installing-helm)
* Click [**HERE**](https://docs.helm.sh/using_helm/#installing-helm) to install the Helm CLI or run the following command:
```
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get | bash
```

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
PipelineAI CLI Args
* `--admin-node`:  Designate one of the worker nodes as the "PipelineAI Admin" node
* `--image-registry-url`:  (Optional) URL to the PipelineAI Docker images (Default 'docker.io')
* `--ingress-type`:  (Optional) "nodeport" or "loadbalancer" (Default `NodePort`)
* `--namespace`: (Optional) Kubernetes namespace (Default 'default')
```
pipeline cluster_kube_install --tag 1.5.0  --image-registry-url=docker.io --admin-node=<node1-or-node2> --ingress-type=<nodeport or loadbalancer> --namespace=default
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
