
# Installation instructions for the TensorFlow/Cloud ML workshop

  - [Project and Cloud ML setup](#project-and-cloud-ml-setup)
  - [Docker-based installation](#docker-based-installation)
    - [Download the container image](#download-the-container-image)
    - [Create a directory to hold data files needed by the workshop](#create-a-directory-to-hold-data-files-needed-by-the-workshop)
    - [Run the container](#run-the-container)
    - [Authorize gcloud in the running container](#authorize-gcloud-in-the-running-container)
    - [Restarting the container later](#restarting-the-container-later)
    - [Running the Docker container on a VM](#running-the-docker-container-on-a-vm)
  - [Virtual environment-based installation](#virtual-environment-based-installation)
    - [Install Conda + Python 2.7 to use as your local virtual environment](#install-conda--python-27-to-use-as-your-local-virtual-environment)
    - [Install TensorFlow into a virtual environment](#install-tensorflow-into-a-virtual-environment)
    - [Install some Python packages](#install-some-python-packages)
    - [Install the Google Cloud SDK](#install-the-google-cloud-sdk)
    - [Cloud ML SDK installation (for 'transfer learning' preprocessing)](#cloud-ml-sdk-installation-for-transfer-learning-preprocessing)
  - [Set up some data files used in the examples](#set-up-some-data-files-used-in-the-examples)
    - [Transfer learning example](#transfer-learning-example)
  - [Optional: Clone/Download the TensorFlow repo from GitHub](#optional-clonedownload-the-tensorflow-repo-from-github)

* * *

You can set up for the workshop in two different, mutually-exclusive ways:

- [Running in a docker container](#docker-based-installation).
- [Installing the necessary packages into a virtual environment](#virtual-environment-based-installation).

You will need to do the [Cloud Project and Cloud ML setup](#project-and-cloud-ml-setup) in either case.

## Project and Cloud ML setup

Follow the instructions below to create a project, initialize it for Cloud ML, and set up a storage bucket to use for the workshop examples.

**Note: Skip the "Setting up your environment" section** of the linked-to page at this step. (If you run in a docker container, it is already done for you).

* [Setting Up Your GCP Project](https://cloud.google.com/ml/docs/how-tos/getting-set-up#setting_up_your_google_cloud_project )
* [Initializing Cloud ML for your project](https://cloud.google.com/ml/docs/how-tos/getting-set-up#initializing_your_product_name_short_project)
* [Setting up your Cloud Storage Bucket](https://cloud.google.com/ml/docs/how-tos/getting-set-up#setting_up_your_cloud_storage_bucket)

## Docker-based installation

We're providing a [Docker](https://www.docker.com/) container image with all the necessary libraries included, for you to download.

To use it, you'll need to have [Docker installed](https://docs.docker.com/engine/installation/).
To run some of the examples, you'll likely need to configure it with at least 4GB of memory.

If you like, you can start up a Google Compute Engine (GCE) VM with docker installed, and run the container there.  This is a good option if you don't want to install Docker locally. See the instructions at the end of this section for how to do that.

### Download the container image

Once Docker is installed and running, download the workshop image:

```sh
$ docker pull gcr.io/google-samples/tf-workshop:v5
```

[Here's the Dockerfile](https://github.com/amygdala/tensorflow-workshop/tree/master/workshop_image) used to build this image.

### Create a directory to hold data files needed by the workshop

Create a directory (called, say, `workshop-data`) to mount as a volume when you start up the docker container.  You can put data/output in this directory so that it is accessible both within and outside the running container.

### Run the container

Once you've downloaded the container image, you can run it like this:

```sh
docker run -v `pwd`/workshop-data:/root/tensorflow-workshop-master/workshop-data -it \
    -p 6006:6006 -p 8888:8888 -p 5000:5000 gcr.io/google-samples/tf-workshop:v5
```

Edit the path to the directory you're mounting as appropriate. The first component of the `-v` arg is the local directory, and the second component is where you want to mount it in your running container.

### Authorize gcloud in the running container

In the docker container, run the following commands, replacing `<your-project-name` with your project name.
For the second two, you will get a URL to paste in your browser, to obtain an auth code. In the browser, allow access, then copy the resulting code and paste it at the prompt in your running docker container.

```shell
gcloud config set project <your-project-name>
gcloud auth login
gcloud beta auth application-default login
```

### Restarting the container later

If you later exit your container and then want to restart it again, you can find the container ID by running:

```sh
$ docker ps -a
```

Then, run:

```sh
$ docker start <container_id>
```

(`docker ps` should then show it running). Once the workshop container is running again, you can exec back into it like this:

```sh
$ docker exec -it <container_id> bash
```

### Running the Docker container on a VM

It is easy to set up a Google Compute Engine (GCE) VM on which to run the Docker container. We sketch the steps below, or see [TLDR_CLOUD_INSTALL.md](TLDR_CLOUD_INSTALL.md) for more detail.

First, make sure that your project has the GCE API enabled. An easy way to do this is to go to the [Cloud Console](https://console.cloud.google.com/), and visit the Compute Engine panel.  It should display a button to enable the API.

- Connect to your project's [Cloud Shell](https://cloud.google.com/shell/).

- From the Cloud Shell, create a container-optimized image as described [here](https://cloud.google.com/container-optimized-os/docs/how-to/create-configure-instance). E.g.:

```
gcloud compute instances create mlworkshop \
    --image-family gci-stable \
    --image-project google-containers \
    --zone us-central1-b --boot-disk-size=100GB \
    --machine-type n1-standard-1
```

- Set up a default network firewall rule for: tcp:8888, tcp:6006, and tcp:5000.  (This opens the ports we'll use for jupyter, Tensorboard, and Flask.) E.g:

```shell
gcloud compute firewall-rules create mlworkshop --allow tcp:8888,tcp:6006,tcp:5000
```

- SSH to your image.  You can do this from the Cloud Console by visiting the Compute Engine panel, and clicking on the 'SSH' pulldown to the right of your instance.

Then, once you've ssh'd to the VM, follow the instructions above to download and run the Docker container there.
Note: **Docker is already installed** on the 'container-optimized' VMs.

When you're done with the workshop, you may want to *stop* or *delete* your VM instance.

You can also delete your firewall rule:

```shell
gcloud compute firewall-rules delete mlworkshop
```

* * *
## Virtual environment-based installation

(These steps are not necessary if you have already completed the instructions for running the Docker image.)

We highly recommend that you use a virtual environment for your TensorFlow installation rather than a direct install onto your machine.  The instructions below walk you through a `conda` install, but a `virtualenv` environment will work as well.

### Install Conda + Python 2.7 to use as your local virtual environment

Anaconda is a Python distribution that includes a large number of standard numeric and scientific computing packages. Anaconda uses a package manager called "conda" that has its own environment system similar to Virtualenv.

Follow the instructions [here](https://www.continuum.io/downloads).  The [miniconda version](http://conda.pydata.org/miniconda.html) should suffice.

### Install TensorFlow into a virtual environment

Follow the instructions [on the TensorFlow site](https://www.tensorflow.org/get_started/os_setup#anaconda_installation) to create a Conda environment with Python 2.7, *activate* it, and then install TensorFlow within it.

**Note**: Install TensorFlow version 0.12, by using the [**pip install steps**](https://www.tensorflow.org/get_started/os_setup#anaconda_installation#using_pip) for conda rather than conda-forge.

If you'd prefer to use virtualenv, see [these instructions](https://www.tensorflow.org/get_started/os_setup#virtualenv_installation) instead.

Remember to activate your environment in all the terminal windows you use during this workshop.

### Install some Python packages

With your conda environment activated, install the following packages:

```sh
$ conda install numpy
$ conda install scipy
$ pip install sklearn pillow
$ conda install matplotlib
$ conda install jupyter
```

(If you are using `virtualenv` instead of `conda`, install the packages using the equivalent `pip` commands instead).


### Install the Google Cloud SDK

Follow the installation instructions [here](https://cloud.google.com/sdk/downloads), then run:

```
gcloud components install beta
```

To get the `gcloud beta ml` commands.

### Cloud ML SDK installation (for 'transfer learning' preprocessing)

The Cloud ML SDK is needed to run the 'preprocessing' stage in the [Cloud ML transfer
learning](workshop_sections/transfer_learning/cloudml) example. It requires Python 2.7 to install. It's possible to
skip this part of setup for most of the exercises.

To install the SDK, follow the setup instructions
[on this page](https://cloud.google.com/ml/docs/how-tos/getting-set-up).
(Assuming you've followed the instructions above, you will have already done some of these steps. **Install TensorFlow version 0.12** as described in [this section](#install-tensorflow-into-a-virtual-environment), not 0.11)

**Note**: if you have issues with the pip install of `python-snappy`, and are running in a conda virtual environment, try `conda install python-snappy` instead.

You don't need to download the Cloud ML samples or docs for this workshop, though you may find it useful to grab them
anyway.


## Set up some data files used in the examples

### Transfer learning example

Because we have limited workshop time, we've saved a set of
[TFRecords]([TFRecords](https://www.tensorflow.org/api_docs/python/python_io/))
generated as part of the [Cloud ML transfer learning](workshop_sections/transfer_learning/cloudml)
example. To save time, copy them now to your own bucket as follows.

Set the `BUCKET` variable to point to your GCS bucket (replacing `your-bucket-name` with the actual name), then copy the records to your bucket.  Then, set the GCS_PATH variable to the newly copied subdir.

```shell
BUCKET=gs://your-bucket-name
gsutil cp -r gs://tf-ml-workshop/transfer_learning/hugs_preproc_tfrecords $BUCKET
GCS_PATH=$BUCKET/hugs_preproc_tfrecords
```


## Optional: Clone/Download the TensorFlow repo from GitHub

We'll be looking at some examples based on code in the tensorflow repo. While it's not necessary, you might want to clone or download it [here](https://github.com/tensorflow/tensorflow), or grab the latest release [here](https://github.com/tensorflow/tensorflow/releases).

