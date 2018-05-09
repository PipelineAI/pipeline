## CUDA + cuDNN
```
wget http://developer.download.nvidia.com/compute/cuda/repos/ubuntu1604/x86_64/cuda-repo-ubuntu1604_9.0.176-1_amd64.deb
sudo dpkg -i cuda-repo-ubuntu1604_9.0.176-1_amd64.deb

sudo apt-key adv --fetch-keys http://developer.download.nvidia.com/compute/cuda/repos/ubuntu1604/x86_64/7fa2af80.pub
sudo apt-get update
sudo apt-get install cuda

wget .../libcudnn7-doc_7.0.5.15-1%2Bcuda9.0_amd64.deb
wget .../libcudnn7-dev_7.0.5.15-1%2Bcuda9.0_amd64.deb
wget .../libcudnn7-doc_7.0.5.15-1%2Bcuda9.0_amd64.deb

sudo dpkg -i libcudnn7_7.0.5.15-1+cuda9.0_amd64.deb
sudo dpkg -i libcudnn7-dev_7.0.5.15-1+cuda9.0_amd64.deb
sudo dpkg -i libcudnn7-doc_7.0.5.15-1+cuda9.0_amd64.deb
```

## PyCUDA
```
sudo apt-get -y install python-pycuda 
sudo pip install -y pycuda
```

## TensorRT Developer
* Based on [THESE](http://developer2.download.nvidia.com/compute/machine-learning/tensorrt/secure/3.0/ga/TensorRT-Installation-Guide.pdf) instructions.
* This assumes Python3
```
wget .../nv-tensorrt-repo-ubuntu1604-ga-cuda9.0-trt3.0-20171128_1-1_amd64.deb

sudo dpkg -i nv-tensorrt-repo-ubuntu1604-ga-cuda9.0-trt3.0-20171128_1-1_amd64.deb
sudo apt-get update
sudo apt-get install tensorrt

sudo apt-get install python3-libnvinfer python3-libnvinfer-dev python3-libnvinfer-doc 
sudo apt-get install uff-converter-tf

sudo dpkg -l | grep TensorRT
```

## TensorFlow
```
pip install tensorflow-gpu
```
## PyCUDA
```
sudo apt-get --yes install python-pycuda

sudo apt-get -y install python-pycuda \

pip install pycuda
```

## TensorRT Examples
* Follow [THESE](https://github.com/parallel-forall/code-samples/blob/master/posts/TensorRT-3.0/convert.ipynb) instructions.

```
cd /usr/lib/python3.5/dist-packages/

python   <-- Inoke Python Shell

import tensorrt as trt
...

```

