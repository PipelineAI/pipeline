These instructions are under active development.

# Deploy to Kubernetes Cluster
_Note:  These instructions apply only to the clustered, enterprise version of PipelineAI._

## Package Model + Runtime into Docker Image
_This can run locally - or on a CI build server anywhere._
```
pipeline predict-server-build --model-type=tensorflow --model-name=mnist --model-tag=a --model-path=./tensorflow/mnist/model
```
```
pipeline predict-server-build --model-type=tensorflow --model-name=mnist --model-tag=b --model-path=./tensorflow/mnist/model
```
```
pipeline predict-server-build --model-type=tensorflow --model-name=mnist --model-tag=c --model-path=./tensorflow/mnist/model
```

## Push Docker Image to Docker Repo

_Note:  We DO support private, internal Docker repos.  Ask PipelineAI support for more details._
```
pipeline predict-server-push --model-type=tensorflow --model-name=mnist --model-tag=a
```
```
pipeline predict-server-push --model-type=tensorflow --model-name=mnist --model-tag=b
```
```
pipeline predict-server-push --model-type=tensorflow --model-name=mnist --model-tag=c
```

## Generate the Kubernetes YAML and Start the Model Server in the Cluster
```
pipeline predict-cluster-start --model-type=tensorflow --model-name=mnist --model-tag=a
```
```
pipeline predict-cluster-start --model-type=tensorflow --model-name=mnist --model-tag=b
```
```
pipeline predict-cluster-start --model-type=tensorflow --model-name=mnist --model-tag=c
```

## Generate Traffic Router Splits
```
pipeline traffic-router-split --model-type=tensorflow --model-name=mnist --model-tag-list=[a,b,c] --model-weight-list=[97,2,1]
```
## Analyze Routers
```
pipeline traffic-router-describe
```

## Scale Out the Model Server
```
pipeline predict-cluster-scale --model-type=tensorflow --model-name=mnist --model-tag=a --replicas=3
```
