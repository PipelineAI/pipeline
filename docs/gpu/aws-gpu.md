## Setup AWS Cloud Instance
### Choose Ubuntu 16.04 as the EC2 Host Instance OS

![Ubuntu 16.04](http://advancedspark.com.s3-website-us-west-2.amazonaws.com/img/aws-ec2-step-1-16-04.png)

_You must choose an AMI with a version of Ubuntu 16.04 that is compatible with the Nvidia drivers specified below! Otherwise, the Nvidia drivers won't install properly._

_GPU's are not available in all regions and zones.  Please check with the cloud provider before picking a zone._

### Modify the Default ROOT VOLUME SIZE from 8GB to 100GB

![AWS Root Volume](http://advancedspark.com.s3-website-us-west-2.amazonaws.com/img/aws-ec2-step-4.png)

### Create `p2` or `g2` GPU-based EC2 Host Instance

![GPU P-Series EC2 Instances](https://s3.amazonaws.com/fluxcapacitor.com/img/aws-p2-series-gpu-instances.png)

![GPU G-Series EC2 Instances](http://advancedspark.com.s3-website-us-west-2.amazonaws.com/img/aws-gpu-instances.png)

### `ssh` into the EC2 Host Instance
```
ssh -i ~/.ssh/<your-pem-file> ubuntu@<ec2-host-name-or-ipaddress>
```

## Install CUDA 8.0 Drivers and Toolkit
_You may want to follow [THESE](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/install-nvidia-driver.html) instructions by AWS if you have problems with the instructions below._

_You can also try [THESE](https://mengdong.github.io/2017/07/14/kubernetes-1.7-gpu-on-mapr-distributed-deep-learning/) instructions._

```
sudo apt-get update
sudo apt-get install -y wget
sudo apt-get install -y linux-headers-$(uname -r)
sudo apt-get install -y gcc
sudo apt-get install -y make
sudo apt-get install -y g++
```
```
cat << EOF | sudo tee --append /etc/modprobe.d/blacklist.conf
blacklist vga16fb
blacklist nouveau
blacklist rivafb
blacklist nvidiafb
blacklist rivatv
EOF
```
```
sudo reboot
```
Wait for reboot and `ssh` back in...
```
wget https://developer.nvidia.com/compute/cuda/8.0/Prod2/local_installers/cuda-repo-ubuntu1604-8-0-local-ga2_8.0.61-1_amd64-deb
sudo dpkg -i cuda-repo-ubuntu1604-8-0-local-ga2_8.0.61-1_amd64-deb
```
```
sudo apt-key add /var/cuda-repo-8-0-local-ga2/7fa2af80.pub
sudo apt-get update
sudo apt-get install -y cuda-toolkit-8-0
```
```
export PATH=/usr/local/cuda/bin:$PATH
echo "export PATH=/usr/local/cuda/bin:$PATH" >> ~/.bashrc

export LD_LIBRARY_PATH=/usr/local/cuda/lib64:/usr/lib/x86_64-linux-gnu:$LD_LIBRARY_PATH
echo "LD_LIBRARY_PATH=/usr/local/cuda/lib64:/usr/lib/x86_64-linux-gnu:$LD_LIBRARY_PATH" >> ~/.bashrc
```

### Verify Installation
```
nvidia-smi

### EXPECTED OUTPUT ###
+-----------------------------------------------------------------------------+
| NVIDIA-SMI 367.106                Driver Version: 367.106                   |
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
```

```
sudo modprobe nvidia

### EXPECTED OUTPUT ###
     <- No Output is Good!
```

```
cd /usr/local/cuda/samples/1_Utilities/deviceQuery

sudo make

./deviceQuery
```
### Optimize Your GPU Instance
Check out [THIS](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/optimize_gpu.html) page for GPU Tuning Tips from Amazon.

## Setup Latest Docker on Cloud Instance
* Must be Docker v1.12+
* **DO NOT RELY ON THE DEFAULT VERSION PROVIDED BY YOUR OS!**
```
sudo apt-get update
sudo curl -fsSL https://get.docker.com/ | sh
sudo curl -fsSL https://get.docker.com/gpg | sudo apt-key add -

### EXPECTED OUTPUT ###
# If you would like to use Docker as a non-root user, you should now consider
# adding your user to the "docker" group with something like:
#
#  sudo usermod -aG docker ubuntu
#
# Remember that you will have to log out and back in for this to take effect!
#
# ^^ IGNORE THIS ^^
```

## Setup [Nvidia Docker](https://github.com/NVIDIA/nvidia-docker/wiki/Why%20NVIDIA%20Docker) on Cloud Instance 
```
wget -P /tmp https://github.com/NVIDIA/nvidia-docker/releases/download/v1.0.1/nvidia-docker_1.0.1-1_amd64.deb
sudo dpkg -i /tmp/nvidia-docker*.deb && rm /tmp/nvidia-docker*.deb
```

### Test nvidia-smi
```
sudo nvidia-docker run --rm nvidia/cuda nvidia-smi

### EXPECTED OUTPUT ###
+-----------------------------------------------------------------------------+
| NVIDIA-SMI 367.106                Driver Version: 367.106                   |
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
Note:  Newer versions are untested, but available [here](https://github.com/NVIDIA/nvidia-docker/releases/).

### Download Docker Image with Tensorflow Examples
```
sudo docker pull fluxcapacitor/gpu-tensorflow-spark:master
```

## Setup Docker Container on Cloud Instance
* Start the Docker Container with a Jupyter/iPython notebook 
* Note:  This Docker image is based on this [Dockerfile](https://github.com/tensorflow/tensorflow/blob/master/tensorflow/tools/docker/Dockerfile.devel-gpu)
```
sudo nvidia-docker run -itd --name=gpu-tensorflow-spark -p 80:80 -p 50070:50070 -p 39000:39000 -p 9000:9000 -p 9001:9001 -p 9002:9002 -p 9003:9003 -p 9004:9004 -p 6006:6006 -p 8754:8754 -p 7077:7077 -p 6066:6066 -p 6060:6060 -p 6061:6061 -p 4040:4040 -p 4041:4041 -p 4042:4042 -p 4043:4043 -p 4044:4044 -p 2222:2222 -p 5050:5050 fluxcapacitor/gpu-tensorflow-spark:master
```

## Verify Successful Setup
```
sudo nvidia-docker exec -it gpu-tensorflow-spark bash
```
```
nvidia-smi
```

* `g2.2xlarge` EC2 Instance (1 Nvidia K520 GPU)

![AWS GPU Nvidia Docker](http://advancedspark.com.s3-website-us-west-2.amazonaws.com/img/aws-docker-gpu-verify-0.png)

* `g2.8xlarge` EC2 Instance (4 Nvidia K520 GPUs)

![AWS 4 GPU Nvidia Docker](http://advancedspark.com.s3-website-us-west-2.amazonaws.com/img/aws-4-gpu-instances-0.png)

* `p2.xlarge` EC2 Instance (1 Nvidia K80 GPU)

![AWS P2 xlarge GPU Nvidia Docker](http://advancedspark.com.s3-website-us-west-2.amazonaws.com/img/aws-docker-p2-xlarge-gpu-verify-0.png)

* `p2.16xlarge` EC2 Instance (16 Nvidia K80 GPU)

![AWS P2 xlarge GPU Nvidia Docker](http://advancedspark.com.s3-website-us-west-2.amazonaws.com/img/aws-docker-p2-16xlarge-gpu-verify.png)

### Verify Running Processes
```
ps -aef | grep jupyter

### EXPECTED OUTPUT ###
UID        PID  PPID  C STIME TTY          TIME CMD
root         1     0  0 18:47 ?        00:00:00 supervise .
root         7     1  0 18:47 ?        00:00:00 bash ./run
root         8     7  2 18:47 ?        00:00:02 /opt/conda/bin/python /opt/conda/bin/tensorboard --host=0.0.0.0 --logdir=/root/tensorboard
root        17     1  4 18:47 ?        00:00:04 /usr/lib/jvm/java-8-openjdk-amd64//bin/java -cp /root/spark-2.1.0-bin-fluxcapacitor/conf/:/root/spark-2.1.0-bin-fluxcapacitor/jars/*:/root/hadoop-2.7.2/etc/hadoop/ -Xmx1g org.apache.spark.deploy.master.Master --host 31a73be579cc --port 7077 --webui-port 8080 --webui-port 6060
root        89     1  4 18:47 ?        00:00:04 /usr/lib/jvm/java-8-openjdk-amd64//bin/java -cp /root/spark-2.1.0-bin-fluxcapacitor/conf/:/root/spark-2.1.0-bin-fluxcapacitor/jars/*:/root/hadoop-2.7.2/etc/hadoop/ -Xmx1g org.apache.spark.deploy.worker.Worker --webui-port 8081 --cores 4 --memory 4g --webui-port 6061 spark://127.0.0.1:7077
root       124     0  0 18:47 ?        00:00:00 bash
root       162     7  0 18:47 ?        00:00:00 /opt/conda/bin/python /opt/conda/bin/jupyterhub --ip= --config=config/jupyterhub/jupyterhub_config.py
root       166   162  0 18:47 ?        00:00:00 node /opt/conda/bin/configurable-http-proxy --ip  --port 8754 --api-ip 0.0.0.0 --api-port 8755 --default-target http://127.0.0.1:8081 --error-target http://127.0.0.1:8081/hub/error
root       184   124  0 18:49 ?        00:00:00 ps -aef
```

### Run TensorBoard Model-Training Dashboard
* Start TensorBoard in the background (`&`)
* Note:  This requires a TensorFlow/Notebook to write to logdir with [SummaryWriter].(https://www.tensorflow.org/versions/r0.12/api_docs/python/train.html#SummaryWriter)
```
tensorboard --logdir /root/train &

### EXPECTED OUTPUT ###
...
I tensorflow/stream_executor/dso_loader.cc:128] successfully opened CUDA library libcublas.so locally
I tensorflow/stream_executor/dso_loader.cc:128] successfully opened CUDA library libcudnn.so locally
I tensorflow/stream_executor/dso_loader.cc:128] successfully opened CUDA library libcufft.so locally
I tensorflow/stream_executor/dso_loader.cc:128] successfully opened CUDA library libcuda.so.1 locally
I tensorflow/stream_executor/dso_loader.cc:128] successfully opened CUDA library libcurand.so locally

