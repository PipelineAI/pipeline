![PipelineAI Logo](http://pipeline.ai/assets/img/logo/pipelineai-split-black-258x62.png) + AWS SageMaker

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
pipeline predict-sage-route --model-name=mnist --model-tag-and-weight-dict='{"cpu":50, "gpu":50}'
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

### Clean Up through AWS SageMaker UI

Delete Model

Delete Endpoint Config

Delete Endpoint
