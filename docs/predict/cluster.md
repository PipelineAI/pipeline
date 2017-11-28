These instructions are under active development.

# Deploy to Kubernetes Cluster
This assumes that you have already built your Model + Runtime into a Docker image using `pipeline predict-server-build`.

```
pipeline predict-server-push --model-type=tensorflow --model-name=mnist --model-tag=v1
```

```
pipeline predict-cluster-start --model-type=tensorflow --model-name=mnist --model-tag=v1
```
