#Install
curl -L https://git.io/getLatestIstio | ISTIO_VERSION=1.2.2 sh -
cd istio-1.2.2
export PATH=$PWD/bin:$PATH

helm install install/kubernetes/helm/istio-init --name istio-init --namespace istio-system
helm install install/kubernetes/helm/istio --name istio --namespace istio-system --set grafana.enabled=true

#Deploy
kubectl label namespace kubeflow istio-injection=enabled
kubectl apply -f samples/bookinfo/platform/kube/bookinfo.yaml

#Gateway
kubectl apply -f samples/bookinfo/networking/bookinfo-gateway.yaml
export INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="http2")].nodePort}')
export SECURE_INGRESS_PORT=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.spec.ports[?(@.name=="https")].nodePort}')
export INGRESS_HOST=104.197.47.49
gcloud compute firewall-rules create allow-gateway-http --allow tcp:$INGRESS_PORT
gcloud compute firewall-rules create allow-gateway-https --allow tcp:$SECURE_INGRESS_PORT
export GATEWAY_URL=$INGRESS_HOST:$INGRESS_PORT

#Destination Rules
kubectl apply -f samples/bookinfo/networking/destination-rule-all.yaml
