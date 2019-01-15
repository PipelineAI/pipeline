### Full sync

### IAM Roles
Make sure the underlying EC2 instances for your EKS cluster contain the AmazonEC2ContainerRegistryPowerUser instance policy.

See [here](https://aws.amazon.com/blogs/security/easily-replace-or-attach-an-iam-role-to-an-existing-ec2-instance-by-using-the-ec2-console/) and [here](https://eksworkshop.com/logging/prereqs/) for more info.

### Choose a node as an "admin" node.
```
kubectl get node

NAME                                          STATUS    ROLES     AGE       VERSION
<node1>                                       Ready     <none>    24m       v1.10.3
<node2>                                       Ready     <none>    24m       v1.10.3
```

### Create the cluster by specifying the admin node from above
* Requires `cli-pipeline>=1.5.243`
```
pipeline _cluster_kube_create --tag 1.5.0 --admin-node <node1 or node2>
```

### Retrieve the ELB DNS name
Note: the ELB DNS may take some time to propagate.
```
kubectl get svc istio-ingressgateway -o wide

NAME                   TYPE           CLUSTER-IP      EXTERNAL-IP  
istio-ingressgateway   LoadBalancer   10.100.12.101   <dns-name>  
```

### Whitelist the DNS Name with PipelineAI (Temporary)
Notify chris@pipeline.ai to whitelist the <dns-name> above.
