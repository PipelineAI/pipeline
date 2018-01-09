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

## AWS SageMaker + PipelineAI GPU
CloudWatch Logs
```
2018-01-09 06:07:51.114803: I external/org_tensorflow/tensorflow/core/common_runtime/gpu/gpu_device.cc:983] Creating TensorFlow device (/job:localhost/replica:0/task:0/device:GPU:0 with 10765 MB memory) -> physical GPU (device: 0, name: Tesla K80, pci bus id: 0000:00:1e.0, compute capability: 3.7)
2018-01-09 06:07:51.535492: I external/org_tensorflow/tensorflow/cc/saved_model/loader.cc:159] Restoring SavedModel bundle.
2018-01-09 06:07:51.591098: I external/org_tensorflow/tensorflow/cc/saved_model/loader.cc:194] Running LegacyInitOp on SavedModel bundle.
2018-01-09 06:07:51.592331: I external/org_tensorflow/tensorflow/cc/saved_model/loader.cc:289] SavedModel load for tags { serve }; Status: success. Took 4909561 microseconds.
2018-01-09 06:07:51.592443: I tensorflow_serving/servables/tensorflow/saved_model_bundle_factory.cc:93] Wrapping session to perform batch processing
2018-01-09 06:07:51.592492: I tensorflow_serving/servables/tensorflow/bundle_factory_util.cc:153] Wrapping session to perform batch processing
2018-01-09 06:07:51.593283: I tensorflow_serving/core/loader_harness.cc:86] Successfully loaded servable version {name: mnist version: 1510612528}
```

SageMaker Response
```
Variant: 'predict-mnist-e'

('{"variant": "mnist-e-tensorflow-tfserving-gpu", "outputs":{"outputs": '
 '[0.11128010600805283, 1.4478532648354303e-05, 0.43401211500167847, '
 '0.06995825469493866, 0.002808149205520749, 0.2786771059036255, '
 '0.01785111241042614, 0.006651511415839195, 0.07679297775030136, '
 '0.001954274717718363]}}')

Request time: 243.282 milliseconds
```

## Create Traffic Routes (a=97%, b=2%, c=1%)
```
pipeline predict-sage-route --model-name=mnist --model-tag-and-weight-dict='{"a":97, "b":2, "c":1}'
```

## Test the Routes (a=97%, b=2%, c=1%)
```
pipeline predict-sage-test --model-name=mnist --test-request-path=./tensorflow/mnist/input/predict/test_request.json --test-request-concurrency=100
```

## Update Traffic Routes (a=1%, b=2%, c=97%)
```
pipeline predict-sage-route --model-name=mnist --model-tag-and-weight-dict='{"a":1, "b":2, "c":97}'
```

## Test the Routes (a=1%, b=2%, c=97%)
```
pipeline predict-sage-test --model-name=mnist --test-request-path=./tensorflow/mnist/input/predict/test_request.json --test-request-concurrency=100
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
