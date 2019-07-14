#Create Kiali Secret
kubectl apply -f conf/kiali-secret.yaml

#Install Istio
curl -L https://git.io/getLatestIstio | ISTIO_VERSION=1.2.2 sh -
cd istio-1.2.2
export PATH=$PWD/bin:$PATH

#helm del --purge istio
#helm del --purge istio-init
helm install install/kubernetes/helm/istio-init --name istio-init --namespace istio-system
helm install install/kubernetes/helm/istio --name istio --namespace istio-system --set grafana.enabled=true --set kiali.enabled=true --set prometheus.enabled=true --set tracing.enabled=true

cd ..

#Deploy
kubectl label namespace kubeflow istio-injection=enabled
#kubectl apply -f samples/bookinfo/platform/kube/bookinfo.yaml

#Gateway
#kubectl apply -f samples/bookinfo/networking/bookinfo-gateway.yaml
#export INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].nodePort}')
#find INGRESS_HOST IP by plugging in INSTANCE_NAME (dev) 
#gcloud compute instances describe [INSTANCE_NAME] --format='get(networkInterfaces[0].accessConfigs[0].natIP)'
#export INGRESS_HOST=104.197.47.49

#gcloud compute firewall-rules create allow-gateway-http --allow tcp:$INGRESS_PORT
#export GATEWAY_URL=$INGRESS_HOST:$INGRESS_PORT

#Destination Rules
#round robin between v1, v2, v3
#kubectl apply -f samples/bookinfo/networking/destination-rule-all.yaml

#Request Routing
#everyone sees v1
#kubectl apply -f samples/bookinfo/networking/virtual-service-all-v1.yaml
#login with "jason" to see v2
#kubectl apply -f samples/bookinfo/networking/virtual-service-reviews-test-v2.yaml

#Logging FluentD http://104.197.47.49:4567
#kubectl apply -f conf/logging-stack.yaml
#kubectl apply -f samples/bookinfo/telemetry/fluentd-istio.yaml
#kubectl -n logging port-forward --address 0.0.0.0 $(kubectl -n logging get pod -l app=kibana -o jsonpath='{.items[0].metadata.name}') 4567:5601
	
#Prometheus http://<ip-address>:16802
kubectl apply -f conf/prometheus-gateway.yaml

#Grafana http://<ip-address>:18829
kubectl apply -f conf/grafana-gateway.yaml

#Kiali http://<ip-address>:15623
kubectl apply -f conf/kiali-gateway.yaml






