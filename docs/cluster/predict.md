These instructions are under active development.

# Prerequisites
* Kubernetes Cluster
* [Istio](https://istio.io/)
* Latest `cli-pipeline` installed using `pip install`
* if Windows, then [Powershell](https://github.com/PowerShell/PowerShell)

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
pipeline predict-server-push --model-name=mnist --model-tag=a --model-type=tensorflow
```
```
pipeline predict-server-push --model-name=mnist --model-tag=b --model-type=tensorflow 
```
```
pipeline predict-server-push --model-name=mnist --model-tag=c --model-type=tensorflow 
```

# Kubernetes
## Start the Model Server in the Kubernetes Cluster
```
pipeline predict-kube-start --model-name=mnist --model-tag=a --model-type=tensorflow 
```
```
pipeline predict-kube-start --model-name=mnist --model-tag=b --model-type=tensorflow 
```
```
pipeline predict-kube-start --model-name=mnist --model-tag=c --model-type=tensorflow 
```

## Test the Routes (a=33%, b=33%, c=33%)
```
pipeline predict-kube-test --model-name=mnist --test-request-path=./tensorflow/mnist/input/predict/test_request.json --test-request-concurrency=100
```

## Create Traffic Routes (a=97%, b=2%, c=1%)
```
pipeline predict-kube-route --model-name=mnist --model-type=tensorflow --model-tag-list=[a,b,c] --model-weight-list=[97,2,1]
```

## Test the Routes (a=97%, b=2%, c=1%)
```
pipeline predict-kube-test --model-name=mnist --test-request-path=./tensorflow/mnist/input/predict/test_request.json --test-request-concurrency=100
```

## Update Traffic Routes (a=1%, b=2%, c=97%)
```
pipeline predict-kube-route --model-name=mnist --model-type=tensorflow --model-tag-list=[a,b,c] --model-weight-list=[1,2,97]
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
pipeline predict-kube-scale --model-name=mnist --model-tag=a --model-type=tensorflow --replicas=3
```

# AWS SageMaker 
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
pipeline predict-sage-route --model-name=mnist --model-type=tensorflow --model-tag-list=[a,b,c] --model-weight-list=[97,2,1]
```

## Test the Routes (a=97%, b=2%, c=1%)
```
pipeline predict-sage-test --model-name=mnist --test-request-path=./tensorflow/mnist/input/predict/test_request.json --test-request-concurrency=100
```

## Update Traffic Routes (a=1%, b=2%, c=97%)
```
pipeline predict-sage-route --model-name=mnist --model-type=tensorflow --model-tag-list=[a,b,c] --model-weight-list=[1,2,97]
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
pipeline predict-sage-scale --model-name=mnist --model-tag=a --model-type=tensorflow --replicas=3
```
