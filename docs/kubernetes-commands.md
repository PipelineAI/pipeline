### Bash into Live Docker Container
_This assumes 1 Docker Container per Pod_
```
kubectl get pod
```
```
kubectl exec -it <pod-name> bash
```

_If more than 1 Docker Container per Pod_
```
kubectl exec -it <pod-name> -c <container-name> bash
```

### Scale Deployment
```
kubectl get deploy
```
```
kubectl scale --replicas=4 deploy <deploy-name>
```

### Upgrade Deployment (aka. Rollout)
```
kubectl set image deploy jupyterhub jupyterhub=fluxcapacitor/jupyterhub:<v2>
```
```
kubectl rollout status deploy jupyterhub
```

### Rollback Deployment
```
kubectl rollout history deploy jupyterhub
```
```
kubectl rollout undo deploy jupyterhub --to-revision=1
```
```
kubectl rollout status deploy jupyterhub
```

## Even More Kubernetes Commands!!
[Kubernetes Cheat Sheet](https://kubernetes.io/docs/user-guide/kubectl-cheatsheet/)

[Deployments](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/)

[Canary Releases](https://kubernetes.io/docs/concepts/cluster-administration/manage-deployment/#canary-deployments)
