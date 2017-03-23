FROM fluxcapacitor/package-gpu-cuda8-16.04:master

WORKDIR /root

# Java
RUN \
  apt-get update \
  && add-apt-repository -y ppa:openjdk-r/ppa \
  && apt-get update \
  && apt-get install -y --no-install-recommends openjdk-8-jdk openjdk-8-jre-headless \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

ENV \
  JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/

ENV \
  BAZEL_VERSION=0.4.4 \
  TENSORFLOW_SERVING_VERSION=0.5.1

# TensorFlow Serving Home (not required on PATH)
ENV \
  TENSORFLOW_SERVING_HOME=/root/serving \
  TENSORFLOW_HOME=/root/serving/tensorflow

# Required by TensorFlow Serving
RUN \
  apt-get update && apt-get install -y \
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
  conda install --yes openblas scikit-learn numpy scipy ipython jupyter matplotlib pandas

RUN \
  conda install --yes -c conda-forge jupyterhub=0.7.2 \
  && conda install --yes -c conda-forge ipykernel=4.5.2 \
  && conda install --yes -c conda-forge notebook=4.4.1 \
  && conda install --yes -c conda-forge findspark=1.0.0 \
  && conda install --yes -c conda-forge jupyter_contrib_nbextensions=0.2.4 \
  && conda install --yes -c anaconda-nb-extensions anaconda-nb-extensions=1.0.0

RUN \
  pip install jupyterlab \
  && jupyter serverextension enable --py jupyterlab --sys-prefix

# Install non-secure dummyauthenticator for jupyterhub (dev purposes only)
RUN \
  pip install jupyterhub-dummyauthenticator

RUN \
  pip install jupyterhub-simplespawner

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
 && git clone -b master --single-branch --recurse-submodules https://github.com/tensorflow/serving.git

RUN \
 cd $TENSORFLOW_SERVING_HOME \
 && git reset --hard ed82469b2797a437a8fe2a05e08c0a1d48ae1263

ENV TF_NEED_CUDA=1
ENV TF_NEED_GCP=0
ENV TF_NEED_JEMALLOC=1
ENV TF_NEED_HDFS=0
ENV TF_NEED_OPENCL=0
ENV TF_ENABLE_XLA=0
ENV TF_CUDA_VERSION=8.0
ENV TF_CUDNN_VERSION=5
ENV CUDA_PATH="/usr/local/cuda"
ENV CUDA_TOOLKIT_PATH=$CUDA_PATH
ENV CUDNN_INSTALL_PATH=$CUDA_PATH
#ENV GCC_HOST_COMPILER_PATH=$(which gcc-5 || which gcc)
#ENV PYTHON_BIN_PATH=$(which python)
ENV CC_OPT_FLAGS="-march=native"
ENV TF_CUDA_COMPUTE_CAPABILITIES=3.0,3.5,3.7,5.2,6.0,6.1
# Check the required COMPUTE_CAPABILITIES from the following link:
#  https://developer.nvidia.com/cuda-gpus
# Also, Tensorflow has a minimum-supported COMPUTE CAPABILITY (ie. 3.5)
# ie. here are the AWS EC2 Instance Types and their COMPUTE CAPABILITIES
#
## AWS P2, GCP Tesla K80 Instances (3.7)
# Product Type  Tesla
# Product Series  K-Series
# Product  K-80
# Operating System  Linux 64-bit
# Recommended/Beta  Recommended/Certified
#
## G2 Instances (3.5)
# Product Type  GRID
# Product Series  GRID Series
# Product  GRID K520
# Operating System  Linux 64-bit
# Recommended/Beta  Recommended/Certified
#
## CG1 Instances (3.0)
# Product Type  Tesla
# Product Series  M-Class
# Product  M2050
# Operating System  Linux 64-bit
# Recommended/Beta  Recommended/Certified

RUN \
  cd $TENSORFLOW_HOME \
  && printf "\n\n\n" | ./configure

RUN \
  cd $TENSORFLOW_SERVING_HOME \
  && sed -i.bak 's/@org_tensorflow\/\/third_party\/gpus\/crosstool/@local_config_cuda\/\/crosstool:toolchain/g' tools/bazel.rc \
  && sed -i.bak '/nccl/d' tensorflow/tensorflow/contrib/BUILD \
  && bazel build -c opt --config=cuda --spawn_strategy=standalone tensorflow_serving/...

ENV \
  PATH=$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/model_servers/:$PATH

# Configure the build for our CUDA configuration.
ENV CI_BUILD_PYTHON python

ENV TF_ENABLE_XLA=1

RUN \
  cd $TENSORFLOW_HOME \
  && tensorflow/tools/ci_build/builds/configured GPU \
    bazel build -c opt --config=cuda tensorflow/tools/pip_package:build_pip_package \
  && bazel-bin/tensorflow/tools/pip_package/build_pip_package /tmp/pip

RUN \
  pip --no-cache-dir install --ignore-installed --upgrade /tmp/pip/tensorflow-*.whl

