#!/bin/sh                                                                               

echo "Current Cluster:"
kubectl config current-context
         
echo "\nServices:"
kubectl get svc                                                                      

echo "\nReplication Controller:"
kubectl get rc

echo "\nPods:"
kubectl get pod
