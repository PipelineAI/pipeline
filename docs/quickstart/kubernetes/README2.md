# TODO:  Fix these links for prometheus and grafana
#
### Install Dashboards
**Prometheus**
```
kubectl apply -f https://raw.githubusercontent.com/istio/istio/0.7.1/install/kubernetes/addons/prometheus.yaml
```
```
kubectl -n istio-system port-forward $(kubectl -n istio-system get pod -l app=prometheus -o jsonpath='{.items[0].metadata.name}') 9090:9090 &   
```
Verify `prometheus-...` pod is running
```
kubectl get pod --namespace=istio-system
```
```
http://localhost:9090/graph 
```

**Grafana**
```
kubectl apply -f https://raw.githubusercontent.com/istio/istio/0.7.1/install/kubernetes/addons/grafana.yaml
```

Verify `grafana-...` pod is running
```
kubectl get pod --namespace=istio-system
```

Open localhost proxy
```
kubectl -n istio-system port-forward $(kubectl -n istio-system get pod -l app=grafana -o jsonpath='{.items[0].metadata.name}') 3000:3000 &
```

View dashboard
```
http://localhost:3000/dashboard/db/istio-dashboard
```

**Service Graph**
```
kubectl apply -f https://raw.githubusercontent.com/istio/istio/0.7.1/install/kubernetes/addons/servicegraph.yaml
```

Verify `grafana-...` pod is running
```
kubectl get pod --namespace=istio-system
```

Open localhost proxy
```
kubectl -n istio-system port-forward $(kubectl -n istio-system get pod -l app=servicegraph -o jsonpath='{.items[0].metadata.name}') 8088:8088 &   
```

View dashboard (60s window)
```
http://localhost:8088/dotviz?filter_empty=true&time_horizon=60s
```
