FROM ubuntu:14.04 

WORKDIR /root

RUN \
  apt-get update \
  && apt-get install -y software-properties-common \
  && apt-get install -y daemontools \
  && apt-get install -y apt-transport-https \
  && add-apt-repository -y ppa:openjdk-r/ppa \
  && apt-get update \
  && apt-get install -y --no-install-recommends openjdk-8-jdk openjdk-8-jre-headless \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

ENV \
  JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/

# Required by TensorFlow Serving
RUN \
  apt-get update && sudo apt-get install -y \
        build-essential \
        curl \
        wget \
        libcurl3-dev \
        git \
        libfreetype6-dev \
        libpng12-dev \
        libzmq3-dev \
        pkg-config \
        software-properties-common \
        swig \
        zip \
        zlib1g-dev

# Install Python with conda
RUN wget -q https://repo.continuum.io/miniconda/Miniconda3-4.1.11-Linux-x86_64.sh -O /tmp/miniconda.sh  && \
    echo '874dbb0d3c7ec665adf7231bbb575ab2 */tmp/miniconda.sh' | md5sum -c - && \
    bash /tmp/miniconda.sh -f -b -p /opt/conda && \
    /opt/conda/bin/conda install --yes python=3.5 sqlalchemy tornado jinja2 traitlets requests pip && \
    /opt/conda/bin/pip install --upgrade pip && \
    rm /tmp/miniconda.sh

ENV \
  PATH=/opt/conda/bin:$PATH

RUN \
  conda install --yes openblas scikit-learn numpy scipy matplotlib pandas seaborn

RUN \
  conda install --yes ipykernel jupyter

RUN \
  pip install grpcio

ENV \
 BAZEL_VERSION=0.4.4 

RUN \
  mkdir /root/bazel \
  && cd /root/bazel \
  && curl -fSsL -O https://github.com/bazelbuild/bazel/releases/download/$BAZEL_VERSION/bazel-$BAZEL_VERSION-installer-linux-x86_64.sh \
  && curl -fSsL -o /root/bazel/LICENSE.txt https://raw.githubusercontent.com/bazelbuild/bazel/master/LICENSE.txt \
  && chmod +x bazel-*.sh \
  && ./bazel-$BAZEL_VERSION-installer-linux-x86_64.sh \
  && rm bazel-$BAZEL_VERSION-installer-linux-x86_64.sh

ENV \
 TENSORFLOW_SERVING_VERSION=0.5.1 

# TensorFlow Serving
RUN \
  git clone -b $TENSORFLOW_SERVING_VERSION --recurse-submodules https://github.com/tensorflow/serving.git

# TensorFlow Serving Home (not required on PATH)
ENV \
  TENSORFLOW_SERVING_HOME=/root/serving

ENV \
  TENSORFLOW_HOME=/root/serving/tensorflow

RUN \
  cd $TENSORFLOW_HOME \
  && printf "\n\n\n\n\ny\n\n\n\n" | ./configure

RUN \
  cd $TENSORFLOW_HOME \
  && bazel build --config=opt //tensorflow/tools/pip_package:build_pip_package 

RUN \
  cd $TENSORFLOW_HOME \
  && bazel-bin/tensorflow/tools/pip_package/build_pip_package /tmp/tensorflow_pkg

ENV \
 TENSORFLOW_VERSION=1.0.0

RUN \
  pip install /tmp/tensorflow_pkg/tensorflow-${TENSORFLOW_VERSION}-cp35-cp35m-linux_x86_64.whl 

# Utility for optimizing/simplifying models for inference
RUN \
  cd $TENSORFLOW_HOME \
  && bazel build tensorflow/python/tools:optimize_for_inference

RUN \
  cd $TENSORFLOW_SERVING_HOME \
  && bazel build -c opt tensorflow_serving/...

ENV \
  PATH=$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/model_servers/:$PATH

ENV \
  LOGS_HOME=/root/logs \
  DATA_HOME=/root/data \
  MODEL_HOME=/root/model \
  TENSORBOARD_SUMMARY_HOME=/root/tensorboard-summary \
  NOTEBOOKS_HOME=/root/notebooks

RUN \
  mkdir -p $LOGS_HOME \
  && mkdir -p $DATA_HOME \
  && mkdir -p $MODEL_HOME \
  && mkdir -p $TENSORBOARD_SUMMARY_HOME 

COPY data $DATA_HOME 
COPY models $MODELS_HOME 
COPY notebooks $NOTEBOOKS_HOME 
COPY config/jupyter_notebook_config.py /root/.jupyter/
COPY run run

EXPOSE 5678 6006 9000 9001 9002 9003 9004

CMD ["supervise", "."]