COPY lib/ lib/

RUN \
  cd ~/lib/jni \
  && ln -s ~/lib/jni/libtensorflow_jni-gpu.so libtensorflow_jni.so

# Utility for optimizing/simplifying models for inference
RUN \
  cd $TENSORFLOW_HOME \
  && bazel build tensorflow/tools/graph_transforms:all

# Utility for optimizing/simplifying models for inference
RUN \
  cd $TENSORFLOW_HOME \
  && bazel build tensorflow/python/tools:all

RUN \
  cd $TENSORFLOW_SERVING_HOME \
  && bazel build //tensorflow_serving/example:all

RUN \
  apt-get update \
  && apt-get install -y python-qt4

# Spark
ENV \
  SPARK_VERSION=2.1.0 \
  PYSPARK_VERSION=0.10.4

RUN \
  # This is not a custom version of Spark.  It's merely a version with all the desired -P profiles enabled.
  wget https://s3.amazonaws.com/fluxcapacitor.com/packages/spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz \
  && tar xvzf spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz \
  && rm spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz

ENV \
  SPARK_HOME=/root/spark-${SPARK_VERSION}-bin-fluxcapacitor

# This must be separate from the ${SPARK_HOME} ENV definition or else Docker doesn't recognize it
ENV \
  PATH=${SPARK_HOME}/bin:$PATH

# Hadoop/HDFS
ENV \
  HADOOP_VERSION=2.7.2

RUN \
 wget http://www.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz \
 && tar xvzf hadoop-${HADOOP_VERSION}.tar.gz \
 && rm hadoop-${HADOOP_VERSION}.tar.gz

ENV \
  HADOOP_HOME=/root/hadoop-${HADOOP_VERSION} \
  HADOOP_OPTS=-Djava.net.preferIPv4Stack=true

# This must be separate from the ${HADOOP_HOME} ENV definition or else Docker doesn't recognize it
ENV \
  HADOOP_CONF=${HADOOP_HOME}/etc/hadoop/ \
  PATH=${HADOOP_HOME}/bin:${HADOOP_HOME}/sbin:${PATH}

# Required by Tensorflow for HDFS
RUN \
  echo 'export CLASSPATH=$(${HADOOP_HDFS_HOME}/bin/hadoop classpath --glob)' >> /root/.bashrc

ENV \
  LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${JAVA_HOME}/jre/lib/amd64/server

# Required by Spark
ENV \
  HADOOP_CONF_DIR=${HADOOP_CONF}

COPY config/ config/
RUN \
  cd ${SPARK_HOME}/conf \
  && ln -s /root/config/spark/core-site.xml \
  && ln -s /root/config/spark/fairscheduler.xml \
  && ln -s /root/config/spark/hdfs-site.xml \
  && ln -s /root/config/spark/hive-site.xml \
  && ln -s /root/config/spark/spark-defaults.conf

RUN \
  mv ${HADOOP_CONF}/core-site.xml ${HADOOP_CONF}/core-site.xml.orig \
  && cd ${HADOOP_CONF} \
  && ln -s /root/config/hadoop/core-site.xml

RUN \
  mv ${HADOOP_CONF}/hdfs-site.xml ${HADOOP_CONF}/hdfs-site.xml.orig \
  && cd ${HADOOP_CONF} \
  && ln -s /root/config/hadoop/hdfs-site.xml

RUN \
  apt-get update \
  && apt-get install -y ssh

RUN \
  sed -i s/#PermitRootLogin.*/PermitRootLogin\ yes/ /etc/ssh/sshd_config \
  && sed -i s/#.*StrictHostKeyChecking.*/StrictHostKeyChecking\ no/ /etc/ssh/ssh_config \
  && ssh-keygen -A \
  && ssh-keygen -t rsa -f /root/.ssh/id_rsa -q -N "" \
  && cat /root/.ssh/id_rsa.pub > /root/.ssh/authorized_keys

RUN \
  sed -i "s%<HADOOP_HOME>%${HADOOP_HOME}%" ${HADOOP_CONF}/*.xml \
  && sed -i "s%\${JAVA_HOME}%${JAVA_HOME}%" ${HADOOP_CONF}/hadoop-env.sh

RUN \
  hadoop namenode -format

# Hue (port 8000)
#RUN \
#  wget https://dl.dropboxusercontent.com/u/730827/hue/releases/3.12.0/hue-3.12.0.tgz \
#  && tar xvzf hue-3.12.0.tgz \
#  && rm hue-3.12.0.tgz

# Required by sshd
RUN \
  mkdir /var/run/sshd

COPY src/ src/
COPY notebooks/ notebooks/
COPY profiles/ /root/.ipython/
COPY run run

# Expose Spark Worker Port for Web Admin UI
EXPOSE 50070 39000 9000 6006 8754 7077 6066 6060 6061 4040 4041 4042 4043 4044
  # 8000
CMD ["supervise", "."]
