# Run Spark on Kubernetes and PipelineAI

## Pre-requisites
* Running Kubernetes Cluster (1.10+)
* Docker installed (Docker for Mac/Windows is OK)

## Download Spark
```
wget http://apache.claz.org/spark/spark-2.4.0/spark-2.4.0-bin-hadoop2.7.tgz

tar -gzvf spark-2.4.0-bin-hadoop2.7.tgz
```

# (Optional) Build Customer Docker Images
```
cd spark-2.4.0-bin-hadoop2.7/

#./bin/docker-image-tool.sh -r pipelineai -t my-tag build
#./bin/docker-image-tool.sh -r pipelineai -t my-tag push
```

## Create the following RBAC (do not use in production):
```
---
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: Role
metadata:
  name: api-role
  namespace: default
rules:
# Just an example, feel free to change it
- apiGroups: [""]
  resources: ["pods"]
  verbs: ["create", "get", "watch", "list"]

---
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: RoleBinding
metadata:
  name: api-role-binding
  namespace: default
subjects:
  - kind: ServiceAccount
    name: default
roleRef:
  kind: Role
  name: api-role
  apiGroup: rbac.authorization.k8s.io
```

## Run the Spark Job 
```
cd spark-2.4.0-bin-hadoop2.7/

./bin/spark-submit \
     --master k8s://https://172.31.33.165:6443 \
     --deploy-mode cluster \
     --name spark-pi \
     --class org.apache.spark.examples.SparkPi \
     --conf spark.executor.instances=1 \
     --conf spark.kubernetes.container.image=pipelineai/spark:2.4.0 \
    local:///opt/spark/examples/jars/spark-examples_2.11-2.4.0.jar
```
Notes:  
* The local:/// directory is *inside* the docker image.
* Instead, we should use s3 - or build a new docker image with a new path for your jars (ie. `/opt/spark/jars/.../`)

## Monitor the Kubernetes resources
```
kubectl get pod

### EXPECTED OUTPUT ###
...
spark-pi-driver
spark-pi-exec
```

# References
* https://weidongzhou.wordpress.com/2018/04/29/running-spark-on-kubernetes/
* https://spark.apache.org/docs/latest/running-on-kubernetes.html#docker-images
