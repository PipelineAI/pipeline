![PipelineAI Logo](http://pipeline.ai/assets/img/logo/pipelineai-logo.png)

## Pull PipelineAI Sample Models
```
git clone https://github.com/PipelineAI/models
```
**Change into the new `models/` directory**
```
cd models
```

# Requirements
## System
* 8GB
* 4 Cores

## Pre-requisites
* Install [Docker](https://www.docker.com/community-edition#/download)
* Install Python 2 or 3 ([Conda](https://conda.io/docs/install/quick.html) is Preferred)
* Install (Windows Only) Install [PowerShell](https://github.com/PowerShell/PowerShell/tree/master/docs/installation) 

## Install [PipelineAI CLI](../README.md#install-pipelinecli)

**NOTE: YOU MUST INSTALL THE CLI ABOVE BEFORE CONTINUING!**

## Install Local Kubernetes
* Install Docker for MacOS with Local Kubernetes (Edge Channel)
or
* Install Docker for Windows with Local Kubernetes (Edge Channel)
or
* Docker for Linux with Kubernetes (Server or Cluster)

### Configure Kubernetes CLI for Local Kubernetes Cluster
```
kubectl config use-context docker-for-desktop
```

### Build Model Prediction Servers - Versions v1a and v1b (TensorFlow-Based)
Notes:
* Install [PipelineAI CLI](../README.md#install-pipelinecli)
* You must be in the `models/` directory created from the `git clone` above.

[**Mnist v1a**](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-v1)
```
pipeline predict-server-build --model-name=mnist --model-tag=v1a --model-type=tensorflow --model-path=./tensorflow/mnist-v1/model 
```
* For GPU-based models, make sure you specify `--model-chip=gpu`

[**Mnist v1b**](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-v1)
```
pipeline predict-server-build --model-name=mnist --model-tag=v1b --model-type=tensorflow --model-path=./tensorflow/mnist-v1/model 
```
* For GPU-based models, make sure you specify `--model-chip=gpu`

### Install Istio Service Mesh CLI

**Mac**
```
curl -L https://github.com/istio/istio/releases/download/0.7.1/istio-0.7.1-osx.tar.gz | tar xz
export PATH=./istio-0.7.1/bin:$PATH
```

**Linux**
```
curl -L https://github.com/istio/istio/releases/download/0.7.1/istio-0.7.1-linux.tar.gz | tar xz
export PATH=./istio-0.7.1/bin:$PATH
```

**Windows**
```
curl -L https://github.com/istio/istio/releases/download/0.7.1/istio_0.7.1_win.zip
# Unzip and Set PATH or whatever Windows folks do.
```

**Verify Successful CLI Install**
```
which istioctl

### EXPECTED OUTPUT ###
./istio-0.7.1/bin/istioctl
```
Note:  You'll want to put `istioctl` on your permanent PATH - or copy to `/usr/local/bin`

### Deploy Istio to Cluster
```
wget https://raw.githubusercontent.com/PipelineAI/pipeline/master/docs/quickstart/kubernetes/istio-0.7.1-default-namespace.yaml

kubectl apply -f ./istio-0.7.1-default-namespace.yaml
```

**Verify Istio Components**
```
kubectl get all

### EXPECTED OUTPUT ###
NAME                   AGE
deploy/grafana         20h
deploy/istio-ca        1d
deploy/istio-ingress   1d
deploy/istio-mixer     1d
deploy/istio-pilot     1d
deploy/prometheus      20h
deploy/servicegraph    1d

NAME                          AGE
rs/grafana-d6bc494bc          20h
rs/istio-ca-75fb7dc8d5        1d
rs/istio-ingress-577d7b7fc7   1d
rs/istio-mixer-859796c6bf     1d
rs/istio-pilot-65648c94fb     1d
rs/prometheus-c79598676       20h
rs/servicegraph-64567d6467    1d

NAME                                READY     STATUS    RESTARTS   AGE
po/grafana-d6bc494bc-fwj67          1/1       Running   0          20h
po/istio-ca-75fb7dc8d5-s25m5        1/1       Running   0          1d
po/istio-ingress-577d7b7fc7-bgvfk   1/1       Running   1          1d
po/istio-mixer-859796c6bf-8qzdn     3/3       Running   0          1d
po/istio-pilot-65648c94fb-rsztg     2/2       Running   5          1d
po/prometheus-c79598676-7pcjm       1/1       Running   0          20h
po/servicegraph-64567d6467-fjv5v    1/1       Running   0          1d

NAME                TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)                                                             AGE
svc/grafana         NodePort       10.109.208.179   <none>        3000:31995/TCP                                                      20h
svc/istio-ingress   LoadBalancer   10.97.87.61      <pending>     80:31634/TCP,443:31847/TCP                                          1d
svc/istio-mixer     ClusterIP      10.110.157.188   <none>        9091/TCP,15004/TCP,9093/TCP,9094/TCP,9102/TCP,9125/UDP,42422/TCP    1d
svc/istio-pilot     ClusterIP      10.97.140.244    <none>        15003/TCP,15005/TCP,15007/TCP,15010/TCP,8080/TCP,9093/TCP,443/TCP   1d
svc/prometheus      NodePort       10.102.73.33     <none>        9090:31994/TCP                                                      20h
svc/servicegraph    NodePort       10.104.28.189    <none>        8088:31993/TCP
```

### Setup `PREDICT_HOST` and `PREDICT_PORT` from Istio
```
# Ingress Host IP
# PREDICT_HOST=$(kubectl -n istio-system get po -l istio=ingress -o jsonpath='{.items[0].status.hostIP}')
# Firewalls may block traffic routed directly to the IP Address of the ingress controller
# Typical firewall error: Failed to establish a new connection: [Errno 51] Network is unreachable',
# When running kubernetes locally use 127.0.0.1 loop back route by default 
PREDICT_HOST=127.0.0.1

# Ingress Port
PREDICT_PORT=$(kubectl get service istio-ingress -o jsonpath='{.spec.ports[?(@.name=="http")].nodePort}')
```

### Deploy Model Prediction Servers - Versions v1a and v1b (TensorFlow-based)
Notes:
* Make sure you install Istio.  See above!
* Make sure nothing is running on port 80 (ie. default Web Server on your laptop).

[**Mnist v1a**](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-v1)
```
pipeline predict-kube-start --model-name=mnist --model-tag=v1a
```
* For GPU-based models, make sure you specify `--model-chip=gpu`

[**Mnist v1b**](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-v1)
```
pipeline predict-kube-start --model-name=mnist --model-tag=v1b
```
* For GPU-based models, make sure you specify `--model-chip=gpu`

### View Running Pods
```
kubectl get pod --namespace=default

### EXPECTED OUTPUT###
NAME                          READY     STATUS    RESTARTS   AGE
predict-mnist-v1a-...-...       2/2       Running   0          5m
predict-mnist-v1b-...-...       2/2       Running   0          5m
```
* Note:  The 2nd Container (2/2) is the Envoy Sidecar.  Envoy is part of Istio.

### Split Traffic Between Model Version v1a (50%) and Model Version v1b (50%)
```
pipeline predict-kube-route --model-name=mnist --model-split-tag-and-weight-dict='{"v1a":50, "v1b":50}' --model-shadow-tag-list='[]'
```
Notes:
* If you specify a model in `--model-shadow-tag-list`, you need to explicitly specify 0% traffic split in `--model-split-tag-and-weight-dict`
* If you see `apiVersion: Invalid value: "config.istio.io/__internal": must be config.istio.io/v1alpha2`, you need to [remove the existing route rules](#clean-up) and re-create them with this command.

### Shadow Traffic from Model Version v1a (100% Live) to Model Version v1b (0% Live, Only Shadow Traffic)
```
pipeline predict-kube-route --model-name=mnist --model-split-tag-and-weight-dict='{"v1a":100, "v1b":0}' --model-shadow-tag-list='["v1b"]'
```
Notes:
* If you specify a model in `--model-shadow-tag-list`, you need to explicitly specify 0% traffic split in `--model-split-tag-and-weight-dict`
* If you see `apiVersion: Invalid value: "config.istio.io/__internal": must be config.istio.io/v1alpha2`, you need to [remove the existing route rules](#clean-up) and re-create them with this command.

### View Route Rules
```
kubectl get routerules

### EXPECTED OUTPUT ###
NAME                            KIND
predict-mnist-dashboardstream   RouteRule.v1alpha2.config.istio.io
predict-mnist-denytherest       RouteRule.v1alpha2.config.istio.io
predict-mnist-invoke            RouteRule.v1alpha2.config.istio.io
predict-mnist-metrics           RouteRule.v1alpha2.config.istio.io
predict-mnist-ping              RouteRule.v1alpha2.config.istio.io
predict-mnist-prometheus        RouteRule.v1alpha2.config.istio.io
```

**REST-Based Http Load Test**
```
pipeline predict-http-test --endpoint-url=http://$PREDICT_HOST:$PREDICT_PORT/predict/mnist/invoke --test-request-path=./tensorflow/mnist-v1/model/pipeline_test_request.json --test-request-concurrency=1000
```
Notes:
* Make sure the Host IP is accessible.  You may need to use `127.0.0.1` or `localhost`
* If you see `no healthy upstream` or `502 Bad Gateway`, just wait 1-2 mins for the model servers to startup.
* If you see a `404` error related to `No message found /predict/mnist/invoke`, the route rules above were not applied.
* See [Troubleshooting](/docs/troubleshooting) for more debugging info.

**Expected Output**
* You should see a 100% traffic to Model Version v1a, however Model Version v1b is receiving a "best effort" amount of live traffic. (See Dashboards to verify.)

[**Mnist v1a**](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-v1)
```
('{"variant": "mnist-v1a-tensorflow-tfserving-cpu", "outputs":{"classes": [3], '
 '"probabilities": [[2.353575155211729e-06, 3.998300599050708e-06, '
 '0.00912125688046217, 0.9443341493606567, 3.8211437640711665e-06, '
 '0.0003914404078386724, 5.226673920333269e-07, 2.389515998402203e-07, '
 '0.04614224657416344, 8.35775360030766e-09]]}}')
 
Request time: 36.414 milliseconds
``` 

### Load Test Model Prediction Servers - Versions v1a and v1b
* Note: you need to be in the `models/` directory created when you performed the `git clone` [above](#pull-pipelineai-sample-models).

**REST-Based Http Load Test**
```
pipeline predict-http-test --endpoint-url=http://$PREDICT_HOST:$PREDICT_PORT/predict/mnist/invoke --test-request-path=./tensorflow/mnist-v1/model/pipeline_test_request.json --test-request-concurrency=1000
```
Notes:
* Make sure the Host IP is accessible.  You may need to use `127.0.0.1` or `localhost`
* If you see `no healthy upstream` or `502 Bad Gateway`, just wait 1-2 mins for the model servers to startup.
* If you see a `404` error related to `No message found /predict/mnist/invoke`, the route rules above were not applied.
* See [Troubleshooting](/docs/troubleshooting) for more debugging info.

**Expected Output**
* You should see a 50/50 split between Model Version v1a and Version v1b

[**Mnist v1a**](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-v1)
```
('{"variant": "mnist-v1a-tensorflow-tfserving-cpu", "outputs":{"outputs": '
 '[0.11128007620573044, 1.4478533557849005e-05, 0.43401220440864563, '
 '0.06995827704668045, 0.0028081508353352547, 0.27867695689201355, '
 '0.017851119861006737, 0.006651509087532759, 0.07679300010204315, '
 '0.001954273320734501]}}')
 
Request time: 36.414 milliseconds

### FORMATTED OUTPUT ###
Digit  Confidence
=====  ==========
0      0.11128007620573044
1      0.00001447853355784
2      0.43401220440864563      <-- Prediction
3      0.06995827704668045
4      0.00280815083533525
5      0.27867695689201355 
6      0.01785111986100673
7      0.00665150908753275
8      0.07679300010204315
9      0.00195427332073450
```

[**Mnist v1b**](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-v1)
 ```
('{"variant": "mnist-v1b-tensorflow-tfserving-cpu", "outputs":{"outputs": '
 '[0.11128010600805283, 1.4478532648354303e-05, 0.43401211500167847, '
 '0.06995825469493866, 0.002808149205520749, 0.2786771059036255, '
 '0.01785111241042614, 0.006651511415839195, 0.07679297775030136, '
 '0.001954274717718363]}}')

Request time: 29.968 milliseconds

### FORMATTED OUTPUT ###
Digit  Confidence
=====  ==========
0      0.11128010600805283
1      0.00001447853264835
2      0.43401211500167847      <-- Prediction
3      0.06995825469493866
4      0.00280814920552074
5      0.27867710590362550 
6      0.01785111241042614
7      0.00665151141583919
8      0.07679297775030136
9      0.00195427471771836
 ```

### Predict with Curl
Use the REST API to POST a JSON document representing the number 2.

![MNIST 2](http://pipeline.ai/assets/img/mnist-2-100x101.png)
```
curl -X POST -H "Content-Type: application/json" \
  -d '{"image": [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.05098039656877518, 0.529411792755127, 0.3960784673690796, 0.572549045085907, 0.572549045085907, 0.847058892250061, 0.8156863451004028, 0.9960784912109375, 1.0, 1.0, 0.9960784912109375, 0.5960784554481506, 0.027450982481241226, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.7882353663444519, 0.11764706671237946, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.988235354423523, 0.7921569347381592, 0.9450981020927429, 0.545098066329956, 0.21568629145622253, 0.3450980484485626, 0.45098042488098145, 0.125490203499794, 0.125490203499794, 0.03921568766236305, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.803921639919281, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6352941393852234, 0.9921569228172302, 0.803921639919281, 0.24705883860588074, 0.3490196168422699, 0.6509804129600525, 0.32156863808631897, 0.32156863808631897, 0.1098039299249649, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.007843137718737125, 0.7529412508010864, 0.9921569228172302, 0.9725490808486938, 0.9686275124549866, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.8274510502815247, 0.29019609093666077, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.2549019753932953, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.847058892250061, 0.027450982481241226, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5921568870544434, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.7333333492279053, 0.44705885648727417, 0.23137256503105164, 0.23137256503105164, 0.4784314036369324, 0.9921569228172302, 0.9921569228172302, 0.03921568766236305, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5568627715110779, 0.9568628072738647, 0.7098039388656616, 0.08235294371843338, 0.019607843831181526, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.43137258291244507, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.15294118225574493, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1882353127002716, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6705882549285889, 0.9921569228172302, 0.9921569228172302, 0.12156863510608673, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.2392157018184662, 0.9647059440612793, 0.9921569228172302, 0.6274510025978088, 0.003921568859368563, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08235294371843338, 0.44705885648727417, 0.16470588743686676, 0.0, 0.0, 0.2549019753932953, 0.9294118285179138, 0.9921569228172302, 0.9333333969116211, 0.27450981736183167, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.4941176772117615, 0.9529412388801575, 0.0, 0.0, 0.5803921818733215, 0.9333333969116211, 0.9921569228172302, 0.9921569228172302, 0.4078431725502014, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.7411764860153198, 0.9764706492424011, 0.5529412031173706, 0.8784314393997192, 0.9921569228172302, 0.9921569228172302, 0.9490196704864502, 0.43529415130615234, 0.007843137718737125, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6235294342041016, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9764706492424011, 0.6274510025978088, 0.1882353127002716, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.18431372940540314, 0.5882353186607361, 0.729411780834198, 0.5686274766921997, 0.3529411852359772, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]}' \
  http://$PREDICT_HOST:$PREDICT_PORT/predict/mnist/invoke \
  -w "\n\n"

### Expected Output ###
{"variant": "mnist-v1a-tensorflow-tfserving-cpu", "outputs":{"outputs": [0.11128007620573044, 1.4478533557849005e-05, 0.43401220440864563, 0.06995827704668045, 0.0028081508353352547, 0.27867695689201355, 0.017851119861006737, 0.006651509087532759, 0.07679300010204315, 0.001954273320734501]}}

### Formatted Output
Digit  Confidence
=====  ==========
0      0.0022526539396494627
1      2.63791100074684e-10
2      0.4638307988643646      <-- Prediction
3      0.21909376978874207
4      3.2985670372909226e-07
5      0.29357224702835083 
6      0.00019597385835368186
7      5.230629176367074e-05
8      0.020996594801545143
9      5.426473762781825e-06
```
Notes:
* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.  This usually happens when using Docker Quick Terminal on Windows 7.
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.

### Scale Model Prediction Servers - Version v1b to 2 Replicas
Scale the Model Server
```
pipeline predict-kube-scale --model-name=mnist --model-tag=v1b --replicas=2
```

**Verify Scaling Event**
```
kubectl get pod

### EXPECTED OUTPUT###

NAME                          READY     STATUS    RESTARTS   AGE
predict-mnist-v1b-...-...       2/2       Running   0          8m
predict-mnist-v1b-...-...       2/2       Running   0          10m
predict-mnist-v1a-...-...       2/2       Running   0          10m
```

**Re-Run REST-Based Http Load Test**
```
pipeline predict-http-test --endpoint-url=http://$PREDICT_HOST:$PREDICT_PORT/predict/mnist/invoke --test-request-path=./tensorflow/mnist-v1/model/pipeline_test_request.json --test-request-concurrency=1000
```
Notes:
* Make sure the Host IP is accessible.  You may need to use `127.0.0.1` or `localhost`
* If you see `no healthy upstream` or `502 Bad Gateway`, just wait 1-2 mins for the model servers to startup.
* If you see a `404` error related to `No message found /predict/mnist/invoke`, the route rules above were not applied.
* See [Troubleshooting](/docs/troubleshooting) for more debugging info.

### PipelineAI Real-Time Dashboard

![Real-Time Throughput and Response Time](http://pipeline.ai/assets/img/hystrix-mini.png)

### Uninstall PipelineAI Models

Notes:
* Each of these will remove the `predict-mnist`
```
pipeline predict-kube-stop --model-name=mnist --model-tag=v1a
```
```
pipeline predict-kube-stop --model-name=mnist --model-tag=v1b
```

### Remove Pipeline" Traffic Routes
```
kubectl delete routerule predict-mnist-dashboardstream
kubectl delete routerule predict-mnist-denytherest
kubectl delete routerule predict-mnist-invoke
kubectl delete routerule predict-mnist-metrics
kubectl delete routerule predict-mnist-ping
kubectl delete routerule predict-mnist-prometheus
```

### Distributed TensorFlow Training 
We assume you already have the following:
* Kubernetes Cluster running anywhere! 
* Access to a shared file system like S3 or GCS

[**Census v1**](https://github.com/PipelineAI/models/tree/master/tensorflow/census-v1)

**Build Training Docker Image**
```
pipeline train-server-build --model-name=census --model-tag=v1 --model-type=tensorflow --model-path=./tensorflow/census-v1/model/
```
Notes:
* If you see the following error , you may need to increase the memory for this training job:  `TypeError: GetNext() takes 1 positional argument but 2 were given`, `tensorflow/core/framework/allocator.cc:101] Allocation of 51380224 exceeds 10% of system memory.`
* To increase the memory, use the following `--memory=8G`
* If you change the model (`pipeline_train.py`), you'll need to re-run `pipeline train-server-build ...`
* `--model-path` must be relative to the current ./models directory (cloned from https://github.com/PipelineAI/models)
* For GPU-based models, make sure you specify `--model-chip=gpu`

**Start Distributed TensorFlow Training Cluster**
```
pipeline train-kube-start --model-name=census --model-tag=v1 --input-host-path= --output-host-path=/root/samples/models/tensorflow/census-v1/model/pipeline_tfserving --runs-host-path=/root/samples/models/tensorflow/census-v1/output --master-replicas=1 --ps-replicas=1 --worker-replicas=1 --train-args="--train-files=/root/models/models/tensorflow/census-v1/input/training/adult.training.csv --eval-files=/root/models/models/tensorflow/census-v1/input/validation/adult.validation.csv --num-epochs=2 --learning-rate=0.025"
```

Notes:
* If you change the model (`pipeline_train.py`), you'll need to re-run `pipeline train-server-build ...`
* `--input-host-path` and `--output-host-path` are host paths (outside the Docker container) mapped inside the Docker container as `/opt/ml/input` (PIPELINE_INPUT_PATH) and `/opt/ml/output` (PIPELINE_OUTPUT_PATH) respectively.
* PIPELINE_INPUT_PATH and PIPELINE_OUTPUT_PATH are environment variables accesible by your model inside the Docker container. 
* PIPELINE_INPUT_PATH and PIPELINE_OUTPUT_PATH are hard-coded to `/opt/ml/input` and `/opt/ml/output`, respectively, inside the Docker conatiner .
* `--input-host-path` and `--output-host-path` should be absolute paths that are valid on the HOST Kubernetes Node
* Avoid relative paths for * `--input-host-path` and `--output-host-path` unless you're sure the same path exists on the Kubernetes Node 
* If you use `~` and `.` and other relative path specifiers, note that `--input-host-path` and `--output-host-path` will be expanded to the absolute path of the filesystem where this command is run - this is likely not the same filesystem path as the Kubernetes Node!
* `--input-host-path` and `--output-host-path` are available outside of the Docker container as Docker volumes
* `--train-args` is used to specify `--train-files` and `--eval-files` and other arguments used inside your model 
* Inside the model, you should use PIPELINE_INPUT_PATH (`/opt/ml/input`) as the base path for the subpaths defined in `--train-files` and `--eval-files`
* We automatically mount `https://github.com/PipelineAI/models` as `/root/samples/models` for your convenience
* You can use our samples by setting `--input-host-path` to anything (ignore it, basically) and using an absolute path for `--train-files`, `--eval-files`, and other args referenced by your model
* You can specify S3 buckets/paths in your `--train-args`, but the host Kubernetes Node needs to have the proper EC2 IAM Instance Profile needed to access the S3 bucket/path
* Otherwise, you can specify ACCESS_KEY_ID and SECRET_ACCESS_KEY in your model code (not recommended_
* `--train-files` and `--eval-files` can be relative to PIPELINE_INPUT_PATH (`/opt/ml/input`), but remember that PIPELINE_INPUT_PATH is mapped to PIPELINE_HOST_INPUT_PATH which must exist on the Kubernetes Node where this container is placed (anywhere)
* `--train-files` and `--eval-files` are used by the model, itself
* You can pass any parameter into `--train-args` to be used by the model (`pipeline_train.py`)
* `--train-args` is a single argument passed into the `pipeline_train.py`
* Models, logs, and event are written to `--output-host-path` (or a subdirectory within it).  These paths are available outside of the Docker container.
* To prevent overwriting the output of a previous run, you should either 1) change the `--output-host-path` between calls or 2) create a new unique subfolder within `--output-host-path` in your `pipeline_train.py` (ie. timestamp).
* Make sure you use a consistent `--output-host-path` across nodes.  If you use timestamp, for example, the nodes in your distributed training cluster will not write to the same path.  You will see weird ABORT errors from TensorFlow.
* On Windows, be sure to use the forward slash `\` for `--input-host-path` and `--output-host-path` (not the args inside of `--train-args`).
* If you see `port is already allocated` or `already in use by container`, you already have a container running.  List and remove any conflicting containers.  For example, `docker ps` and/or `docker rm -f train-census-v1-tensorflow-tfserving-cpu`.
* For GPU-based models, make sure you specify `--start-cmd=nvidia-docker` - and make sure you have `nvidia-docker` installed!
* For GPU-based models, make sure you specify `--model-chip=gpu`
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.

(_We are working on making this more intuitive._)

### PipelineAI Quick Start (CPU, GPU, and TPU)
Train and Deploy your ML and AI Models in the Following Environments:
* [Hosted Community Edition](/docs/quickstart/community)
* [Docker](/docs/quickstart/docker)
* [Kubernetes](/docs/quickstart/kubernetes)
* [AWS SageMaker](/docs/quickstart/sagemaker)
