
# Installing TensorFlow on Docker on a Mac.

This tutorial is presented via [Jupyter](http://jupyter.org) notebooks.  To
run them on your machine, you will need a working TensorFlow
installation (v0.10).

Below are instructions on how to set up a TensorFlow environment using
Docker.  Although Docker runs in a VM, the advantage is that Docker
images come with all dependencies are pre-installed and pre-compiled.

*Note: Docker for Mac requires OS X 10.10.3 Yosemite or newer running on a 2010 or newer Mac.*

## Clone this repository

Using git, clone this tutorial and enter that directory.

```
cd $HOME
git clone https://github.com/wolffg/tf-tutorial.git
cd tf-tutorial
```

## Set up Docker

Docker runs your notebooks from a virtual machine.  Docker images
already contain installed and compiled versions of TensorFlow.

Prerequisites:
* A Mac machine (Docker will work on Windows, but the step-by-step instructions here may not work perfectly since we have not tried them)
* Some knowledge of Python

If you already have the Docker Toolbox installed, skip to
"Installing/running a TensorFlow Docker Image." Otherwise, go to
[https://docs.docker.com/docker-for-mac/](https://docs.docker.com/docker-for-mac/) and follow the
instructions there, which should roughly be:

Download Docker for Mac. 
* On the Toolbox page, find the Stable channel.
* Download Docker.dmg.
* Drag the icon into your Applications folder
* Run the Docker for Mac app.
  * It will ask for root permissions to start with; allow it to do so.
  * Wait until the dialog says "Docker is now up and running!".
* Run a docker command in the terminal to confirm Docker
installation has worked:
```
docker run hello-world
```

#### Installing and Running the TensorFlow Image

On OS X, if you have not already, run the Docker for Mac app,
usually placed in `/Applications/`, and which looks like this:

![Docker For Mac Icon](images/docker-for-mac.png)

There may be a long pause as the Docker service starts.

If you click on the whale icon in your toolbar, you should eventually
see a green light and "Docker is running".

## Run Docker with Jupyter

Go to where you cloned the repository (we're assuming `$HOME`):

```
cd $HOME/tf-tutorial
docker run  -v `pwd`:/tutorial -p 0.0.0.0:6006:6006 -p 0.0.0.0:8888:8888 \
   -it tensorflow/tensorflow:0.10.0rc0 bash
```

This will start a Docker instance with the tutorial materials mounted
at `/tutorial`.

*(Note: All further commands are run in the Docker
image, so your prompt will be `root@[something]#`).*

If you want to do the DeepDream codelab, you will also need to install Pillow within the Docker instance:

```
pip install Pillow
```

Once started, run the Jupyter server in the right directory.

```
cd /tutorial
jupyter notebook &
```


**On OSX:** You can navigate to:

* [http://localhost:8888](http://localhost:8888)

## When you're done

To exit Docker, you can simply enter `exit` or hit `Ctrl-D`.