Starting TensorBoard on port 6006
(You can navigate to http://<your-cloud-ip>:6006)
...
```
* Hit `Enter` a few times

* Navigate your browser to the TensorBoard UI
```
http://<your-cloud-ip>:6006
```

![Tensorboard](http://advancedspark.com.s3-website-us-west-2.amazonaws.com/img/tensorboard-0.png)

### Single-GPU Tensorflow Training Example
* Note:  The first operation on *each* GPU will incur a significant performance overhead during [PTX assembly compilation](http://stackoverflow.com/questions/40410210/tensorflow-2-gpu-slower-then-single-gpu/40430717#40430717).

* Let's use [this](https://github.com/tensorflow/tensorflow/blob/r0.12/tensorflow/models/image/cifar10/cifar10_multi_gpu_train.py) example.

* **1 GPU**:  Note `examples/sec` and `sec/batch`
```
python /root/tensorflow/tensorflow/models/image/cifar10/cifar10_multi_gpu_train.py --num_gpus=1 --max_steps=1000 --train_dir=/root/train/1gpu

### EXPECTED OUTPUT ###
...
2016-09-08 16:10:30.718689: step 990, loss = 2.51 (717.6 examples/sec; 0.178 sec/batch)
```

### Multiple-GPU Tensorflow Training Example
* This will only work if your EC2 instance has >1 GPU (ie. p2.16xlarge, g2.8xlarge)
* Note:  The first operation on *each* GPU will incur a significant performance overhead during [PTX assembly compilation](http://stackoverflow.com/questions/40410210/tensorflow-2-gpu-slower-then-single-gpu/40430717#40430717).

![Multi-GPU Training](http://advancedspark.com.s3-website-us-west-2.amazonaws.com/img/multiple-gpu.png)

*Step 1*:  CPU transfers model to each GPU 

*Step 2*:  CPU synchronizes and waits for all GPUs to process batch

*Step 3*:  CPU copies all training results (gradients) back from GPU

*Step 4*:  CPU builds new model from average of gradients from all GPUs

*Step 5*:  Repeat Step 1 until stop condition is reached (ie. `--max_steps=1000`)

* Let's use the [same](https://github.com/tensorflow/tensorflow/blob/r0.12/tensorflow/models/image/cifar10/cifar10_multi_gpu_train.py) example.

* **2 GPUs**:  Note the increase in `examples/sec` and `sec/batch`
```
python /root/tensorflow/tensorflow/models/image/cifar10/cifar10_multi_gpu_train.py --num_gpus=2 --max_steps=1000 --train_dir=/root/train/2gpu

### EXPECTED OUTPUT ###
...
2016-09-08 16:06:01.299470: step 990, loss = 2.31 (1342.8 examples/sec; 0.095 sec/batch)
```

* **4 GPUs**:  Note the increase in `examples/sec` and `sec/batch`
```
python /root/tensorflow/tensorflow/models/image/cifar10/cifar10_multi_gpu_train.py --num_gpus=4 --max_steps=1000 --train_dir=/root/train/4gpu

### EXPECTED OUTPUT ###
...
2016-09-08 15:59:51.653752: step 990, loss = 2.36 (1925.9 examples/sec; 0.066 sec/batch)
```

* **16 GPUs**:  Note the increase in `examples/sec` and `sec/batch`
```
python /root/tensorflow/tensorflow/models/image/cifar10/cifar10_multi_gpu_train.py --num_gpus=16 --max_steps=1000 --train_dir=/root/train/16gpu

### EXPECTED OUTPUT ###
...
2016-12-27 17:15:23.486812: step 990, loss = 2.30 (2401.1 examples/sec; 0.053 sec/batch)
```

![AWS P2 xlarge GPU Nvidia Docker](https://s3-us-west-2.amazonaws.com/advancedspark.com/img/aws-docker-p2-16xlarge-training-0.png)

* Navigate your browser to the TensorBoard UI
```
http://<your-cloud-ip>:6006
```

![Tensorboard](http://advancedspark.com.s3-website-us-west-2.amazonaws.com/img/tensorboard-2.png)

### Run TensorFlow Jupyter/iPython Notebooks 
* Run example notebooks
```
http://<your-cloud-ip>:8754
```

* Login to Jupyter Notebook (Use Any Username/Password)

* Run the Examples Notebooks
![Jupyter Login](http://advancedspark.com.s3-website-us-west-2.amazonaws.com/img/jupyter-1.png)

### Stop and Start Cloud Instance (ie. Cost Savings)
* To save money, you can stop the EC2 instance when you are done experimenting
* When you start the cloud instance back up, you may need to start the Docker container
```
sudo nvidia-docker start gpu-tensorflow-spark
```
* Then you can use Jupyter again
```
http://<your-cloud-ip>:8754
```
or shell back into the Docker container 
```
sudo nvidia-docker exec -it gpu-tensorflow-spark bash
```

## References
* [Dockerfile](https://github.com/fluxcapacitor/pipeline/tree/master/gpu.ml)
* [DockerHub Image](https://hub.docker.com/r/fluxcapacitor/)
