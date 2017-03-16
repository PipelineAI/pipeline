FROM fluxcapacitor/package-java-openjdk-1.8

WORKDIR /root

ENV \
 SPRING_CORE_VERSION=4.3.0.RELEASE \
 SPRING_BOOT_VERSION=1.3.5.RELEASE \
 SPRING_CLOUD_VERSION=1.1.2.RELEASE \
 SPRING_CLOUD_DEPENDENCIES=Brixton.RC1 \
 BAZEL_VERSION=0.4.4 \
 TENSORFLOW_SERVING_VERSION=0.5.1
 
# TensorFlow Serving Home (not required on PATH)
ENV \
  TENSORFLOW_SERVING_HOME=/root/serving

# Required by TensorFlow Serving
RUN \
  apt-get update && sudo apt-get install -y \
        build-essential \
        curl \
        libcurl3-dev \
        git \
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
        zlib1g-dev

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
  && printf "\n\n\n\n\n\n\n\n\n" | ./configure

RUN \
  cd ~/serving \
  && bazel build -c opt tensorflow_serving/...

ENV \
  PATH=$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/model_servers/:$PATH

# Utility for optimizing/simplifying models for inference
RUN \
  cd ~/serving/tensorflow \
  && bazel build tensorflow/python/tools:optimize_for_inference

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

ENV \
  TENSORFLOW_VERSION=1.0.1

RUN \
  pip install --ignore-installed --upgrade pip setuptools \
  && pip install --upgrade tensorflow==$TENSORFLOW_VERSION

RUN \
  apt-get update \
  && apt-get install -y python-qt4

RUN \
  mkdir -p /root/logs

ENV \
  LOGS_HOME=/root/logs

COPY run run
COPY src/ src/
COPY build.sbt build.sbt
COPY lib/ lib/
COPY client/ client/
COPY store/ store/

RUN \
  cd client \
  && /root/apache-maven-3.3.9/bin/mvn clean install \
  && cp target/tensorflow-prediction-client-1.0-SNAPSHOT.jar ../lib

RUN \
  sbt clean package 

RUN \
  mkdir -p /root/tensorboard 

ENV \
  STORE_HOME=/root/store  

#RUN \
#  cd $STORE_HOME/tensorflow_inception \
#  && wget https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip \
#  && unzip inception5h.zip

EXPOSE 5678 6006 9000 9001 9002 9040 

CMD ["supervise", "."]
