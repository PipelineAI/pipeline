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
helm install install/kubernetes/helm/istio --name istio --namespace istio-system --set gateways.istio-ingressgateway.type=NodePort --set grafana.enabled=true --set kiali.enabled=true --set prometheus.enabled=true --set tracing.enabled=true --set "kiali.dashboard.grafanaURL=http://grafana:3000"

#Bookinfo
kubectl label namespace kubeflow istio-injection=enabled
kubectl apply -f samples/bookinfo/platform/kube/bookinfo.yaml
kubectl apply -f samples/bookinfo/networking/bookinfo-gateway.yaml

export INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].nodePort}')

#find INGRESS_HOST IP by plugging in INSTANCE_NAME (dev) 
#gcloud compute instances describe [INSTANCE_NAME] --format='get(networkInterfaces[0].accessConfigs[0].natIP)'
export INGRESS_HOST=$(dig +short myip.opendns.com @resolver1.opendns.com)
#104.197.47.49

#gcloud compute firewall-rules create allow-gateway-http --allow tcp:$INGRESS_PORT
export GATEWAY_URL=$INGRESS_HOST:$INGRESS_PORT

#Destination Rules
#round robin between v1, v2, v3
kubectl apply -f samples/bookinfo/networking/destination-rule-all.yaml

#Request Routing
kubectl apply -f samples/bookinfo/networking/virtual-service-all-v1.yaml
kubectl apply -f samples/bookinfo/networking/virtual-service-reviews-test-v2.yaml

cd ..
#Prometheus
kubectl apply -f conf/prometheus-gateway.yaml

#Grafana
kubectl apply -f conf/grafana-gateway.yaml

#Kiali
kubectl apply -f conf/kiali-gateway.yaml


echo "Navigate to ${GATEWAY_URL}"



