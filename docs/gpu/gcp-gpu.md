## Setup GCP Cloud Instance
### Choose Ubuntu 16.04 as the Host Instance OS

_You must choose the `ubuntu-1604-xenial-v20170307` image - not the default Ubuntu 16.04!  Otherwise, the Nvidia drivers specified below will not install properly due to the Linux Kernel version._

_You can use the `gcloud` CLI script (at the bottom of this page) if you cannot select this version from the UI._

### Select GCP Instance including a Tesla K80 GPU

### `ssh` into the GCP Host Instance
```
ssh -i ~/.ssh/<your-pem-file> <username>@<gcp-host-name-or-ipaddress>
```

### Install CUDA 8.0 Drivers and Toolkit
```
sudo apt-get update
sudo apt-get install -y wget
sudo apt-get install -y linux-headers-$(uname -r)
sudo apt-get install -y gcc
sudo apt-get install -y make
sudo apt-get install -y g++
wget https://developer.nvidia.com/compute/cuda/8.0/Prod2/local_installers/cuda-repo-ubuntu1604-8-0-local-ga2_8.0.61-1_amd64-deb
sudo dpkg -i cuda-repo-ubuntu1604-8-0-local-ga2_8.0.61-1_amd64-deb
sudo apt-get update
sudo apt-get install -y cuda
export PATH=$PATH:/usr/local/cuda/bin
export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:/usr/local/cuda/lib64:/usr/local/cuda/extras/CPUTI/lib64"
export CUDA_HOME=/usr/local/cuda
```
^^ YOU MAY WANT TO ADD THE `export` COMMANDS TO YOUR `~/.bashrc` FILE.

### Verify Successful Installation
```
nvidia-smi

### EXPECTED OUTPUT ###
+-----------------------------------------------------------------------------+
| NVIDIA-SMI 375.26                 Driver Version: 375.26                    |
|-------------------------------+----------------------+----------------------+
| GPU  Name        Persistence-M| Bus-Id        Disp.A | Volatile Uncorr. ECC |
| Fan  Temp  Perf  Pwr:Usage/Cap|         Memory-Usage | GPU-Util  Compute M. |
|===============================+======================+======================|
|   0  Tesla K80           Off  | 0000:00:1E.0     Off |                    0 |
| N/A   33C    P8    30W / 149W |      0MiB / 11439MiB |      0%      Default |
+-------------------------------+----------------------+----------------------+

+-----------------------------------------------------------------------------+
| Processes:                                                       GPU Memory |
|  GPU       PID  Type  Process name                               Usage      |
|=============================================================================|
|  No running processes found                                                 |
+-----------------------------------------------------------------------------+
```

```
nvcc --version

### EXPECTED OUTPUT ###
nvcc: NVIDIA (R) Cuda compiler driver
Copyright (c) 2005-2016 NVIDIA Corporation
Built on Tue_Jan_10_13:22:03_CST_2017
Cuda compilation tools, release 8.0, V8.0.61
```
```
sudo modprobe nvidia

### EXPECTED OUTPUT ###
<NOTHING - MAKE SURE THERE IS NO ERROR>
```
```
cd /usr/local/cuda/samples/1_Utilities/deviceQuery

sudo make

./deviceQuery

### EXPECTED OUTPUT ###
./deviceQuery Starting...

 CUDA Device Query (Runtime API) version (CUDART static linking)

Detected 1 CUDA Capable device(s)

Device 0: "Tesla K80"
  CUDA Driver Version / Runtime Version          8.0 / 8.0
  CUDA Capability Major/Minor version number:    3.7
  Total amount of global memory:                 11440 MBytes (11995578368 bytes)
  (13) Multiprocessors, (192) CUDA Cores/MP:     2496 CUDA Cores
  GPU Max Clock rate:                            824 MHz (0.82 GHz)
  Memory Clock rate:                             2505 Mhz
  Memory Bus Width:                              384-bit
  L2 Cache Size:                                 1572864 bytes
  Maximum Texture Dimension Size (x,y,z)         1D=(65536), 2D=(65536, 65536), 3D=(4096, 4096, 4096)
  Maximum Layered 1D Texture Size, (num) layers  1D=(16384), 2048 layers
  Maximum Layered 2D Texture Size, (num) layers  2D=(16384, 16384), 2048 layers
  Total amount of constant memory:               65536 bytes
  Total amount of shared memory per block:       49152 bytes
  Total number of registers available per block: 65536
  Warp size:                                     32
  Maximum number of threads per multiprocessor:  2048
  Maximum number of threads per block:           1024
  Max dimension size of a thread block (x,y,z): (1024, 1024, 64)
  Max dimension size of a grid size    (x,y,z): (2147483647, 65535, 65535)
  Maximum memory pitch:                          2147483647 bytes
  Texture alignment:                             512 bytes
  Concurrent copy and kernel execution:          Yes with 2 copy engine(s)
  Run time limit on kernels:                     No
  Integrated GPU sharing Host Memory:            No
  Support host page-locked memory mapping:       Yes
  Alignment requirement for Surfaces:            Yes
  Device has ECC support:                        Enabled
  Device supports Unified Addressing (UVA):      Yes
  Device PCI Domain ID / Bus ID / location ID:   0 / 0 / 4
  Compute Mode:
     < Default (multiple host threads can use ::cudaSetDevice() with device simultaneously) >

deviceQuery, CUDA Driver = CUDART, CUDA Driver Version = 8.0, CUDA Runtime Version = 8.0, NumDevs = 1, Device0 = Tesla K80
Result = PASS
```

