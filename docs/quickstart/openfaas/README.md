![PipelineAI Logo](http://pipeline.ai/assets/img/logo/pipelineai-split-black-258x62.png) 

## Pre-requisites
### Install Tools
* [Docker](https://www.docker.com/community-edition#/download)
* Python 2 or 3 ([Conda](https://conda.io/docs/install/quick.html) is Preferred)
* (Windows Only) [PowerShell](https://github.com/PowerShell/PowerShell/tree/master/docs/installation) 
* [Kubernetes Cluster](/docs/kube-setup)

### [OpenFaaS](https://github.com/openfaas/faas)

**Install [OpenFaaS-netes](https://github.com/openfaas/faas-netes) on Kubernetes**
```
git clone https://github.com/openfaas/faas-netes
```
```
cd faas-netes
```
```
kubectl apply -f ./namespaces.yml
```
```
kubectl apply -f ./yaml
```
or try [THIS](https://github.com/openfaas/faas/blob/master/guide/deployment_k8s.md#kubernetes) link.

### View OpenFaaS UIs
For simplicity the default configuration uses NodePorts (rather than an IngressController.)

| Service           | TCP port |
--------------------|----------|
| API Gateway / UI  | 31112    |
| Prometheus        | 31119    |

**Create `localhost` Proxies to the OpenFaaS API Gateway**
(The `_` at the beginning of `_service_connect` is not a typo.  Just work with us, for now!)
```
pipeline _service_connect --service-name=gateway
```

### Install PipelineAI CLI
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.
```
pip install cli-pipeline==1.5.26 --ignore-installed --no-cache -U 
```

### Pull PipelineAI [Sample Models](https://github.com/PipelineAI/models)
```
git clone https://github.com/PipelineAI/models
```

### Build CPU and GPU Models (TensorFlow-based with TensorFlow Serving)
[CPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-cpu)
```
pipeline predict-server-build --model-name=mnist --model-tag=cpu --model-type=tensorflow --model-path=./tensorflow/mnist-cpu/model --model-chip=cpu
```

[GPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-gpu)
```
pipeline predict-server-build --model-name=mnist --model-tag=gpu --model-type=tensorflow --model-path=./tensorflow/mnist-gpu/model --model-chip=gpu
```

### Push TensorFlow Models to Docker Repo
Notes:  
* This can be an AWS ECR Docker Repo - or any public Docker Repo (ie. DockerHub).

Defaults
* `--image-registry-url`:  docker.io
* `--image-registry-repo`:  pipelineai

[CPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-cpu)
```
pipeline predict-server-push --model-name=mnist --model-tag=cpu --image-registry-url=<your-registry> --image-registry-repo=<your-repo>
```

[GPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-gpu)
```
pipeline predict-server-push --model-name=mnist --model-tag=gpu --image-registry-url=<your-registry> --image-registry-repo=<your-repo>
```

### Start TensorFlow Models on OpenFaaS

[CPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-cpu)
```
pipeline predict-faas-start --model-name=mnist --model-tag=cpu
```

[GPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-gpu)
```
pipeline predict-faas-start --model-name=mnist --model-tag=gpu
```

### Run Load Test on Models CPU and GPU (100 Predictions)
```
pipeline predict-http-test --model-endpoint-url=<openfaas-gateway-url> --test-request-path=./tensorflow/mnist-cpu/input/predict/test_request.json --test-request-concurrency=100
```
Notes:
* We are testing with sample data from the CPU version of the model.  
* This is OK since the sample data is the same for CPU and GPU.
* If the endpoint status (above) is not `InService`, this call won't work.  Please be patient.

**Expected Output**

[CPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-cpu)
```
('{"variant": "mnist-cpu-tensorflow-tfserving-cpu", "outputs":{"outputs": '
 '[0.11128007620573044, 1.4478533557849005e-05, 0.43401220440864563, '
 '0.06995827704668045, 0.0028081508353352547, 0.27867695689201355, '
 '0.017851119861006737, 0.006651509087532759, 0.07679300010204315, '
 '0.001954273320734501]}}')
 
 Request time: 240.805 milliseconds
 ```
 
[GPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-gpu)
```
('{"variant": "mnist-gpu-tensorflow-tfserving-gpu", "outputs":{"outputs": '
 '[0.11128010600805283, 1.4478532648354303e-05, 0.43401211500167847, '
 '0.06995825469493866, 0.002808149205520749, 0.2786771059036255, '
 '0.01785111241042614, 0.006651511415839195, 0.07679297775030136, '
 '0.001954274717718363]}}')

Request time: 158.047 milliseconds
```

### Monitor Your Models

[CPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-cpu)
```
2018-01-09 21:38:04.021915: I tensorflow_serving/model_servers/main.cc:147] Building single TensorFlow model file config: model_name: mnist model_base_path: /root/ml/model/pipeline_tfserving
...
2018-01-09 21:38:04.128440: I tensorflow_serving/core/loader_harness.cc:74] Loading servable version {name: mnist version: 1510612528}
2018-01-09 21:38:04.134781: I external/org_tensorflow/tensorflow/core/platform/cpu_feature_guard.cc:137] Your CPU supports instructions that this TensorFlow binary was not compiled to use: AVX2 FMA
...
2018-01-09 21:38:04.206946: I tensorflow_serving/core/loader_harness.cc:86] Successfully loaded servable version {name: mnist version: 1510612528}
E0109 21:38:04.210768165 53 ev_epoll1_linux.c:1051] grpc epoll fd: 5
2018-01-09 21:38:04.213992: I tensorflow_serving/model_servers/main.cc:288] Running ModelServer at 0.0.0.0:9000 ...
```

[GPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-gpu)
```
2018-01-09 21:40:47.842724: I tensorflow_serving/model_servers/main.cc:148] Building single TensorFlow model file config: model_name: mnist model_base_path: /root/ml/model/pipeline_tfserving
...
2018-01-09 21:40:47.949612: I external/org_tensorflow/tensorflow/cc/saved_model/loader.cc:240] Loading SavedModel with tags: { serve }; from: /root/ml/model/pipeline_tfserving/1510612528
2018-01-09 21:40:48.217917: I external/org_tensorflow/tensorflow/stream_executor/cuda/cuda_gpu_executor.cc:898] successful NUMA node read from SysFS had negative value (-1), but there must be at least one NUMA node, so returning NUMA node zero
2018-01-09 21:40:48.218607: I external/org_tensorflow/tensorflow/core/common_runtime/gpu/gpu_device.cc:1202] Found device 0 with properties: 
name: Tesla K80 major: 3 minor: 7 memoryClockRate(GHz): 0.8755
pciBusID: 0000:00:1e.0
totalMemory: 11.17GiB freeMemory: 11.10GiB
2018-01-09 21:40:48.218644: I external/org_tensorflow/tensorflow/core/common_runtime/gpu/gpu_device.cc:1296] Adding visible gpu device 0
2018-01-09 21:40:50.336216: I external/org_tensorflow/tensorflow/core/common_runtime/gpu/gpu_device.cc:983] Creating TensorFlow device (/job:localhost/replica:0/task:0/device:GPU:0 with 10765 MB memory) -> physical GPU (device: 0, name: Tesla K80, pci bus id: 0000:00:1e.0, compute capability: 3.7)
...
2018-01-09 21:40:50.634043: I tensorflow_serving/core/loader_harness.cc:86] Successfully loaded servable version {name: mnist version: 1510612528}
2018-01-09 21:40:50.640806: I tensorflow_serving/model_servers/main.cc:289] Running ModelServer at 0.0.0.0:9000 ...
```

### Stop Model through OpenFaaS UI
* Delete Function
