These instructions are under active development.

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
_Note:  By default, we use DockerHub.  However, you can also specify a private, internal Docker repo._
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

## Create/Update Traffic Routes
```
pipeline predict-kube-route --model-name=mnist --model-type=tensorflow --model-tag-list=[a,b,c] --model-weight-list=[97,2,1]
```

## Analyze Routes
```
pipeline predict-kube-describe
```

## Scale Out the Model Server
```
pipeline predict-kube-scale --model-name=mnist --model-tag=a --model-type=tensorflow --replicas=3
```

# AWS SageMaker 
## Start the Model Server in the Kubernetes Cluster
```
pipeline predict-sage-start --model-name=mnist --model-tag=a --model-type=tensorflow --aws-iam-arn=<aws-iam-arn> --aws-instance-type=<aws-instance-type>
```
```
pipeline predict-sage-start --model-name=mnist --model-tag=b --model-type=tensorflow --aws-iam-arn=<aws-iam-arn> --aws-instance-type=<aws-instance-type>
```
```
pipeline predict-sage-start --model-name=mnist --model-tag=c --model-type=tensorflow --aws-iam-arn=<aws-iam-arn> --aws-instance-type=<aws-instance-type>
```

## Create/Update Traffic Routes
```
pipeline predict-sage-route --model-name=mnist --model-type=tensorflow --model-tag-list=[a,b,c] --model-weight-list=[97,2,1]
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
