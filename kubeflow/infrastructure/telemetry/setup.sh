# TODO

#FluentD
kubectl apply -f logging-stack.yaml
kubectl apply -f samples/bookinfo/telemetry/fluentd-istio.yaml

#Prometheus (add to virtualservice)
kubectl -n istio-system port-forward --address 0.0.0.0 $(kubectl -n istio-system get pod -l app=prometheus -o jsonpath='{.items[0].metadata.name}') 9090:9090

#Grefana (add to virtualservice)
kubectl -n istio-system port-forward --address 0.0.0.0 $(kubectl -n istio-system get pod -l app=grafana -o jsonpath='{.items[0].metadata.name}') 3000:3000
