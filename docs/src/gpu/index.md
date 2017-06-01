# GPUs and Performance
## GPUs
PipelineIO supports GPUs natively throughout the entire platform.

![Nvidia GPU](/img/nvidia-cuda-338x181.png)

Here are some publically-available resources including Docker images, source code, Nvidia driver and toolkit configuration, videos, slides, and workshop materials that demonstrate PipelineIO's support for GPUs.

### [GitHub Repo](https://github.com/fluxcapacitor/pipeline/tree/master/gpu.ml)
[fluxcapacitor/pipeline/gpu.ml](https://github.com/fluxcapacitor/pipeline/tree/master/gpu.ml)

[fluxcapacitor/pipeline/package.ml](https://github.com/fluxcapacitor/pipeline/tree/master/package.ml)

### [Docker Images](https://hub.docker.com/r/fluxcapacitor/)
[AWS (GPU) + TensorFlow + Spark + HDFS + Docker](https://github.com/fluxcapacitor/pipeline/wiki/AWS-GPU-Tensorflow-Docker)

[Google Cloud (GPU) + TensorFlow + Spark + HDFS + Docker](https://github.com/fluxcapacitor/pipeline/wiki/GCP-GPU-Tensorflow-Docker)

## Performance
PipelineIO maintains a collection of [Docker Images](https://hub.docker.com/r/fluxcapacitor) with many optimizations already enabled including OpenBLAS, AVX, AVX2, FMA, etc.

### [GitHub Repo](https://github.com/fluxcapacitor/pipeline/tree/master/package.ml)

[fluxcapacitor/pipeline/package.ml](https://github.com/fluxcapacitor/pipeline/tree/master/package.ml)

### [Docker Images](https://hub.docker.com/r/fluxcapacitor/)

[AWS (CPU) + TensorFlow + Spark + HDFS + Docker](https://github.com/fluxcapacitor/pipeline/wiki/AWS-CPU-Tensorflow-Docker)

[Google Cloud (CPU) + TensorFlow + Spark + HDFS + Docker](https://github.com/fluxcapacitor/pipeline/wiki/GCP-CPU-Tensorflow-Docker)

## ML/AI Model Performance Optimizations
Click [HERE](http://pipeline.io/model_optimize/) for examples of optimizing model prediction performance with PipelineIO.

### TensorFlow Example
**Before Optimization**

![Unoptimized TensorFlow Model](http://pipeline.io/img/unoptimized-tensorflow-linear.png)

**After Optimization**

![Optimized TensorFlow Model](http://pipeline.io/img/optimized-tensorflow-linear.png)

## Recent [Events](/events/index.md) 
[Videos and Slides](/events/index.md)

{!contributing.md!}
