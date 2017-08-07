FROM fluxcapacitor/package-tensorflow-16d39e9-d690fdd-serving-cpu:master

ENV \
  PIPELINE_MODEL_CHIP=cpu

RUN rm /bin/sh && ln -s /bin/bash /bin/sh

WORKDIR /root

RUN \
  apt-get update

# Create an app user so our program doesn't run as root.
RUN groupadd -r appuser \
  && useradd -r -g appuser -d /root -p password -c "app user" appuser \
  && usermod -aG sudo appuser

RUN \
  apt-get install -y sudo

RUN \
  echo "appuser ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers \
  && cat /etc/sudoers

RUN \
  chmod a+wrx /root

RUN \
  echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list \
  && apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 \
  && apt-get update \
  && apt-get install -y sbt

# Anaconda's libgfortran=3.0 is not co-operating, so we use apt-get
RUN \
  apt-get install libgfortran3
RUN \
  conda install -c anaconda openblas

RUN \
  apt-get install -y nginx 

RUN \
  service nginx start

RUN \
  apt-get install -y tmux \
  && apt-get install -y screen

RUN \
  wget https://github.com/prometheus/prometheus/releases/download/v1.7.1/prometheus-1.7.1.linux-amd64.tar.gz \
  && tar xvfz prometheus-1.7.1.linux-amd64.tar.gz \
  && rm prometheus-1.7.1.linux-amd64.tar.gz

RUN \
  wget https://s3-us-west-2.amazonaws.com/grafana-releases/release/grafana_4.3.1_amd64.deb \
  && apt-get install -y adduser libfontconfig \
  && dpkg -i grafana_4.3.1_amd64.deb \
  && rm grafana_4.3.1_amd64.deb

RUN \
  service grafana-server start

#RUN \
#  mv /etc/grafana/grafana.ini /etc/grafana/grafana.ini.orig \
#  && cd /etc/grafana/ \
#  && ln -s /root/config/grafana/grafana.ini

RUN \
  mkdir -p /root/logs

ENV \
  LOGS_HOME=/root/logs

COPY sysutils/ sysutils/

ENV \
  PIPELINE_JVM_MODEL_SERVER_PATH=/root/jvm
COPY jvm/ jvm/
RUN \
  cd $PIPELINE_JVM_MODEL_SERVER_PATH && sbt clean-files package
RUN \
  cd $PIPELINE_JVM_MODEL_SERVER_PATH/lib/jni \
  && ln -s libtensorflow_jni-$PIPELINE_MODEL_CHIP.so libtensorflow_jni.so

COPY config/ config/

RUN \
  mv /root/prometheus-1.7.1.linux-amd64/prometheus.yml /root/prometheus-1.7.1.linux-amd64/prometheus.yml.orig \
  && cd /root/prometheus-1.7.1.linux-amd64/ \
  && ln -s /root/config/prometheus/prometheus.yml

ENV \
  PATH=/root/prometheus-1.7.1.linux-amd64/:$PATH
RUN \
  mv /etc/nginx/sites-available/default /etc/nginx/sites-available/default.orig \
  && cd /etc/nginx/sites-available/ \
  && ln -s /root/config/nginx/default \
  && cd /etc/nginx/sites-enabled/ \
  && rm default \
  && ln -s /etc/nginx/sites-available/default

ENV \
  CONFLUENT_VERSION=3.3.0 \
  CONFLUENT_MAJOR_VERSION=3.3

ENV \
  CONFLUENT_HOME=/root/confluent-${CONFLUENT_VERSION}

ENV \
  PATH=$CONFLUENT_HOME/bin:$PATH

RUN \
 wget http://packages.confluent.io/archive/${CONFLUENT_MAJOR_VERSION}/confluent-oss-${CONFLUENT_VERSION}-2.11.tar.gz \
 && tar xvzf confluent-oss-${CONFLUENT_VERSION}-2.11.tar.gz \
 && rm confluent-oss-${CONFLUENT_VERSION}-2.11.tar.gz

RUN \
  git clone https://github.com/edenhill/librdkafka.git \
  && cd librdkafka \
  && ./configure \
  && make \
  && make install

RUN \
  pip install --upgrade confluent-kafka

COPY src/ src/

ENV \
  PIPELINE_MODEL_SERVER_PATH=/root/src/main/python/model_server

RUN \
  apt-get install -y erlang erlang-ssl erlang-inets erlang-dev \
    erlang-edoc erlang-eunit erlang-tools erlang-common-test

RUN \
  apt-get install --yes sqlite3 libsqlite3-dev

RUN \
  apt-get install --yes nodejs-legacy npm

