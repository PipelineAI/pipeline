# Installing TensorFlow on Ubuntu

We will present the tutorial in [Jupyter](jupyter.org) notebooks.  To
run them on your machine, you will need a working TensorFlow
installation (v0.10).

Follow these instructions, which assume you have Ubuntu Linux.  Other
Linux distributions may not be supported.


## Clone this repository.

```
git clone https://github.com/wolffg/tf-tutorial.git
cd tf-tutorial
```

## Install pip and dependencies

```
sudo apt-get install python-pip python-dev python-matplotlib
```

## Install TensorFlow

This uses CPU only.

```
export TF_BINARY_URL=https://storage.googleapis.com/tensorflow/linux/cpu/tensorflow-0.10.0-cp27-none-linux_x86_64.whl
sudo pip install --upgrade $TF_BINARY_URL
sudo pip install jupyter Pillow
```

For GPU instructions, see [tensorflow.org](https://www.tensorflow.org/versions/r0.10/get_started/os_setup.html).

## Running Jupyter

From your "tensorflow" virtualenv prompt, run the following:

```
jupyter notebook
```

Click on `0_tf_hello_world.ipynb` to test that jupyter is running
correctly.

You should be able to run the notebook without issue.

<hr>
