FROM fluxcapacitor/package-ubuntu-16.04:master

WORKDIR /root

RUN \
  apt-get install -y software-properties-common \
  && add-apt-repository -y ppa:openjdk-r/ppa \
  && apt-get update \
  && apt-get install -y --no-install-recommends openjdk-8-jdk openjdk-8-jre-headless \
  && apt-get install -y apt-transport-https \
  && apt-get install -y wget \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

ENV \
  JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/

ENV \
  BAZEL_VERSION=0.4.5 \
  TENSORFLOW_SERVING_VERSION=0.5.1

# TensorFlow Serving Home (not required on PATH)
ENV \
  TENSORFLOW_SERVING_HOME=/root/serving \
  TENSORFLOW_HOME=/root/serving/tensorflow

# Required by TensorFlow Serving
RUN \
 apt-get update \
 && apt-get install -y \
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
  apt-get install -y python-qt4

RUN \
  mkdir /root/bazel \
  && cd /root/bazel \
  && curl -fSsL -O https://github.com/bazelbuild/bazel/releases/download/$BAZEL_VERSION/bazel-$BAZEL_VERSION-installer-linux-x86_64.sh \
  && curl -fSsL -o /root/bazel/LICENSE.txt https://raw.githubusercontent.com/bazelbuild/bazel/$BAZEL_VERSION/LICENSE.txt \
  && chmod +x bazel-*.sh \
  && ./bazel-$BAZEL_VERSION-installer-linux-x86_64.sh \
  && rm bazel-$BAZEL_VERSION-installer-linux-x86_64.sh

# Clone Tensorflow Serving and the Tensorflow Submodule
RUN \
 cd ~ \
 && git clone -b master --single-branch --recurse-submodules https://github.com/tensorflow/serving.git \
 && cd $TENSORFLOW_SERVING_HOME \
 && git reset --hard 4d0a571ff9c15b937f58d3d5e97a5310b5decf2b
   # be83fe98104f26362dd2819d20b53ffac9b8f592

ENV TF_NEED_CUDA=0
ENV TF_NEED_GCP=0
ENV TF_NEED_JEMALLOC=1
ENV TF_NEED_HDFS=1
ENV TF_NEED_OPENCL=0
ENV TF_ENABLE_XLA=0
ENV CC_OPT_FLAGS="-march=native"

ENV \
  LANG=en_us.UTF-8

RUN \
  cd $TENSORFLOW_HOME \
  && printf "\n\n\n" | ./configure

RUN \
  cd $TENSORFLOW_SERVING_HOME \
# http://stackoverflow.com/questions/41293077/how-to-compile-tensorflow-with-sse4-2-and-avx-instructions
# Note:  removing --copt=-mavx --copt=-mavx2 --copt=-mfma --copt=-mfpmath=both --copt=-msse4.1 --copt=-msse4.2 since the commodity AWS EC2 instance types (ie. r3.2xlarge) don't not support these optimizations
  && bazel build --local_resources 5000,1.0,1.0 -c opt --spawn_strategy=standalone --genrule_strategy=standalone -k //tensorflow_serving/...

ENV \
  PATH=$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/model_servers/:$PATH

ENV CI_BUILD_PYTHON python
ENV TF_ENABLE_XLA=1

RUN \
  cd $TENSORFLOW_HOME \
  && tensorflow/tools/ci_build/builds/configured CPU \
# Note:  removing --copt=-mavx --copt=-mavx2 --copt=-mfma --copt=-mfpmath=both --copt=-msse4.1 --copt=-msse4.2 since the commondity AWS EC2 instance types (ie. r3.2xlarge) don't not support these optimizations
  && bazel build --local_resources 5000,1.0,1.0 -c opt --spawn_strategy=standalone --genrule_strategy=standalone -k //tensorflow/...

RUN \
  cd $TENSORFLOW_HOME \
  && bazel-bin/tensorflow/tools/pip_package/build_pip_package /tmp/pip

RUN \
  pip --no-cache-dir install --ignore-installed --upgrade /tmp/pip/tensorflow-*.whl

RUN \
  TF_CPP_MIN_LOG_LEVEL=0 \
  TF_XLA_FLAGS=--xla_generate_hlo_graph=.*
