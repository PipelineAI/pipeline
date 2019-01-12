### Choose a node as the "admin" node.
```
kubectl get node

NAME                                          STATUS    ROLES     AGE       VERSION
<node1>                                       Ready     <none>    24m       v1.10.3
<node2>                                       Ready     <none>    24m       v1.10.3
```

### Create the cluster by specifying the admin node from above
```
pipeline _cluster_kube_create --tag 1.5.0 --admin-node <admin-node-name>
```
