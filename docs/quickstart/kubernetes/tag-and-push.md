### Pull, Tag, and Push PipelineAI Docker Images
_Email [contact@pipeline.ai](mailto:contact@pipeline.ai) to request access to the PipelineAI Docker Repo._

```
pipeline _env_registry_fullsync --tag=1.5.0 --chip=cpu

pipeline _env_registry_fulltag --from-image-registry-url=docker.io \
                               --from-image-registry-repo=pipelineai \
                               --from-tag=1.5.0 \
                               --to-image-registry-url=<your-docker-repo-url> \
                               --to-image-registry-repo=pipelineai \
                               --to-tag=1.5.0 \
                               --chip=cpu

pipeline _env_registry_fullpush --image-registry-url=<your-docker-repo-url> \
                                --image-registry-repo=pipelineai \
                                --tag=1.5.0 \
                                --chip=cpu
```
