![PipelineAI Logo](http://pipeline.ai/assets/img/logo/pipelineai-split-black-258x62.png) 

+ 

AWS SageMaker

**Minimum System Requirements**
* 8GB
* 4 Cores

### Install Docker Community Edition (CE)
```
https://www.docker.com
```

### Install PipelineAI CLI
```
pip install cli-pipeline --user --ignore-installed --no-cache -U 
```

### Pull PipelineAI [Sample Models](https://github.com/PipelineAI/models)
```
git clone https://github.com/PipelineAI/models
```

### Build TensorFlow Models + TensorFlow Serving
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

### Start TensorFlow Models on AWS SageMaker
Notes
* You may need to increase your quota limits for the specific instance type with AWS.
* We are using the same instance type for both CPU and GPU model versions.  This is intentional for this demo, but it is not required.
* These models take a LOOONG time to start up fully.
* You can check the CloudWatch [LOGS](#monitor-your-models) to monitor the startup process.

Examples
* `--aws-iam-arn`: arn:aws:iam::<account-number>:role/service-role/AmazonSageMaker-ExecutionRole...

[CPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-cpu)
```
pipeline predict-sage-start --model-name=mnist --model-tag=cpu --model-type=tensorflow --aws-iam-arn=<aws-iam-arn> --aws-instance-type=ml.p2.xlarge
```

[GPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-gpu)
```
pipeline predict-sage-start --model-name=mnist --model-tag=gpu --model-type=tensorflow --aws-iam-arn=<aws-iam-arn> --aws-instance-type=ml.p2.xlarge
```

### Split Traffic Between CPU Model (50%) and GPU Model (50%)
```
pipeline predict-sage-route --model-name=mnist --aws-instance-type-dict='{"cpu":"ml.p2.xlarge", "gpu":"ml.p2.xlarge"}' --model-tag-and-weight-dict='{"cpu":50, "gpu":50}'
```

### Run Load Test on Models CPU and GPU
Notes
* We are testing with sample data from the CPU version of the model.  
* This is OK since the sample data is the same for CPU and GPU.
```
pipeline predict-sage-test --model-name=mnist --test-request-path=./tensorflow/mnist-cpu/input/predict/test_request.json --test-request-concurrency=1000
```

**Expected Output**

[CPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-cpu)
```
('{"variant": "mnist-cpu-tensorflow-tfserving-cpu", "outputs":{"outputs": '
 '[0.11128007620573044, 1.4478533557849005e-05, 0.43401220440864563, '
 '0.06995827704668045, 0.0028081508353352547, 0.27867695689201355, '
 '0.017851119861006737, 0.006651509087532759, 0.07679300010204315, '
 '0.001954273320734501]}}')
 
 Request time: 440.805 milliseconds
 ```
 
[GPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-gpu)
```
('{"variant": "mnist-gpu-tensorflow-tfserving-gpu", "outputs":{"outputs": '
 '[0.11128010600805283, 1.4478532648354303e-05, 0.43401211500167847, '
 '0.06995825469493866, 0.002808149205520749, 0.2786771059036255, '
 '0.01785111241042614, 0.006651511415839195, 0.07679297775030136, '
 '0.001954274717718363]}}')

Request time: 358.047 milliseconds
```

### Monitor Your Models
![AWS SageMaker + CloudWatch Monitoring](http://pipeline.ai/assets/img/sagemaker-cloudwatch-links.png)

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

### Clean Up through AWS SageMaker UI

Delete Model

Delete Endpoint Config

Delete Endpoint
