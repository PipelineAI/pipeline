https://www.weave.works/documentation/scope-latest-installing/#k8s

Run the following locally or on a secure machine with `port 4040` open

`kube-system` namespace
```
kubectl port-forward -n kube-system "$(kubectl get -n kube-system pod --selector=weave-scope-component=app -o jsonpath='{.items..metadata.name}')" 4040
```

`default` namespace
```
kubectl port-forward -n default "$(kubectl get -n default pod --selector=weave-scope-component=app -o jsonpath='{.items..metadata.name}')" 4040
```

Navigate to the following:
```
http://<localhost-or-secure-machine>:4040
```

Snapshots from here:
```
curl --silent --location --remote-name https://cloud.weave.works/k8s/scope.yaml?v=1.3.0
curl --silent --location --remote-name https://cloud.weave.works/k8s/scope.yaml?v=1.3.0&k8s-service-type=LoadBalancer
curl --silent --location --remote-name https://cloud.weave.works/k8s/scope.yaml?v=1.3.0&k8s-service-type=NodePort
```

