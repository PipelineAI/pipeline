These instructions are under active development.

# Deploy to Kubernetes Cluster
_Note:  These instructions apply only to the clustered, enterprise version of PipelineAI._

## Package Model + Runtime into Docker Image
_This can run locally - or on a CI build server anywhere._
```
pipeline predict-server-build --model-name=mnist --model-tag=a --model-type=tensorflow --model-path=./tensorflow/mnist/model
```
```
pipeline predict-server-build --model-name=mnist --model-tag=b --model-type=tensorflow --model-path=./tensorflow/mnist/model
```
```
pipeline predict-server-build --model-name=mnist --model-tag=c --model-type=tensorflow --model-path=./tensorflow/mnist/model
```

## Push Docker Image to Docker Repo

_Note:  We DO support private, internal Docker repos.  Ask PipelineAI support for more details._
```
pipeline predict-server-push --model-name=mnist --model-tag=a --model-type=tensorflow
```
```
pipeline predict-server-push --model-name=mnist --model-tag=b --model-type=tensorflow 
```
```
pipeline predict-server-push --model-name=mnist --model-tag=c --model-type=tensorflow 
```

## Generate the Kubernetes YAML and Start the Model Server in the Cluster
```
pipeline predict-cluster-start --model-name=mnist --model-tag=a --model-type=tensorflow 
```
```
pipeline predict-cluster-start --model-name=mnist --model-tag=b --model-type=tensorflow 
```
```
pipeline predict-cluster-start --model-name=mnist --model-tag=c --model-type=tensorflow 
```

## Create/Update Traffic Routes
```
pipeline predict-cluster-route --model-name=mnist --model-type=tensorflow --model-tag-list=[a,b,c] --model-weight-list=[97,2,1]
```

## Analyze Routes
```
pipeline predict-cluster-describe
```

## Scale Out the Model Server
```
pipeline predict-cluster-scale --model-name=mnist --model-tag=a --model-type=tensorflow --replicas=3
```
