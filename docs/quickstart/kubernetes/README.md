## Create Kubernetes Cluster
We recommend [**JenkinsX**](https://jenkins-x.io/getting-started/create-cluster/) to install on AWS, Azure, Google Cloud, or On-Premise.

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
Note: the LoadBalancer DNS may take some time to propagate.
```
kubectl get svc istio-ingressgateway -o wide

NAME                   TYPE                       CLUSTER-IP      EXTERNAL-IP  
istio-ingressgateway   <NodePort/LoadBalancer>    10.100.12.101   <dns-name>
```

### Whitelist the DNS Name with PipelineAI
Email [**contact@pipeline.ai**](mailto:contact@pipeline.ai) to whitelist your DNS name with PipelineAI.

### Uninstall and Cleanup
```
pipeline cluster-kube-uninstall --tag 1.5.0
```
