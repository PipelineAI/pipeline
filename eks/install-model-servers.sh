#!/bin/bash
helm install seldon-core-operator --name seldon-core-operator --namespace kubeflow --set istio.enabled=true --set ambassador.enabled=false --repo https://storage.googleapis.com/seldon-charts

kubectl create namespace deployment
kubectl create -f deployment-gateway.yaml
kubectl label namespace deployment istio-injection=enabled
