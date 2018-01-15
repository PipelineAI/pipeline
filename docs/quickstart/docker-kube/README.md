![PipelineAI Logo](http://pipeline.ai/assets/img/logo/pipelineai-split-black-258x62.png)

### Install Docker for Mac with Kubernetes 
```
https://www.docker.com/docker-mac
```

**Enable Kubernetes**

![Docker for Desktop Kubernetes](http://pipeline.ai/assets/img/docker-desktop-kubernetes.png?classes=border,shadow)

**Minimum Version**
* Edge (Not Stable)
* 17.12.0-ce
* Kubernetes v1.8.2

![Docker for Desktop Kubernetes About](http://pipeline.ai/assets/img/docker-desktop-kubernetes-about.png?classes=border,shadow)

**Minimum Docker System Requirements**
* 8GB
* 4 Cores

![Docker for Desktop Kubernetes Config](http://pipeline.ai/assets/img/docker-desktop-kubernetes-config.png?classes=border,shadow)

**Configure Kubernetes CLI for Local Kubernetes Cluster**
```
kubectl config use-context docker-for-desktop
```

### Install PipelineAI CLI
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.
```
pip install cli-pipeline==1.5.18 --ignore-installed --no-cache -U 
```

### Install Istio
```
curl -L https://git.io/getLatestIstio | sh -
```

**Deploy Istio into Local Kubernetes Cluster**
```
kubectl apply -f https://raw.githubusercontent.com/istio/istio/0.4.0/install/kubernetes/istio.yaml
```

### Pull PipelineAI Sample Models
```
git clone https://github.com/PipelineAI/models
```

### Build Models A and B (TensorFlow-based)

[CPU](https://github.com/PipelineAI/models/tree/f559987d7c889b7a2e82528cc72d003ef3a34573/tensorflow/mnist-0.025) (Learning Rate = 0.025)
```
pipeline predict-server-build --model-name=mnist --model-tag=a --model-type=tensorflow --model-path=./tensorflow/mnist-0.025/model
```

[CPU](https://github.com/PipelineAI/models/tree/f559987d7c889b7a2e82528cc72d003ef3a34573/tensorflow/mnist-0.050) (Learning Rate = 0.050)
```
pipeline predict-server-build --model-name=mnist --model-tag=b --model-type=tensorflow --model-path=./tensorflowcd /mnist-0.050/model
```

### Push Models A and B (TensorFlow-based)

[CPU](https://github.com/PipelineAI/models/tree/f559987d7c889b7a2e82528cc72d003ef3a34573/tensorflow/mnist-0.025) (Learning Rate = 0.025)
```
pipeline predict-server-push --model-name=mnist --model-tag=a
```

[CPU](https://github.com/PipelineAI/models/tree/f559987d7c889b7a2e82528cc72d003ef3a34573/tensorflow/mnist-0.050) (Learning Rate = 0.050)
```
pipeline predict-server-build --model-name=mnist --model-tag=b
```

### Deploy Models A and B (TensorFlow-based)

[CPU](https://github.com/PipelineAI/models/tree/f559987d7c889b7a2e82528cc72d003ef3a34573/tensorflow/mnist-0.025) (Learning Rate = 0.025)
```
pipeline predict-kube-start --model-name=mnist --model-tag=a
```

[CPU](https://github.com/PipelineAI/models/tree/f559987d7c889b7a2e82528cc72d003ef3a34573/tensorflow/mnist-0.050) (Learning Rate = 0.050)
```
pipeline predict-kube-start --model-name=mnist --model-tag=b
```

### View Running Pods
```
kubectl get pod

### EXPECTED OUTPUT###
NAME                                 READY     STATUS    RESTARTS   AGE
predict-mnist-a-c7b7cfc6-fcpb5       2/2       Running   0          5m
predict-mnist-b-5b9795f5-sz84h       2/2       Running   0          5m
```

### Split Traffic Between Model A (50%) and Model B (50%)
```
pipeline predict-kube-route --model-name=mnist --model-tag-and-weight-dict='{"a":50, "b":50}'
```

### View Route Rules
```
kubectl get routerules

### EXPECTED OUTPUT ###
NAME                            KIND
predict-mnist-dashboardstream   RouteRule.v1alpha2.config.istio.io
predict-mnist-denytherest       RouteRule.v1alpha2.config.istio.io
predict-mnist-invocations       RouteRule.v1alpha2.config.istio.io
predict-mnist-metrics           RouteRule.v1alpha2.config.istio.io
predict-mnist-ping              RouteRule.v1alpha2.config.istio.io
predict-mnist-prometheus        RouteRule.v1alpha2.config.istio.io
```

### Run Load Test on Models A and B
The input data is the same across both models, so we just use the data from [mnist-0.025](https://github.com/PipelineAI/models/blob/6c9a2a0c6f132e07fad54783ae71180a01eb146a/tensorflow/mnist-0.025/input/predict/test_request.json).
```
pipeline predict-kube-test --model-name=mnist --test-request-path=./tensorflow/mnist-0.025/input/predict/test_request.json --test-request-concurrency=1000
```

**Expected Output**
[CPU](https://github.com/PipelineAI/models/tree/f559987d7c889b7a2e82528cc72d003ef3a34573/tensorflow/mnist-0.025) (Learning Rate = 0.025)
```
('{"variant": "mnist-a-tensorflow-tfserving-cpu", "outputs":{"outputs": '
 '[0.11128007620573044, 1.4478533557849005e-05, 0.43401220440864563, '
 '0.06995827704668045, 0.0028081508353352547, 0.27867695689201355, '
 '0.017851119861006737, 0.006651509087532759, 0.07679300010204315, '
 '0.001954273320734501]}}')
 
 Request time: 36.414 milliseconds
 ```

[CPU](https://github.com/PipelineAI/models/tree/f559987d7c889b7a2e82528cc72d003ef3a34573/tensorflow/mnist-0.050) (Learning Rate = 0.050)
 ```
('{"variant": "mnist-b-tensorflow-tfserving-cpu", "outputs":{"outputs": '
 '[0.11128010600805283, 1.4478532648354303e-05, 0.43401211500167847, '
 '0.06995825469493866, 0.002808149205520749, 0.2786771059036255, '
 '0.01785111241042614, 0.006651511415839195, 0.07679297775030136, '
 '0.001954274717718363]}}')

Request time: 29.968 milliseconds
 ```

### Scale Model A to 2 Replicas
```
pipeline predict-kube-scale --model-name=mnist --model-tag=a --replicas=2
```
```
kubectl get pod

### EXPECTED OUTPUT###

NAME                                 READY     STATUS    RESTARTS   AGE
predict-mnist-a-c7b7cfc6-8knqh       2/2       Running   0          8m
predict-mnist-a-c7b7cfc6-fcpb5       2/2       Running   0          10m
predict-mnist-b-5b9795f5-sz84h       2/2       Running   0          10m
```

### Re-Run Load Test on Models A and B
_You may need to wait 30 secs for the 2nd replica to start up completely._
```
pipeline predict-kube-test --model-name=mnist --test-request-path=./tensorflow/mnist-0.025/input/predict/test_request.json --test-request-concurrency=1000
```

### Clean Up

### Uninstall PipelineAI Models
```
pipeline predict-kube-stop --model-name=mnist --model-tag=a
```
```
pipeline predict-kube-stop --model-name=mnist --model-tag=b
```

### Uninstall PipelineAI Traffic Routes
```
kubectl delete routerule predict-mnist-dashboardstream
kubectl delete routerule predict-mnist-denytherest
kubectl delete routerule predict-mnist-invocations
kubectl delete routerule predict-mnist-metrics
kubectl delete routerule predict-mnist-ping
kubectl delete routerule predict-mnist-prometheus
```

### Uninstall Istio
```
kubectl delete -f https://raw.githubusercontent.com/istio/istio/0.4.0/install/kubernetes/istio.yaml
```
