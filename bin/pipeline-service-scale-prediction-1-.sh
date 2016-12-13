#!/bin/sh                                                                               
                                                                                         
kubectl scale --replicas=1 rc prediction-pmml
kubectl scale --replicas=1 rc prediction-codegen
kubectl scale --replicas=1 rc prediction-keyvalue
kubectl scale --replicas=1 rc prediction-tensorflow
