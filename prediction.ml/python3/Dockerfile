FROM fluxcapacitor/package-java-openjdk-1.8:master

WORKDIR /root

RUN \
  apt-get update

# Install Python with conda into the default root virtual environment
RUN wget -q https://repo.continuum.io/miniconda/Miniconda3-4.3.11-Linux-x86_64.sh -O /tmp/miniconda.sh  && \
    echo '1924c8d9ec0abf09005aa03425e9ab1a */tmp/miniconda.sh' | md5sum -c - && \
    bash /tmp/miniconda.sh -f -b -p /opt/conda && \
    /opt/conda/bin/conda install --yes python=3.5 pip && \
    /opt/conda/bin/pip install --upgrade pip && \
    rm /tmp/miniconda.sh

ENV \
  PATH=/opt/conda/bin:$PATH

RUN \
  apt-get update

RUN \
  mkdir -p /root/logs

ENV LOGS_HOME=/root/logs

COPY src/ src/

ENV \
  PIO_MODEL_STORE_HOME=/root/model_store

ENV \
  PIO_MODEL_SERVER_PATH=src/main/python

ENV \
  PIO_MODEL_TYPE=python3

ENV \
  PIO_MODEL_NAMESPACE=default

ENV \
  PIO_MODEL_NAME=python3-zscore

ENV \
  PIO_MODEL_VERSION=v0

ENV \
  PIO_MODEL_SERVER_PORT="9876"

ENV \
  PIO_MODEL_SERVER_PROMETHEUS_PORT="8080"

RUN \
  git config --global http.sslverify false

RUN \
  git config --global http.sslCAinfo /etc/ssl/certs/ca-certificates.crt

RUN \
  git config --global alias.up '!git remote update -p; git merge --ff-only @{u}'

#RUN \
#  git clone -b master https://github.com/fluxcapacitor/source.ml.git

RUN \
  ln -s /root/volumes/source.ml/prediction.ml/model_store

COPY scripts/ scripts/

ENV \
  PATH=/root/scripts:$PATH

RUN \
  conda create --yes -n model_environment_python3 python=3.5

RUN \
  create_model_server_environment_python3

RUN \
  create_environment_python3

COPY run run

EXPOSE 9876 8080

CMD ["supervise", "."]
