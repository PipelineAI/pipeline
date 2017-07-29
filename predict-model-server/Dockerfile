FROM dl2.fluxcapacitorcorp.com/ha-docker/minipy3nginx:master

ENV \
  PATH=/opt/conda/bin:$PATH

ENV \
  PIPELINE_MODEL_SERVER_PATH=/usr/local/appuser/predict-model-server/src/main/python/model_server

ENV \
  PIPELINE_MODEL_SERVER_PORT=9876

ENV \
  PIPELINE_MODEL_SERVER_PROMETHEUS_PORT=10254

ENV \
  CONTAINERPILOT=file:///usr/local/appuser/predict-model-server/config/containerpilot/containerpilot.json

ENV \
  PIPELINE_MODEL_PATH=/usr/local/appuser/predict-model-server/src/main/python/model_server/model

ENV \
  PIPELINE_CONDA_ENV_NAME=pipeline

ENV \
  SHELL=/bin/bash

ENV \
  PATH=/usr/local/appuser/predict-model-server:$PATH

RUN \
  mkdir -p /usr/local/appuser/predict-model-server

WORKDIR /usr/local/appuser/predict-model-server

RUN \
 apt-get update \
 && apt-get install -y wget \
 && apt-get install -y apt-utils \
 && apt-get install -y software-properties-common \
 && apt-get install -y python-software-properties \
 && apt-get install -y curl \
 && apt-get install -y wget \
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

RUN rm /bin/sh && ln -s /bin/bash /bin/sh

RUN \
  mkdir -p predict-model-server/logs

ENV \
  LOGS_HOME=/usr/local/appuser/predict-model-server/logs

COPY config/ config/

COPY sysutils/ sysutils/

COPY src/ src/

COPY html/ /var/www/html/

RUN \
  mv /etc/nginx/sites-available/default /etc/nginx/sites-available/default.orig \
  && cd /etc/nginx/sites-available/ \
  && ln -s /usr/local/appuser/predict-model-server/config/nginx/default \
  && cd /etc/nginx/sites-enabled/ \
  && rm default \
  && ln -s /etc/nginx/sites-available/default

COPY run run

EXPOSE 80 6969 9876 10254

CMD ["supervise", "."]