RUN \
  echo '{ "allow_root": true }' > /root/.bowerrc \
  && npm install -g bower

COPY guild/ guild/
RUN \
  cd guild \
  && make
RUN \
  ln -s /root/guild/scripts/guild-dev /usr/local/bin/guild

ENV \
  PIPELINE_MODEL_SERVER_PORT=9876

# Note:  Port 9040 is used in 2 separate contexts:
#        1) separate http server for prometheus running within the python-based model server
#        2) the same port as the main JVM-based model server
ENV \
  PIPELINE_MODEL_SERVER_PROMETHEUS_PORT=9040

ENV \
  PIPELINE_MODEL_SERVER_TENSORFLOW_SERVING_PORT=9000

ENV \
  PIPELINE_MODEL_SERVER_TENSORFLOW_SERVING_REQUEST_BATCHING=true

ENV \
  TF_CPP_MIN_LOG_LEVEL=0

COPY html/ /var/www/html/
COPY run run

ARG model_type
RUN \
  echo $model_type
ENV \
  PIPELINE_MODEL_TYPE=$model_type

ARG model_name
RUN \
  echo $model_name
ENV \
  PIPELINE_MODEL_NAME=$model_name

ARG model_tag
RUN \
  echo $model_tag

ARG model_path
RUN \
  echo $model_path
ENV \
  PIPELINE_MODEL_PATH=/root/model

RUN \
  pip install awscli

COPY $model_path $PIPELINE_MODEL_PATH
#RUN \
#  if [[ $model_path == "http:"* ]] || [[ $model_path == "https:"* ]]; then \
#    wget -r -e --no-parent -O $PIPELINE_MODEL_PATH $model_path; \
#  fi 

#RUN \
#  if [[ $model_path == "s3:"* ]]; then \
#    aws s3 cp --no-sign-request --recursive \
#      $model_path \
#      $PIPELINE_MODEL_PATH; \
#  fi 

ENV \
  PIPELINE_CONDA_ENV_NAME=pipeline

RUN \
  if [[ $PIPELINE_MODEL_TYPE == "python" ]] || \
     [[ $PIPELINE_MODEL_TYPE == "keras" ]] || \
     [[ $PIPELINE_MODEL_TYPE == "scikit" ]] || \
     [[ $PIPELINE_MODEL_TYPE == "tensorflow" ]]; then \
       echo "" \
       && echo "Creating and Activating '$PIPELINE_CONDA_ENV_NAME' Conda Environment with '$PIPELINE_MODEL_TYPE/$PIPELINE_MODEL_NAME' Model Dependencies with '$PIPELINE_MODEL_PATH/pipeline_conda_environment.yml'..." \
       && echo "" \
       && conda env create --name $PIPELINE_CONDA_ENV_NAME \
           --file $PIPELINE_MODEL_PATH/pipeline_conda_environment.yml \
       && echo "" \
       && echo "...Created and Activated!" \
       && echo "" \
       && echo "" \
       && echo "Installing Model Server Dependencies..." \
       && echo "" \
       && conda env update --name $PIPELINE_CONDA_ENV_NAME \ 
           --file $PIPELINE_MODEL_SERVER_PATH/requirements/pipeline_model_server_conda_environment.yml \
       && echo "" \
       && echo "source activate $PIPELINE_CONDA_ENV_NAME" >> ~/.bashrc \
       && echo "...Model Server Dependencies Installed!" \
       && echo ""; \
  fi

# TODO:  Include this in the above if statement 
RUN \
  if [ -e "$PIPELINE_MODEL_PATH/pipeline_setup.sh" ]; then \  
     CONDA_ENV_ROOT_PATH=$(conda info --root) \ 
       && echo "Setting Up Conda Environment with '$PIPELINE_MODEL_PATH/pipeline_setup.sh'..." \
       && conda info --root \
       && conda env list \
       && echo "" \
       && source activate $PIPELINE_CONDA_ENV_NAME \ 
       && echo "Running '$PIPELINE_MODEL_PATH/pipeline_setup.sh':\n" \
       && chmod a+x $PIPELINE_MODEL_PATH/pipeline_setup.sh \   
       && $PIPELINE_MODEL_PATH/pipeline_setup.sh \
       && export \
       && echo "...Conda Environment Successfully Setup at '$CONDA_ENV_ROOT_PATH/PIPELINE_CONDA_ENV_NAME/etc/conda/activate.d/pipeline_conda_environment_setup.sh'!" \
       && echo ""; \
  fi 

