FROM fluxcapacitor/package-ubuntu-14.04

WORKDIR /root

ENV \
 TENSORFLOW_VERSION=0.11.0 \
 TENSORFLOW_SERVING_VERSION=master \
 SPRING_CORE_VERSION=4.3.0.RELEASE \
 SPRING_BOOT_VERSION=1.3.5.RELEASE \
 SPRING_CLOUD_VERSION=1.1.2.RELEASE \
 SPRING_CLOUD_DEPENDENCIES=Brixton.RC1 \
 BAZEL_VERSION=0.3.2
 
# TensorFlow Serving Home (not required on PATH)
ENV \
  TENSORFLOW_SERVING_HOME=/root/serving

# Required by TensorFlow Serving
RUN \
 apt-get update \
 && apt-get install -y \
      build-essential \
      libfreetype6-dev \
      libpng12-dev \
      libzmq3-dev \
      pkg-config \
      python-dev \
      python-numpy \
      python-pip \
      software-properties-common \
      swig \
      zip \
      zlib1g-dev \
      libcurl3-dev

RUN pip install enum34 futures mock six && \
    pip install --pre 'protobuf>=3.0.0a3' && \
    pip install -i https://testpypi.python.org/simple --pre grpcio

RUN \
  echo "build --spawn_strategy=standalone --genrule_strategy=standalone" >>/root/.bazelrc

ENV \
  BAZELRC=/root/.bazelrc

RUN add-apt-repository -y ppa:openjdk-r/ppa && \
    apt-get update && \
    apt-get install -y --no-install-recommends openjdk-8-jdk openjdk-8-jre-headless && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

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
  && printf "\n\n\n\n\n\n" | ./configure

RUN \
  cd ~/serving \
  && bazel build -c opt tensorflow_serving/...

RUN \
  echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list \
  && sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 \
  && sudo apt-get update \
  && sudo apt-get install -y sbt

RUN \
#  apt-get install -y maven
  wget http://apache.mirrors.tds.net/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz \
  && tar -xvzf apache-maven-3.3.9-bin.tar.gz \
  && rm apache-maven-3.3.9-bin.tar.gz

RUN \
  export PATH=~/apache-maven-3.3.9/bin:$PATH

###################################
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
  conda install --yes scikit-learn numpy scipy ipython jupyter matplotlib pandas seaborn

RUN \
  conda install -c conda-forge tensorflow

RUN \
  conda install --yes -c conda-forge py4j

# Overcomes limitation with conda matplotlib (1.5.1)
RUN \
  apt-get install -y python-qt4
###################################

RUN \
  mkdir -p /root/logs

ENV \
  LOGS_HOME=/root/logs

COPY run run
COPY datasets/ datasets/
COPY src/ src/
COPY build.sbt build.sbt
COPY lib/ lib/
COPY client/ client/
COPY models/ models/

#RUN \
#  cd client \
#  && mvn clean install \
#  && cp target/tensorflow-prediction-client-1.0-SNAPSHOT.jar ../lib

RUN \
  sbt clean package 

RUN \
  mkdir -p /root/tensorboard 

ENV \
  DATASETS_HOME=/root/datasets \
  MODELS_HOME=/root/models

ENV \
  PATH=$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/model_servers/:$PATH
 
EXPOSE 5678 6006 9040 9090 9091

CMD ["supervise", "."]
