These instructions are under active development.

# Pre-requisites
* Kubernetes Cluster with [Istio](https://istio.io/) Installed *or* AWS SageMaker
* If using Windows locally, then install [Powershell](https://github.com/PowerShell/PowerShell)
* Latest `cli-pipeline` installed locally using `pip install`

# Package Model + Runtime into a Docker Image
```
pipeline predict-server-build --model-name=mnist --model-tag=a --model-type=tensorflow --model-path=./tensorflow/mnist/model
```
```
pipeline predict-server-build --model-name=mnist --model-tag=b --model-type=tensorflow --model-path=./tensorflow/mnist/model
```
```
pipeline predict-server-build --model-name=mnist --model-tag=c --model-type=tensorflow --model-path=./tensorflow/mnist/model
```

# Push Docker Image to Docker Repo
* By default, we use the following public DockerHub repo `docker.io/pipelineai`
* By convention, we use `predict-` to namespace our model servers (ie. `predict-mnist`)
* To use your own defaults or conventions, specify `--image-registry-url`, `--image-registry-repo`, or `--image-registry-namespace`
```
pipeline predict-server-push --model-name=mnist --model-tag=a
```
```
pipeline predict-server-push --model-name=mnist --model-tag=b
```
```
pipeline predict-server-push --model-name=mnist --model-tag=c
```

# Kubernetes
* Due to an Istio Ingress [design choice](https://github.com/istio/istio/issues/1752), we need to namespace our prediction calls with `model_name`/invocations instead of just `/invocations`.
* We use Istio RouteRules to rewrite to `/invocations`.
* This will cause issues if you hit the Ingress endpoint without setting up the RouteRules.

## Start the Model Server in the Kubernetes Cluster
```
pipeline predict-kube-start --model-name=mnist --model-tag=a
```
```
pipeline predict-kube-start --model-name=mnist --model-tag=b
```
```
pipeline predict-kube-start --model-name=mnist --model-tag=c
```

## Create Traffic Route Rules (a=34%, b=33%, c=33%)
```
pipeline predict-kube-route --model-name=mnist --model-tag-and-weight-dict='{"a":97, "b":2, "c":1}'
```

## Test the Routes (a=34%, b=33%, c=33%)
```
pipeline predict-kube-test --model-name=mnist --test-request-path=./tensorflow/mnist/input/predict/test_request.json --test-request-concurrency=100
```

## Create Traffic Routes (a=97%, b=2%, c=1%)
```
pipeline predict-kube-route --model-name=mnist --model-tag-and-weight-dict='{"a":97, "b":2, "c":1}'
```

## Test the Routes (a=97%, b=2%, c=1%)
```
pipeline predict-kube-test --model-name=mnist --test-request-path=./tensorflow/mnist/input/predict/test_request.json --test-request-concurrency=100
```

## Update Traffic Routes (a=1%, b=2%, c=97%)
```
pipeline predict-kube-route --model-name=mnist --model_tag_and_weight_dict='{"a":1, "b":2, "c":97}'
```

## Test the Routes (a=1%, b=2%, c=97%)
```
pipeline predict-kube-test --model-name=mnist --test-request-path=./tensorflow/mnist/input/predict/test_request.json --test-request-concurrency=100
```

## Analyze the Routes
```
pipeline predict-kube-describe
```

## Scale Out the Model Server
_Note: The distribution of traffic should remain the same despite scaling out a particular model version._
```
pipeline predict-kube-scale --model-name=mnist --model-tag=a --replicas=3
```

## Test the Routes (a=1% with 3x Replicas, b=2%, c=97%)
* Wait for the scale out (above) to complete before proceeding.
* You should see the same distribution between a, b, and c as above - even with the scale out.  (This is a feature of Istio.)
```
pipeline predict-kube-test --model-name=mnist --test-request-path=./tensorflow/mnist/input/predict/test_request.json --test-request-concurrency=100
```

## Monitor Model Servers
Navigate to the following url to see the models in action:
```
http://hystrix.community.pipeline.ai/hystrix-dashboard/monitor/monitor.html?streams=%5B%7B%22name%22%3A%22%22%2C%22stream%22%3A%22http%3A%2F%2Fturbine.community.pipeline.ai%2Fturbine.stream%22%2C%22auth%22%3A%22%22%2C%22delay%22%3A%22%22%7D%5D
```

# AWS SageMaker 
_Note:  The following CLI commands use boto3 which expects your AWS credentials in `~/.aws/credentials`._

## Start the Model Server in the Kubernetes Cluster
* `aws-iam-arn`: arn:aws:iam::...:role/service-role/AmazonSageMaker-ExecutionRole-...
* `aws-instance-type`: Click [HERE](https://aws.amazon.com/sagemaker/pricing/instance-types/) for instance types.
```
pipeline predict-sage-start --model-name=mnist --model-tag=a --model-type=tensorflow --aws-iam-arn=<aws-iam-arn> --aws-instance-type=<aws-instance-type>
```
```
pipeline predict-sage-start --model-name=mnist --model-tag=b --model-type=tensorflow --aws-iam-arn=<aws-iam-arn> --aws-instance-type=<aws-instance-type>
```
```
pipeline predict-sage-start --model-name=mnist --model-tag=c --model-type=tensorflow --aws-iam-arn=<aws-iam-arn> --aws-instance-type=<aws-instance-type>
```

## Create Traffic Routes (a=97%, b=2%, c=1%)
```
pipeline predict-sage-route --model-name=mnist --aws-instance-type-dict='{"a":"ml.m4.xlarge", "b":"ml.m4.xlarge", "c":"ml.m4.xlarge"}' --model-tag-and-weight-dict='{"a":97, "b":2, "c":1}'
```

## Test the Routes (a=97%, b=2%, c=1%)
```
pipeline predict-sage-test --model-name=mnist --test-request-path=./tensorflow/mnist/input/predict/test_request.json --test-request-concurrency=100
```

## Update Traffic Routes (a=1%, b=2%, c=97%)
```
pipeline predict-sage-route --model-name=mnist --aws-instance-type-dict='{"a":"ml.m4.xlarge", "b":"ml.m4.xlarge", "c":"ml.m4.xlarge"}' --model-tag-and-weight-dict='{"a":1, "b":2, "c":97}'
```

## Test the Routes (a=1%, b=2%, c=97%)
```
pipeline predict-sage-test --model-name=mnist --test-request-path=./tensorflow/mnist/input/predict/test_request.json --test-request-concurrency=100
```

## Monitor Models using AWS Cloud Watch

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

## Analyze Routes
# TODO:  Coming Soon
```
pipeline predict-sage-describe
```

## Scale Out the Model Server
# TODO:  Coming Soon
```
pipeline predict-sage-scale --model-name=mnist --model-tag=a --replicas=3
```