# Guild/TensorFlow Requirements
RUN \
  if [[ $PIPELINE_MODEL_TYPE == "tensorflow" ]]; then \
      source activate $PIPELINE_CONDA_ENV_NAME \
      && pip install psutil \
      && source activate $PIPELINE_CONDA_ENV_NAME \
      && pip2 install tensorflow==1.3.0rc2 \
      && conda list -n $PIPELINE_CONDA_ENV_NAME; \
  fi

ENV SHELL=/bin/bash
RUN \
  chmod a+wr -R /opt/conda/lib/python3.6/site-packages \
  && chmod a+wr -R /root/.cache/pip

RUN \
  chmod a+wr -R /root/model \
  && chmod a+wr -R /root/.ivy2 \
  && chmod a+wr -R /root/.sbt

USER appuser

# TODO:  Clean up this logic
# TODO:  The pipeline_train.py file is expected to place the saved model in the right directory
# TODO:  If using TensorFlow Serving, ensure that $PIPELINE_MODEL_PATH/versions directory exists after training
RUN \
  if [[ $PIPELINE_MODEL_TYPE == "tensorflow" ]] \
     && [ -f "$PIPELINE_MODEL_PATH/pipeline_train.sh" ] \ 
     && [ ! -f "$PIPELINE_MODEL_PATH/Guild" ]; then \
       echo "PIPELINE_MODEL_PATH=$PIPELINE_MODEL_PATH" \
       && echo "PIPELINE_MODEL_TYPE=$PIPELINE_MODEL_TYPE" \
       && echo "PIPELINE_MODEL_NAME=$PIPELINE_MODEL_NAME" \
       && echo "PIPELINE_MODEL_CHIP=$PIPELINE_MODEL_CHIP" \
       && echo "PIPELINE_MODEL_PATH=$PIPELINE_MODEL_PATH" \
       && echo "" \
       && echo "Starting TensorFlow-based Model Training..." \
       && echo "" \
       && source activate $PIPELINE_CONDA_ENV_NAME \
       && cd $PIPELINE_MODEL_PATH \
       && python pipeline_train.py \
       && cd /root \
       && echo "" \
       && echo "...Training Complete!" \
       && echo ""; \
  fi

# TODO:  Clean up this logic
# TODO:  The pipeline_train.py file is expected to place the saved model in the right directory
#       && ln -s /root/model/runs/model/versions 
RUN \
  if [[ $PIPELINE_MODEL_TYPE == "tensorflow" ]] \
     && [ -f "$PIPELINE_MODEL_PATH/pipeline_train.py" ] \
     && [ -f "$PIPELINE_MODEL_PATH/Guild" ]; then \
       echo "PIPELINE_MODEL_PATH=$PIPELINE_MODEL_PATH" \
       && echo "PIPELINE_MODEL_TYPE=$PIPELINE_MODEL_TYPE" \
       && echo "PIPELINE_MODEL_NAME=$PIPELINE_MODEL_NAME" \
       && echo "PIPELINE_MODEL_CHIP=$PIPELINE_MODEL_CHIP" \
       && echo "PIPELINE_MODEL_PATH=$PIPELINE_MODEL_PATH" \
       && echo "" \
       && echo "Starting TensorFlow-based Model Training..." \
       && echo "" \
       && source activate $PIPELINE_CONDA_ENV_NAME \
       && cd $PIPELINE_MODEL_PATH \
       && guild prepare \
       && guild train --flag=learning_rate=0.05 \
       && guild train --flag=learning_rate=0.025 \
       && cd /root \
       && echo "" \
       && echo "...Training Complete!" \
       && echo ""; \
  fi

RUN \
  if [[ $PIPELINE_MODEL_TYPE == "python" ]] \
     && [ -f "$PIPELINE_MODEL_PATH/pipeline_train.py" ]; then \
       echo "PIPELINE_MODEL_PATH=$PIPELINE_MODEL_PATH" \
       && echo "PIPELINE_MODEL_TYPE=$PIPELINE_MODEL_TYPE" \
       && echo "PIPELINE_MODEL_NAME=$PIPELINE_MODEL_NAME" \
       && echo "PIPELINE_MODEL_CHIP=$PIPELINE_MODEL_CHIP" \
       && echo "PIPELINE_MODEL_PATH=$PIPELINE_MODEL_PATH" \
       && echo "" \
       && echo "Starting Python-based Model Training..." \
       && echo "" \
       && source activate $PIPELINE_CONDA_ENV_NAME \
       && cd $PIPELINE_MODEL_PATH \
       && python pipeline_train.py \
       && cd /root \
       && echo "" \
       && echo "...Training Complete!" \
       && echo ""; \
  fi

EXPOSE 6969 9876 9000 9040 9090 3000 6333 9092 8082 8081

CMD ["supervise", "."]
