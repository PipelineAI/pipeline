#Create Kiali Secret
kubectl apply -f conf/kiali-secret.yaml

#Install Istio
helm install install/kubernetes/helm/istio-init --name istio-init --namespace istio-
helm install install/kubernetes/helm/istio --name istio --namespace istio-system --set grafana.enabled=true --set kiali.enabled=true --set prometheus.enabled=true --set tracing.enabled=true

#Prometheus http://104.197.47.49:16802
kubectl apply -f conf/prometheus-gateway.yaml

#Grafana http://104.197.47.49:18829
kubectl apply -f conf/grafana-gateway.yaml

#Kiali http://104.197.47.49:15623
kubectl apply -f conf/kiali-gateway.yaml


