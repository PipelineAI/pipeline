### Choose a node as the "admin" node.
```
kubectl get node

NAME                                          STATUS    ROLES     AGE       VERSION
<node1>                                       Ready     <none>    24m       v1.10.3
<node2>                                       Ready     <none>    24m       v1.10.3
```

### Create the cluster by specifying the admin node from above
* Requires `cli-pipeline>=1.5.243`
```
pipeline _cluster_kube_create --tag 1.5.0 --admin-node <admin-node-name>
```

### Retrieve the ELB DNS name
Note: the ELB DNS may take some time to propagate.
```
kubectl get svc istio-ingress -o wide

NAME                   TYPE           CLUSTER-IP      EXTERNAL-IP  
istio-ingressgateway   LoadBalancer   10.100.12.101   <dns-name>  
```

### Whitelist the DNS Name with PipelineAI (Temporary)
