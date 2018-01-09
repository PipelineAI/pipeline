![PipelineAI Logo](http://pipeline.ai/assets/img/logo/pipelineai-split-black-258x62.png)

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

### Pull PipelineAI Sample Models
```
git clone https://github.com/PipelineAI/models
```

### Build TensorFlow Models + TensorFlow Serving
CPU
```
pipeline predict-server-build --model-name=mnist --model-tag=cpu --model-type=tensorflow --model-path=./tensorflow/mnist-cpu/model --model-chip=cpu
```
GPU
```
pipeline predict-server-build --model-name=mnist --model-tag=gpu --model-type=tensorflow --model-path=./tensorflow/mnist-gpu/model --model-chip=gpu
```

### TODO: Push TensorFlow Models to Docker Repo
Notes:  
* This can be an AWS ECR Docker Repo - or any public Docker Repo (ie. DockerHub).

Defaults:
* `--image-registry-url`:  docker.io
* `--image-registry-repo`:  pipelineai

CPU
```
pipeline predict-server-push --model-name=mnist --model-tag=cpu --image-registry-url=<your-registry> --image-registry-repo=<your-repo>
```

GPU
```
pipeline predict-server-push --model-name=mnist --model-tag=gpu --image-registry-url=docker.io --image-registry-repo=<your-repo>
```

### Start TensorFlow Models on AWS SageMaker
Notes
* You may need to increase your quota limits for the specific instance type with AWS.
* We are using the same instance type for both CPU and GPU model versions.  This is intentional for this demo, but it is not required.

Examples
* `--aws-iam-arn`: arn:aws:iam::<account-number>:role/service-role/AmazonSageMaker-ExecutionRole-<timestamp>

CPU
```
pipeline predict-sage-start --model-name=mnist --model-tag=cpu --model-type=tensorflow --aws-iam-arn=<aws-iam-arn> --aws-instance-type=ml.p3.2xlarge
```

GPU
```
pipeline predict-sage-start --model-name=mnist --model-tag=gpu --model-type=tensorflow --aws-iam-arn=<aws-iam-arn> --aws-instance-type=ml.p3.2xlarge
```

### Split Traffic Between CPU Model (50%) and GPU Model (50%)
```
pipeline predict-sage-route --model-name=mnist --model-tag-and-weight-dict='{"cpu":50, "gpu":50}'
```

### Run Load Test on Models CPU and GPU
```
pipeline predict-sage-test --model-name=mnist --test-request-path=./tensorflow/mnist-0.025/input/predict/test_request.json --test-request-concurrency=1000
```

**Expected Output**

CPU
```
('{"variant": "mnist-cpu-tensorflow-tfserving-cpu", "outputs":{"outputs": '
 '[0.11128007620573044, 1.4478533557849005e-05, 0.43401220440864563, '
 '0.06995827704668045, 0.0028081508353352547, 0.27867695689201355, '
 '0.017851119861006737, 0.006651509087532759, 0.07679300010204315, '
 '0.001954273320734501]}}')
 
 Request time: 36.414 milliseconds
 ```
 
GPU
```
('{"variant": "mnist-gpu-tensorflow-tfserving-gpu", "outputs":{"outputs": '
 '[0.11128007620573044, 1.4478533557849005e-05, 0.43401220440864563, '
 '0.06995827704668045, 0.0028081508353352547, 0.27867695689201355, '
 '0.017851119861006737, 0.006651509087532759, 0.07679300010204315, '
 '0.001954273320734501]}}')
 
Request time: 29.968 milliseconds
```

### Clean Up through AWS SageMaker UI

Delete Model

Delete Endpoint Config

Delete Endpoint
