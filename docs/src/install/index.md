# Install PipelineAI Open Source 
While we recommend the hosted [PipelineAI Community Edition](http://community.pipeline.io) when evaluating PipelineAI, we provide installation instructions below to setup PipelineAI in your own cloud-based or on-premise environment.

Note:  Support is limited for this offering, but we would love your feedback, bug reports, and feature requests [HERE](https://pipelineio.zendesk.com). 

## Standalone 
The standalone PipelineAI Community Edition uses a single Docker image that can run in any CPU and GPU-based environment that supports [Docker](https://www.docker.com/) for CPUs or [Nvidia-Docker](https://github.com/NVIDIA/nvidia-docker) for GPUs.

![Nvidia GPU](/img/nvidia-cuda-338x181.png)

![Docker](/img/docker-logo-150x126.png)

### AWS GPU
[AWS GPU](https://github.com/fluxcapacitor/pipeline/wiki/AWS-GPU-Tensorflow-Docker)

### Google Cloud GPU
[Google Cloud GPU](https://github.com/fluxcapacitor/pipeline/wiki/GCP-GPU-Tensorflow-Docker)

### AWS CPU
[AWS CPU](https://github.com/fluxcapacitor/pipeline/wiki/AWS-CPU-Tensorflow-Docker)

### Google Cloud CPU
[Google Cloud CPU](https://github.com/fluxcapacitor/pipeline/wiki/GCP-CPU-Tensorflow-Docker)

## Distributed 
PipelineAI uses Kubernetes for Docker Container management and orchestration.

![Kubernetes](/img/kubernetes-logo-200x171.png)

![PipelineAI Cluster](/img/weavescope-pipelineio.png)

### [Local](https://github.com/fluxcapacitor/pipeline/wiki/Setup-Pipeline-Mini) 
Local, Mini Kubernetes Cluster + PipelineAI Community on Local Laptop or Low-Memory Server

### [AWS](https://github.com/fluxcapacitor/pipeline/wiki/Setup-Pipeline-AWS)
Full Kubernetes Cluster + PipelineAI Community on AWS.

![AWS](/img/aws-logo-185x73.png)

This requires large instance types with at least 50 GB RAM, 8 CPUs, 100 GB Disk.

### [Google Cloud](https://github.com/fluxcapacitor/pipeline/wiki/Setup-Pipeline-Google)
Full Kubernetes Cluster + PipelineAI Community on Google Cloud

![Google Cloud Platform](/img/gce-logo-190x90.png)

This requires large instance types with at least 50 GB RAM, 8 CPUs, 100 GB Disk.

### [Azure](https://github.com/fluxcapacitor/pipeline/wiki/Setup-Pipeline-Azure)
Full Kubernetes Cluster + PipelineAI Community Edition on Azure

![Azure Cluster](/img/azure-logo-200x103.png)

This requires large instance types with at least 50 GB RAM, 8 CPUs, 100 GB Disk.

{!contributing.md!}
