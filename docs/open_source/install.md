# Installing PipelineIO in Your Environment
While we recommend the hosted [PipelineIO Community Edition](http://community.pipeline.io) when evaluating PipelineIO, we provide installation instructions below to setup PipelineIO in your own cloud-based or on-premise environment.

Note:  Support is limited for this offering, but we would love your feedback, bug reports, and feature requests [here](https://pipelineio.zendesk.com). 

## Setup Standalone PipelineIO
The standalone PipelineIO Community Edition uses a single Docker image that can run in any CPU and GPU-based environment that supports [Docker](https://www.docker.com/) for CPUs or [Nvidia-Docker](https://github.com/NVIDIA/nvidia-docker) for GPUs.

![Nvidia GPU](/img/nvidia-cuda-338x181.png)

![Docker](/img/docker-logo-150x126.png)

### [AWS GPU](https://github.com/fluxcapacitor/pipeline/wiki/AWS-GPU-Tensorflow-Docker)
Standalone Docker Image + PipelineIO Community Edition for AWS GPU Instance

### [Google GPU](https://github.com/fluxcapacitor/pipeline/wiki/GCP-GPU-Tensorflow-Docker)
Standalone Docker Image + PipelineIO Community Edition for Google GPU Instance

## Setup Clustered PipelineIO
PipelineIO uses Kubernetes for Docker Container management and orchestration.

![Nvidia GPU](/img/nvidia-cuda-338x181.png)

![Kubernetes](/img/kubernetes-logo-200x171.png)

![Docker](/img/docker-logo-150x126.png)

![PipelineIO Cluster](/img/weavescope-pipelineio.png)

### [Mini](https://github.com/fluxcapacitor/pipeline/wiki/Setup-Pipeline-Mini) 
Mini Kubernetes Cluster + PipelineIO Community on Local Laptop or Low-Memory Server

### [AWS](https://github.com/fluxcapacitor/pipeline/wiki/Setup-Pipeline-AWS)
Full Kubernetes Cluster + PipelineIO Community on AWS.  

This requires large AWS instance types with at least 60 GB RAM, 8 CPUs, 100 GB Disk.

![AWS](/img/aws-logo-185x73.png)

### [Google Cloud](https://github.com/fluxcapacitor/pipeline/wiki/Setup-Pipeline-Google)
Full Kubernetes Cluster + PipelineIO Community on Google Cloud

This requires large Google Cloud instance types with at least 52 GB RAM, 8 CPUs, 100 GB Disk.

![Google Cloud Platform](/img/gce-logo-190x90.png)

### [Azure](https://github.com/fluxcapacitor/pipeline/wiki/Setup-Pipeline-Azure)
Full Kubernetes Cluster + PipelineIO Community Edition on Azure

This requires large Google Cloud instance types with at least 60 GB RAM, 8 CPUs, 100 GB Disk.

![Azure](/img/azure-logo-200x103.png)

{!contributing.md!}
