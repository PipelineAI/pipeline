## Kubernetes WeaveScope Dashboard
### Create Kubernetes WeaveScope Dashboard
```
kubectl create -f kubernetes/weavescope.yaml
```

### Find External Service IP of WeaveScope
```
kubectl get svc -w
```

### Navigate to the Kubernetes WeaveScope UI
```
http://<external-service-ip>:4040
```