### (OPTIONAL) Setup Docker v1.17+ on Cloud Instance
* **DO NOT RELY ON THE DEFAULT VERSION PROVIDED BY YOUR OS!**
* If Docker already exists, check the version with the following
```
docker version
```
**This must be v1.17+!!!**

Install the latest Docker if needed
```
sudo apt-get update

sudo curl -fsSL https://get.docker.com/ | sh

sudo curl -fsSL https://get.docker.com/gpg | sudo apt-key add -
```

### Setup [Nvidia Docker](https://github.com/NVIDIA/nvidia-docker/wiki/Why%20NVIDIA%20Docker) on Cloud Instance 
```
wget -P /tmp https://github.com/NVIDIA/nvidia-docker/releases/download/v1.0.1/nvidia-docker_1.0.1-1_amd64.deb

sudo dpkg -i /tmp/nvidia-docker*.deb 

rm /tmp/nvidia-docker*.deb
```
Note:  Newer versions of `nvidia-docker` are untested, but may be available [here](https://github.com/NVIDIA/nvidia-docker/releases/).

### Verify Successful `nvidia-docker` Setup
```
sudo nvidia-docker run --rm nvidia/cuda nvidia-smi

### EXPECTED OUTPUT ###
+-----------------------------------------------------------------------------+
| NVIDIA-SMI 375.26                 Driver Version: 375.26                    |
|-------------------------------+----------------------+----------------------+
| GPU  Name        Persistence-M| Bus-Id        Disp.A | Volatile Uncorr. ECC |
| Fan  Temp  Perf  Pwr:Usage/Cap|         Memory-Usage | GPU-Util  Compute M. |
|===============================+======================+======================|
|   0  Tesla K80           Off  | 0000:00:1E.0     Off |                    0 |
| N/A   33C    P8    30W / 149W |      0MiB / 11439MiB |      0%      Default |
+-------------------------------+----------------------+----------------------+

+-----------------------------------------------------------------------------+
| Processes:                                                       GPU Memory |
|  GPU       PID  Type  Process name                               Usage      |
|=============================================================================|
|  No running processes found                                                 |
+-----------------------------------------------------------------------------+
```

### Download PipelineAI Standalone GPU Docker Image
This image contains JupyterHub, Spark, TensorFlow, Tensorboard, and HDFS - as well as many sample notebooks.

### Run PipelineAI Standalone GPU Docker Container
* Start the Docker Container with a Jupyter/iPython notebook 
* Note:  This Docker image is based on this [Dockerfile](https://github.com/tensorflow/tensorflow/blob/master/tensorflow/tools/docker/Dockerfile.devel-gpu),
 but contains many optimizations and installations for real-world ML and AI model training, optimizing, and deploying.
```
sudo nvidia-docker run -itd --name=gpu-tensorflow-spark -p 80:80 -p 50070:50070 -p 39000:39000 -p 9000:9000 -p 9001:9001 -p 9002:9002 -p 9003:9003 -p 9004:9004 -p 6006:6006 -p 8754:8754 -p 7077:7077 -p 6066:6066 -p 6060:6060 -p 6061:6061 -p 4040:4040 -p 4041:4041 -p 4042:4042 -p 4043:4043 -p 4044:4044 -p 2222:2222 -p 5050:5050 fluxcapacitor/gpu-tensorflow-spark:master
```

## Verify Successful Startup
Shell into the running Docker Container
```
sudo nvidia-docker exec -it gpu-tensorflow-spark bash
```

Verify that `nvidia-smi` works within the Docker Container
```
nvidia-smi

### EXPECTED OUTPUT ###
+-----------------------------------------------------------------------------+
| NVIDIA-SMI 375.26                 Driver Version: 375.26                    |
|-------------------------------+----------------------+----------------------+
| GPU  Name        Persistence-M| Bus-Id        Disp.A | Volatile Uncorr. ECC |
| Fan  Temp  Perf  Pwr:Usage/Cap|         Memory-Usage | GPU-Util  Compute M. |
|===============================+======================+======================|
|   0  Tesla K80           Off  | 0000:00:04.0     Off |                    0 |
| N/A   36C    P8    27W / 149W |      0MiB / 11439MiB |      0%      Default |
+-------------------------------+----------------------+----------------------+

+-----------------------------------------------------------------------------+
| Processes:                                                       GPU Memory |
|  GPU       PID  Type  Process name                               Usage      |
|=============================================================================|
|  No running processes found                                                 |
+-----------------------------------------------------------------------------+
```

* Navigate your browser to the TensorBoard UI
```
http://<your-cloud-ip>:6006
```

![Tensorboard](http://pipeline.ai/assets/img/tensorboard-2.png)

### Navigate to Jupyter Notebooks
```
http://<your-cloud-ip>:8754
```

Login to Jupyter Notebook

**Use any username/password.**

![Jupyter Login](http://pipeline.ai/assets/img/jupyter-0.png)

Run the Notebooks
![Jupyter Login](http://pipeline.ai/assets/img/jupyter-1.png)

### Stop and Start Cloud Instance (ie. Cost Savings)
* To save money, you can stop/start the GCP instance as needed

* After cloud instance re-start, start the Docker Container
```
sudo nvidia-docker start gpu-tensorflow-spark
```
_Note: If you see an error related to `modprobe not running` or `Error looking up volume plugin nvidia-docker`, just re-install the CUDA 8.0 Toolkit and `nvidia-docker` plugin as you did above._

* Navigate to JupyterHub
```
http://<your-cloud-ip>:8754
```

* Shell back into the Docker Container
```
sudo nvidia-docker exec -it gpu-tensorflow-spark bash
```

## Create Instance Using [`gcloud` CLI](https://cloud.google.com/sdk/downloads)
_Note:  GPU's are not available in all regions and zones.  Please check with the cloud provider before picking a zone._
```
gcloud beta compute instances create pipeline-gpu \
    --machine-type n1-standard-4 \
    --min-cpu-platform "Intel Broadwell" \
    --zone us-west1-c \
    --boot-disk-size=100GB --boot-disk-auto-delete --boot-disk-type=pd-ssd \
    --accelerator type=nvidia-tesla-k80,count=1 \
    --image ubuntu-1604-xenial-v20170307 \
    --image-project ubuntu-os-cloud \
    --maintenance-policy TERMINATE \
    --restart-on-failure \
    --metadata startup-script='#!/bin/bash
    sudo apt-get update
    sudo apt-get install -y wget
    sudo apt-get install -y linux-headers-$(uname -r)
    sudo apt-get install -y gcc
    sudo apt-get install -y make
    sudo apt-get install -y g++
    wget https://developer.nvidia.com/compute/cuda/8.0/Prod2/local_installers/cuda-repo-ubuntu1604-8-0-local-ga2_8.0.61-1_amd64-deb
    sudo dpkg -i cuda-repo-ubuntu1604-8-0-local-ga2_8.0.61-1_amd64-deb
    sudo apt-get update
    sudo apt-get install -y cuda
    export PATH=$PATH:/usr/local/cuda/bin
    sudo curl -fsSL https://get.docker.com/ | sh
    sudo curl -fsSL https://get.docker.com/gpg | sudo apt-key add -
    wget -P /tmp https://github.com/NVIDIA/nvidia-docker/releases/download/v1.0.1/nvidia-docker_1.0.1-1_amd64.deb
    sudo dpkg -i /tmp/nvidia-docker*.deb && rm /tmp/nvidia-docker*.deb
    sudo nvidia-docker run -itd --name=gpu-tensorflow-spark -p 80:80 -p 50070:50070 -p 39000:39000 -p 9000:9000 -p 9001:9001 -p 9002:9002 -p 9003:9003 -p 9004:9004 -p 6006:6006 -p 8754:8754 -p 7077:7077 -p 6066:6066 -p 6060:6060 -p 6061:6061 -p 4040:4040 -p 4041:4041 -p 4042:4042 -p 4043:4043 -p 4044:4044 -p 2222:2222 -p 5050:5050 fluxcapacitor/gpu-tensorflow-spark:master
    sleep 15
    sudo nvidia-docker exec -d gpu-tensorflow-spark service apache2 restart'
```

## References
* [Dockerfile](https://github.com/fluxcapacitor/pipeline/tree/master/gpu.ml)
* [DockerHub Image](https://hub.docker.com/r/fluxcapacitor/gpu-tensorflow-spark)
