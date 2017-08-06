FROM fluxcapacitor/package-ubuntu-16.04:master

# !!!!!!!!!!!!!!!!!! REQUIREMENTS !!!!!!!!!!!!!!!!!!
#   * S3 does not support underscores "_" in bucket names
#   * Python does not support dashes "-" in package directory paths

# Set Environment Variables
# Container Path is the base service workdir inside the container that contains all application files (REST API and Model)
# and is used to find the "logs" directory for splunk forwarding
ENV \
  CONTAINER_PATH=/root/predict_model_server

WORKDIR $CONTAINER_PATH

# Tornado REST API path
ENV \
  PIPELINE_MODEL_SERVER_PATH=$CONTAINER_PATH/model_server

# Tornado REST API port
ENV \
  PIPELINE_MODEL_SERVER_PORT=80

# Metrics monitoring port
ENV \
  PIPELINE_MODEL_SERVER_PROMETHEUS_PORT=10254

# Directory containing the Data Science Model
ENV \
  PIPELINE_MODEL_PATH=$PIPELINE_MODEL_SERVER_PATH/model

ENV \
  PIPELINE_CONDA_ENV_NAME=pipeline

ENV \
  SHELL=/bin/bash

# Directory
ENV \
  LOGS_HOME=$CONTAINER_PATH/logs

# Update PATH to include Conda and ContainerPath
ENV \
  PATH=/opt/conda/bin:$PATH

ENV \
  PATH=$CONTAINER_PATH:$PATH

ENV \
  PATH=$PIPELINE_MODEL_SERVER_PATH:$PATH

# set bash as the standard shell
RUN rm /bin/sh && ln -s /bin/bash /bin/sh

# Create directory for the application
RUN \
  mkdir -p $CONTAINER_PATH

# Create directory for logs
RUN \
  mkdir -p logs

# wget/curl/zip - used for installations
# daemontools - used in child containers to run SUPERVISE to restart the Python Tornado REST API if the process stops
RUN \
 apt-get update \
 && apt-get install -y wget \
 && apt-get install -y apt-utils \
 && apt-get install -y software-properties-common \
 && apt-get install -y python-software-properties \
 && apt-get install -y curl \
 && apt-get install -y vim \
 && apt-get install -y git \
 && apt-get install -y zip \
 && apt-get install -y python-dev \
 && apt-get install -y python-pip \
 && apt-get install -y daemontools \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

# Install Anaconda with Python3
RUN wget -q https://repo.continuum.io/miniconda/Miniconda3-4.3.21-Linux-x86_64.sh -O /tmp/miniconda.sh  && \
    echo 'c1c15d3baba15bf50293ae963abef853 */tmp/miniconda.sh' | md5sum -c - && \
    bash /tmp/miniconda.sh -f -b -p /opt/conda && \
    /opt/conda/bin/conda install --yes python=3.6 pip && \
    /opt/conda/bin/pip install --upgrade pip && \
    rm /tmp/miniconda.sh

COPY config/ config/

# sysutils is used to limit and monitor jvm and container memory CONTAINER_MAX_MEMORY and cpu CONTAINER_CORE_LIMIT
COPY sysutils/ sysutils/

COPY src/main/python/model_server/ $PIPELINE_MODEL_SERVER_PATH/

# Copy the bash script that executes model training and starts the Tornado REST API server into the container
COPY run run

# Expose port 80 for the Tornado REST API and port 10254 for metrics monitoring
EXPOSE 80 10254
