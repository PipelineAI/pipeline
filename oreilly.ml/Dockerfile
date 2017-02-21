FROM ubuntu:14.04 

WORKDIR /root

ENV \
 BAZEL_VERSION=0.4.4 \
 TENSORFLOW_SERVING_VERSION=0.5.1 \
 TENSORFLOW_VERSION=1.0.0

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

#RUN \
#  echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list \
#  && sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 \
#  && sudo apt-get update \
#  && sudo apt-get install -y sbt

#RUN \
#  sbt clean clean-files

#RUN \
#  wget http://apache.mirrors.tds.net/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz \
#  && tar -xvzf apache-maven-3.3.9-bin.tar.gz \
#  && rm apache-maven-3.3.9-bin.tar.gz

#RUN \
#  export MAVEN_HOME=/root/apache-maven-3.3.9/

#RUN \
#  export PATH=$MAVEN_HOME/bin:$PATH

# TensorFlow Serving Home (not required on PATH)
ENV \
  TENSORFLOW_SERVING_HOME=/root/serving

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
#        python-dev \
#        python-numpy \
#        python-pip \
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

RUN \
  echo "build --spawn_strategy=standalone --genrule_strategy=standalone" >>/root/.bazelrc

ENV \
  BAZELRC=/root/.bazelrc

RUN \
  mkdir /root/bazel \
  && cd /root/bazel \
  && curl -fSsL -O https://github.com/bazelbuild/bazel/releases/download/$BAZEL_VERSION/bazel-$BAZEL_VERSION-installer-linux-x86_64.sh \
  && curl -fSsL -o /root/bazel/LICENSE.txt https://raw.githubusercontent.com/bazelbuild/bazel/master/LICENSE.txt \
  && chmod +x bazel-*.sh \
  && ./bazel-$BAZEL_VERSION-installer-linux-x86_64.sh \
  && rm bazel-$BAZEL_VERSION-installer-linux-x86_64.sh

# TensorFlow Serving
RUN \
 cd ~ \
 && git clone -b $TENSORFLOW_SERVING_VERSION --recurse-submodules https://github.com/tensorflow/serving.git

RUN \
  cd ~/serving/tensorflow \
  && printf "\n\n\n\n\ny\n\n\n\n" | ./configure

RUN \
  cd ~/serving \
  && bazel build -c opt tensorflow_serving/...

ENV \
  PATH=$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/model_servers/:$PATH

# Utility for optimizing/simplifying models for inference
#RUN \
#  cd ~/serving/tensorflow \
#  && bazel build tensorflow/python/tools:optimize_for_inference

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

# TensorFlow Serving
RUN \
 cd ~ \
 && git clone -b $TENSORFLOW_SERVING_VERSION --recurse-submodules https://github.com/tensorflow/serving.git

RUN \
  cd ~/serving/tensorflow \
  && printf "\n\n\n\n\ny\n\n\n\n" | ./configure

RUN \
  cd ~/serving \
  && bazel build -c opt tensorflow_serving/...

ENV \
  PATH=$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/model_servers/:$PATH

#RUN \
#  pip install --ignore-installed --upgrade pip setuptools \
#  && pip install --upgrade tensorflow==$TENSORFLOW_VERSION

RUN \
  git clone -b v$TENSORFLOW_VERSION https://github.com/tensorflow/tensorflow 

RUN \
  cd ~/tensorflow \
  && printf "\n\n\n\n\ny\n\n\n\n" | ./configure

RUN \
  bazel build --config=opt //tensorflow/tools/pip_package:build_pip_package

RUN \
  mkdir -p /root/logs

ENV \
  LOGS_HOME=/root/logs \
  DATA_HOME=/root/data \
  MODEL_HOME=/root/model \
  TENSORBOARD_HOME=/root/tensorboard \
  NOTEBOOKS_HOME=/root/notebooks

RUN \
  mkdir -p $TENSORBOARD_HOME

COPY data $DATA_HOME 
COPY model $MODEL_HOME 
COPY notebooks $NOTEBOOKS_HOME 
COPY config/jupyter_notebook_config.py /root/.jupyter/

EXPOSE 5678 6006 9000 9001 9002 9003 9004

CMD ["supervise", "."]
