#!/bin/bash

# Args:
#   $1:  zone
#   $2:  index

gcloud beta compute instances create pipeline-oreilly-gpu-$1-$2 \
    --machine-type n1-highmem-8 --zone $1 \
    --boot-disk-size=100GB --boot-disk-auto-delete --boot-disk-type=pd-ssd \
    --accelerator type=nvidia-tesla-k80,count=1 \
    --image-family ubuntu-1604-lts --image-project ubuntu-os-cloud \
    --maintenance-policy TERMINATE --restart-on-failure \
    --metadata startup-script='#!/bin/bash
    sudo apt-get update
    sudo apt-get install -y wget
    wget https://developer.nvidia.com/compute/cuda/8.0/Prod2/local_installers/cuda_8.0.61_375.26_linux-run
    sudo apt-get install -y linux-headers-$(uname -r)
    sudo apt-get install -y gcc
    sudo apt-get install -y make
    sudo apt-get install -y g++
    chmod a+x cuda_8.0.61_375.26_linux-run
    sudo ./cuda_8.0.61_375.26_linux-run --override --silent --driver --toolkit --toolkitpath=/usr/local/cuda-8.0
    export PATH=$PATH:/usr/local/cuda/bin
    sudo curl -fsSL https://get.docker.com/ | sh
    sudo curl -fsSL https://get.docker.com/gpg | sudo apt-key add -
    wget -P /tmp https://github.com/NVIDIA/nvidia-docker/releases/download/v1.0.1/nvidia-docker_1.0.1-1_amd64.deb
    sudo dpkg -i /tmp/nvidia-docker*.deb && rm /tmp/nvidia-docker*.deb
    sudo docker pull fluxcapacitor/gpu-tensorflow-spark:master
    sudo nvidia-docker run -itd --name=gpu-tensorflow-spark -p 80:80 -p 50070:50070 -p 39000:39000 -p 9000:9000 -p 6006:6006 -p 8754:8754 -p 7077:7077 -p 6066:6066 -p 6060:6060 -p 6061:6061 -p 4040:4040 -p 4041:4041 -p 4042:4042 -p 4043:4043 -p 4044:4044 fluxcapacitor/gpu-tensorflow-spark:master'
