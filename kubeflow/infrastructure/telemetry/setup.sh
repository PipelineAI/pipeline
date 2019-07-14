#Create Kiali Secret
kubectl apply -f conf/kiali-secret.yaml

#Install Istio
curl -L https://git.io/getLatestIstio | ISTIO_VERSION=1.2.2 sh -
cd istio-1.2.2
export PATH=$PWD/bin:$PATH

#helm del --purge istio
#helm del --purge istio-init
kubectl apply -f install/kubernetes/helm/helm-service-account.yaml
helm init --service-account tiller
helm install install/kubernetes/helm/istio-init --name istio-init --namespace istio-system
helm install install/kubernetes/helm/istio --name istio --namespace istio-system --set grafana.enabled=true --set kiali.enabled=true --set prometheus.enabled=true --set tracing.enabled=true

cd ..
	
#Prometheus
kubectl apply -f conf/prometheus-gateway.yaml

#Grafana
kubectl apply -f conf/grafana-gateway.yaml

#Kiali
kubectl apply -f conf/kiali-gateway.yaml






