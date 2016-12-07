#!/bin/sh                                                                               
                                                                                         
kubectl scale --replicas=1 rc prediction-pmml
kubectl scale --replicas=1 rc prediction-codegen
