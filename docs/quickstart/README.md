### Install Docker for Mac with Kubernetes
```
https://www.docker.com/docker-mac
```

**Minimum Version**
* Edge (Not Stable)
* 17.12.0-ce-mac45 (21669)

![Docker for Desktop Kubernetes](http://pipeline.ai/assets/img/docker-desktop-kubernetes.png)

![Docker for Desktop Kubernetes About](http://pipeline.ai/assets/img/docker-desktop-kubernetes-about.png)

**Minimum Docker System Requirements**
* 8GB
* 4 Cores

![Docker for Desktop Kubernetes Config](http://pipeline.ai/assets/img/docker-desktop-kubernetes-config.png)

```
kubectl config use-context docker-for-desktop
```

### Install PipelineCLI
```
pip install cli-pipeline==1.5.12 --user --ignore-installed --no-cache -U 
```

### Install Dashboards
```
kubectl apply ...
```

### Install Istio
```
kubectl apply ...
```

### Pull PipelineAI Sample Models
```
git clone https://github.com/PipelineAI/models
```

### Build TensorFlow Model A and B
```
pipeline predict-server-build --model-name=mnist --model-tag=a --model-type=tensorflow --model-path=./tensorflow/mnist-0.0025/model
```
```
pipeline predict-server-build --model-name=mnist --model-tag=b --model-type=tensorflow --model-path=./tensorflow/mnist-0.0050/model
```

### Deploy TensorFlow Model A and B
```
pipeline predict-kube-start --model-name=mnist --model-tag=a
```
```
pipeline predict-kube-start --model-name=mnist --model-tag=b
```

### Route Traffic to Model A (50%) and Model B (50%)
```
pipeline predict-kube-route --model-name=mnist --model-tag-and-weight-dict='{"a":50, "b":50}'
```

### Run Load Test
```
pipeline predict-kube-test --model-name=mnist --test-request-path=./tensorflow/mnist/input/predict/test_request.json --test-request-concurrency=1000
```
