FROM pipelineai/ubuntu-16.04-gpu:1.5.0

WORKDIR /root

# MXNET
RUN \
  add-apt-repository -y ppa:certbot/certbot \
  && apt-get update \
  && apt-get install --no-install-recommends -y \
    libatlas-base-dev \
    libopencv-dev \
    graphviz \
    protobuf-compiler \
    libprotoc-dev \
  && pip install gevent gunicorn mxnet-model-server \
  && pip install --no-cache-dir mxnet-cu90 \
  && mkdir /root/mxnet_model_server \
  && rm -rf /var/lib/apt/lists/* \
  && apt-get clean

COPY mxnet/wsgi.py mxnet/setup_mms.py mxnet/mxnet-model-server.sh /root/mxnet_model_server/
# TODO: 
#COPY mxnet/mms_app_gpu.conf /root/ml/model/

ENV \
  PATH="/root/mxnet_model_server:${PATH}" \
  MXNET_MODEL_SERVER_GPU_IMAGE=1 \
  gpu_id=0

#RUN \
#  echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list \
#  && apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 \
#  && apt-get update \
#  && apt-get install --no-install-recommends -y --allow-unauthenticated sbt=1.0.2 \
#  && rm -rf /var/lib/apt/lists/* \
#  && apt-get clean

COPY config/prometheus/ config/prometheus/
ENV \
  PROMETHEUS_VERSION=2.3.2

RUN \
  wget https://github.com/prometheus/prometheus/releases/download/v$PROMETHEUS_VERSION/prometheus-$PROMETHEUS_VERSION.linux-amd64.tar.gz \
  && tar xvfz prometheus-$PROMETHEUS_VERSION.linux-amd64.tar.gz \
  && rm prometheus-$PROMETHEUS_VERSION.linux-amd64.tar.gz

ENV \
  PATH=/root/prometheus-$PROMETHEUS_VERSION.linux-amd64/:$PATH

ENV \
  GRAFANA_VERSION=5.2.2

RUN \
  wget https://s3-us-west-2.amazonaws.com/grafana-releases/release/grafana-$GRAFANA_VERSION.linux-amd64.tar.gz \ 
  && tar -zxvf grafana-$GRAFANA_VERSION.linux-amd64.tar.gz \
  && rm grafana-$GRAFANA_VERSION.linux-amd64.tar.gz

ENV \
  PATH=/root/grafana-$GRAFANA_VERSION/bin:$PATH 

COPY config/grafana/ config/grafana/
RUN \
  cd /root/grafana-$GRAFANA_VERSION/conf \
  && ln -s /root/config/grafana/grafana.ini \
  && ln -s /root/config/grafana/dashboards \
  && ln -s /root/config/grafana/datasources.yaml \
  && ln -s /root/config/grafana/dashboards.yaml

RUN \
  mkdir -p /root/logs

ENV \
  LOGS_HOME=/root/logs

COPY sysutils/ sysutils/

ENV \
  CONFLUENT_VERSION=5.0.0 \
  CONFLUENT_MAJOR_VERSION=5.0

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

#RUN \
#  pip install git+https://github.com/wintoncode/winton-kafka-streams.git

RUN \
  apt-get update \
  && apt-get install --no-install-recommends -y nginx \
  && rm -rf /var/lib/apt/lists/* \
  && apt-get clean

COPY config/nginx/ config/nginx/

RUN \
  mv /etc/nginx/sites-available/default /etc/nginx/sites-available/default.orig \
  && cd /etc/nginx/sites-available/ \
  && ln -s /root/config/nginx/default \
  && cd /etc/nginx/sites-enabled/ \
  && rm default \
  && ln -s /etc/nginx/sites-available/default

# forward request and error logs to docker log collector
#RUN \
#  ln -sf /dev/stdout /var/log/nginx/access.log \
#  && ln -sf /dev/stderr /var/log/nginx/error.log

RUN \
  service nginx start

# Must run ths or you will see the following error:
#   ImportError: librdkafka.so.1: cannot open shared object file: No such file or directory
RUN \
  ldconfig

ENV \
  PIPELINE_RESOURCE_SERVER_PATH=/root/src/main/python/model_server

ENV \
  PIPELINE_RESOURCE_SERVER_PORT=9876

ENV \
  PIPELINE_RESOURCE_SERVER_TENSORFLOW_SERVING_PORT=9000

ENV \
  PIPELINE_RESOURCE_SERVER_TENSORFLOW_SERVING_REQUEST_BATCHING=true

# https://github.com/tensorflow/serving/issues/819
RUN \
  add-apt-repository ppa:ubuntu-toolchain-r/test -y \
  && apt-get update \
  && apt-get install --no-install-recommends -y libstdc++6 \
  && rm -rf /var/lib/apt/lists/* \
  && apt-get clean

RUN \
  wget https://s3.amazonaws.com/fluxcapacitor.com/packages/tensorflow_model_server.gpu \
  && mv tensorflow_model_server.gpu /usr/local/bin/tensorflow_model_server \
  && chmod a+x /usr/local/bin/tensorflow_model_server

ENV \
  PIPELINE_HYSTRIX_DASHBOARD_PORT=7979

COPY dashboard/jetty-0.4.7.RC0.jar dashboard/jetty-0.4.7.RC0.jar
COPY dashboard/hystrix-dashboard-0.1.0-dev.0.uncommitted.war dashboard/hystrix-dashboard-0.1.0-dev.0.uncommitted.war
COPY html/ html/
COPY src/ src/

RUN \
  cp -R html/* /var/www/html/

COPY config/kafka/ config/kafka/

ENV \
  LD_LIBRARY_PATH=${LD_LIBRARY_PATH:+${LD_LIBRARY_PATH}:}/usr/lib/x86_64-linux-gnu:/usr/local/cuda/lib64:/usr/local/cuda-9.0/lib64:/usr/local/cuda/extras/CUPTI/lib64

RUN \
  ldconfig /usr/lib/x86_64-linux-gnu \
  && ldconfig /usr/local/cuda/lib64 \
  && ldconfig /usr/local/cuda-9.0/lib64 \
  && ldconfig /usr/local/cuda/extras/CUPTI/lib64 

ENV \
  PATH=/usr/local/cuda/bin:$PATH

# TensorRT 
RUN \
  wget https://developer.download.nvidia.com/compute/machine-learning/repos/ubuntu1404/x86_64/nvinfer-runtime-trt-repo-ubuntu1404-3.0.4-ga-cuda9.0_1.0-1_amd64.deb \
  && dpkg -i nvinfer-runtime-trt-repo-ubuntu1404-3.0.4-ga-cuda9.0_1.0-1_amd64.deb \
  && apt-get update \
  && apt-get install --no-install-recommends -y \
       libnvinfer-dev \
       libcudnn7-dev=7.0.5.15-1+cuda9.0 \
       libcudnn7=7.0.5.15-1+cuda9.0

COPY jvm/ jvm/

ENV \
  PIPELINE_JVM_MODEL_SERVER_PATH=/root/jvm

#####################
# Setup OpenJDK 1.11
#####################
RUN \
  apt-get update \
  && apt-get install -y software-properties-common \
  && add-apt-repository -y ppa:openjdk-r/ppa \
  && apt-get update \
  && apt-get install -y --no-install-recommends openjdk-11-jdk openjdk-11-jre-headless \
  && apt-get install -y apt-transport-https \
  && apt-get install -y wget \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

ENV \
  JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/

RUN \
  echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list \
  && apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 \
  && apt-get update \
  && apt-get install --no-install-recommends -y --allow-unauthenticated sbt=1.2.7 \
  && rm -rf /var/lib/apt/lists/* \
  && apt-get clean

RUN \
  cd $PIPELINE_JVM_MODEL_SERVER_PATH \
  && ./build.sh

# Don't forget to update the pipeline cli if these ports change!
EXPOSE \
  8080 \
  9090 \
  3000 

COPY run run

ENTRYPOINT ["./run"]
