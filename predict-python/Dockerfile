FROM fluxcapacitor/package-ubuntu-16.04:master

ARG MODEL_TYPE
RUN \
  echo $MODEL_TYPE
ENV \
  PIPELINE_MODEL_TYPE=$MODEL_TYPE

ARG MODEL_NAME
RUN \
  echo $MODEL_NAME
ENV \
  PIPELINE_MODEL_NAME=$MODEL_NAME

ARG MODEL_VERSION
RUN \
  echo $MODEL_VERSION
ENV \
  PIPELINE_MODEL_VERSION=$MODEL_VERSION

ENV \
  APP_NAME=predict-$MODEL_TYPE-$MODEL_NAME-v$MODEL_VERSION
RUN \
  echo $APP_NAME

ENV \
  PATH=/opt/conda/bin:$PATH

ENV \
  PIPELINE_MODEL_SERVER_PATH=/usr/local/appuser/$APP_NAME/src/main/python/model_server

ENV \
  PIPELINE_MODEL_SERVER_PORT=9876

ENV \
  PIPELINE_MODEL_SERVER_PROMETHEUS_PORT=10254

ENV \
  CONTAINERPILOT=file:///usr/local/appuser/$APP_NAME/config/containerpilot/containerpilot.json


ENV \
  PIPELINE_MODEL_PATH=/usr/local/appuser/$APP_NAME/src/main/python/model_server/model

ENV \
  PIPELINE_CONDA_ENV_NAME=pipeline

ENV \
  SHELL=/bin/bash

ENV \
  PATH=/usr/local/appuser/$APP_NAME:$PATH

RUN rm /bin/sh && ln -s /bin/bash /bin/sh

RUN \
  mkdir -p $APP_NAME/logs

ENV \
  LOGS_HOME=/usr/local/appuser/$APP_NAME/logs

RUN \
  mkdir -p /usr/local/appuser/$APP_NAME

WORKDIR /usr/local/appuser/$APP_NAME

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
 && apt-get install -y nginx \
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

COPY sysutils/ sysutils/

COPY src/ src/

COPY html/ /var/www/html/

COPY model/model-store/$MODEL_TYPE/$MODEL_NAME/$MODEL_VERSION/ $PIPELINE_MODEL_PATH

RUN \
  mv /etc/nginx/sites-available/default /etc/nginx/sites-available/default.orig \
  && cd /etc/nginx/sites-available/ \
  && ln -s /usr/local/appuser/$APP_NAME/config/nginx/default \
  && cd /etc/nginx/sites-enabled/ \
  && rm default \
  && ln -s /etc/nginx/sites-available/default

RUN \
  echo "" \
  && echo "Creating and Activating '$PIPELINE_CONDA_ENV_NAME' Conda Environment with '$PIPELINE_MODEL_TYPE/$PIPELINE_MODEL_NAME' Model Dependencies with '$PIPELINE_MODEL_PATH/pipeline_conda_environment.yml'..." \
  && echo "" \
  && conda env create --name $PIPELINE_CONDA_ENV_NAME \
    --file $PIPELINE_MODEL_PATH/pipeline_conda_environment.yml \
  && echo "" \
  && echo "...Created and Activated!" \
  && echo ""

RUN \
  echo "" \
  && echo "Installing Model Server Dependencies..." \
  && echo "" \
  && conda env update --name $PIPELINE_CONDA_ENV_NAME \
    --file $PIPELINE_MODEL_SERVER_PATH/requirements/pipeline_model_server_conda_environment.yml \
  && echo "" \
  && echo "...Model Server Dependencies Installed!" \
  && echo ""

# Activate Conda Environment
RUN \
  source activate $PIPELINE_CONDA_ENV_NAME \
  && pip install psutil

RUN \
  source activate $PIPELINE_CONDA_ENV_NAME

RUN \
  conda list -n $PIPELINE_CONDA_ENV_NAME >/var/www/html/conda-list-$PIPELINE_CONDA_ENV_NAME.txt

COPY run run

EXPOSE 80 6969 9876 10254

CMD ["supervise", "."]
