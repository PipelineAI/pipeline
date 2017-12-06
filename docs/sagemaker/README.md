These docs are under active development.

## Use PipelineAI CLI to Tag and Push a Docker Image to ECR
The commands below are a short cut for [THESE](http://docs.aws.amazon.com/AmazonECR/latest/userguide/docker-push-ecr-image.html) steps.
```
pipeline _sage_tag_and_push --src-image-registry-url=docker.io --src-image-registry-repo=pipelineai --src-image-registry-namespace=train --dest-image-registry-url=954636985443.dkr.ecr.us-west-2.amazonaws.com --dest-image-registry-repo=pipelineai --dest-image-registry-namespace=train --model-name=census --model-tag=v1 --model-type=tensorflow
```
```
pipeline _sage_tag_and_push --src-image-registry-url=docker.io --src-image-registry-repo=pipelineai --src-image-registry-namespace=predict --dest-image-registry-url=954636985443.dkr.ecr.us-west-2.amazonaws.com --dest-image-registry-repo=pipelineai --dest-image-registry-namespace=predict --model-name=mnist --model-tag=v1 --model-type=tensorflow
```
