### Install Docker for Mac with Kubernetes
Minimum Requirements
* 8GB
* 4 Cores

### Install PipelineCLI
```
pip install cli-pipeline==1.5.12 --user --ignore-installed --no-cache -U 
```

### Install Dashboards
```
...
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

...
