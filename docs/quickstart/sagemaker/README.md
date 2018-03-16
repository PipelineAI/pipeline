![PipelineAI Logo](http://pipeline.ai/assets/img/logo/pipelineai-split-black-258x62.png) 

### AWS SageMaker Overview
![PipelineAI + AWS SageMaker Dashboard](http://pipeline.ai/assets/img/sagemaker-train-tune-deploy-with-logos.png)

![PipelineAI + AWS SageMaker Overview](http://pipeline.ai/assets/img/sagemaker-pipelineai-overview.png)

<a href="http://www.youtube.com/watch?feature=player_embedded&v=P5BWsyDwjYs" target="_blank"><img src="http://img.youtube.com/vi/P5BWsyDwjYs/0.jpg" alt="PipelineAI + SageMaker" border="10" /></a>

Click [HERE](https://docs.aws.amazon.com/sagemaker/latest/dg/how-it-works-hosting.html) for more details on AWS SageMaker.

### Install PipelineAI CLI
* Click [**HERE**](../README.md#install-pipelinecli) to install the PipelineAI CLI

### Pull PipelineAI [Sample Models](https://github.com/PipelineAI/models)
```
git clone https://github.com/PipelineAI/models
```
**Change into the new `models/` directory**
```
cd models
```

### Build CPU and GPU Models (TensorFlow-based with TensorFlow Serving)
[CPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-cpu)
```
pipeline predict-server-build --model-name=mnist --model-tag=cpu --model-type=tensorflow --model-path=./tensorflow/mnist-cpu/model --model-chip=cpu 
```
* For GPU-based models, make sure you specify `--model-chip=gpu`

[GPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-gpu)
```
pipeline predict-server-build --model-name=mnist --model-tag=gpu --model-type=tensorflow --model-path=./tensorflow/mnist-gpu/model --model-chip=gpu 
```
* For GPU-based models, make sure you specify `--model-chip=gpu`

### Register TensorFlow Model Server with Docker Repo
Notes:  
* This can be an AWS ECR Docker Repo - or any public Docker Repo (ie. DockerHub).

Defaults
* `--image-registry-url`:  docker.io
* `--image-registry-repo`:  pipelineai

[CPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-cpu)
```
pipeline predict-server-register --model-name=mnist --model-tag=cpu 
```
* For GPU-based models, make sure you specify `--model-chip=gpu`

[GPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-gpu)
```
pipeline predict-server-register --model-name=mnist --model-tag=gpu 
```
* For GPU-based models, make sure you specify `--model-chip=gpu`

### Start TensorFlow Models on AWS SageMaker
Notes
* You may need to increase your quota limits for the specific instance type with AWS.
* We are using the same instance type for both CPU and GPU model versions.  This is intentional for this demo, but it is not required.
* You can check the CloudWatch [LOGS](#monitor-your-models) to monitor the startup process.

Examples
* `--aws-iam-arn`: arn:aws:iam::<account-number>:role/service-role/AmazonSageMaker-ExecutionRole...

[CPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-cpu)
```
pipeline predict-sage-start --model-name=mnist --model-tag=cpu --aws-iam-arn=<aws-iam-arn> 
```

[GPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-gpu)
```
pipeline predict-sage-start --model-name=mnist --model-tag=gpu --aws-iam-arn=<aws-iam-arn>
```

### Split Traffic Between CPU Model (50%) and GPU Model (50%)
```
pipeline predict-sage-route --model-name=mnist --aws-instance-type-dict='{"cpu":"ml.p2.xlarge", "gpu":"ml.p2.xlarge"}' --model-split-tag-and-weight-dict='{"cpu":50, "gpu":50}'
```
Notes:
* You may need to increase your AWS EC2 quotas for the special `ml.p2.xlarge` instance (note the `ml.` prefix).
* From an Instance Limit standpoint, `ml.p*.*` instances are different than regular `p*.*` instances.  They require a separate quota increase.

![AWS SageMaker Endpoint](http://pipeline.ai/assets/img/sagemaker-cpu-gpu-endpoint.png) 

### Wait for the Model Endpoint to Start
```
pipeline predict-sage-describe --model-name=mnist

### EXPECTED OUTPUT ###
...
Endpoint Status 'InService'  <-- WAIT UNTIL THIS GOES FROM 'Creating' to 'InService' 
...
```
Notes:
* This may take 5-10 mins.  
* To decrease the start time, make sure your Docker images are available in AWS ECR (Elastc Container Registry)
* DockerHub is *very* slow - especially on larger images
* DockerHub will time out often - especially on larger images
* If anything in your Docker image lineage is pulling from DockerHub (ie. FROM docker.io/pipelineai/predict-cpu:1.5.0), you will see 3-5x longer Docker download times
3-5x longer Docker download times lead to 3-5x longer SageMaker startup times.
* *This is not SageMaker's fault!*
* Again, using AWS ECR will put the Docker images closer to SageMaker - enabling faster SageMaker startup times

### Run Load Test on Models CPU and GPU (100 Predictions)
```
pipeline predict-sage-test --model-name=mnist --test-request-path=./tensorflow/mnist-cpu/input/predict/test_request.json --test-request-concurrency=100
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
Notes:
* The logs below show that we are, indeed, using the GPU-based TensorFlow Serving runtime in the GPU model.
* Click [HERE](https://github.com/PipelineAI/serving/blob/536d112b3144a72dec6a64abec67d84e72e455d5/tensorflow_serving/tools/docker/Dockerfile.devel-gpu) for the Dockerfile of the GPU version of TensorFlow Serving that we use below.

![AWS SageMaker + CloudWatch Monitoring](http://pipeline.ai/assets/img/sagemaker-cloudwatch-links.png)

![AWS SageMaker CPU vs. GPU Latency](http://pipeline.ai/assets/img/sagemaker-cpu-gpu-latency.png) 

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

### Stop Model Endpoint
```
pipeline predict-sage-stop --model-name=mnist
```

### Stop Model through AWS SageMaker UI
* Delete Model
* Delete Endpoint Config
* Delete Endpoint

More details [HERE](https://docs.aws.amazon.com/sagemaker/latest/dg/ex1-cleanup.html)


### Distributed TensorFlow Training
* These instructions are under active development

_Note:  The paths below are relative to the sample datasets located here:  `s3://datapalooza-us-west-2/tensorflow/census/input/`._

```
pipeline train-sage-start --model-name=census --model-tag=a --model-type=tensorflow --input-path=./tensorflow/census/input --output-path=./tensorflow/census/output --master-replicas=1 --ps-replicas=1 --worker-replicas=1 --train-args="--train-files=training/adult.training.csv --eval-files=validation/adult.validation.csv --num-epochs=2 --learning-rate=0.025"
```
