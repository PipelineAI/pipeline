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
  && printf "\n\n\n\n\n\n\n\n\n" | ./configure

RUN \
  cd $TENSORFLOW_SERVING_HOME \
  && bazel build tensorflow_serving/...

ENV \
  PATH=$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/model_servers/:$PATH

RUN \
  cd $TENSORFLOW_HOME \
  && printf "\n\n\n\ny\ny\n\n\n\n" | ./configure

RUN \
  cd $TENSORFLOW_HOME \
  && bazel build --config=opt //tensorflow/tools/pip_package:build_pip_package

RUN \
  cd $TENSORFLOW_HOME \
  && bazel-bin/tensorflow/tools/pip_package/build_pip_package /tmp/tensorflow_pkg

# Utility for optimizing/simplifying models for inference
RUN \
  cd $TENSORFLOW_HOME \
  && bazel build tensorflow/python/tools:optimize_for_inference

ENV \
 TENSORFLOW_VERSION=1.0.0

RUN \
  pip install /tmp/tensorflow_pkg/tensorflow-${TENSORFLOW_VERSION}-cp35-cp35m-linux_x86_64.whl

ENV \
  LOGS_HOME=/root/logs \
  DATA_HOME=/root/data \
  MODELS_HOME=/root/models \
  TENSORBOARD_HOME=/root/tensorboard \
  SOURCE_HOME=/root/src

# TODO:  Remove this once we have a Spark sample
RUN \
  mkdir -p $SOURCE_HOME/spark

RUN \
  mkdir -p $LOGS_HOME \
  && mkdir -p $TENSORBOARD_HOME 

RUN \
  ln -s $TENSORFLOW_HOME

# Spark
ENV \
  SPARK_VERSION=2.0.1 \
  PYSPARK_VERSION=0.10.3

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

# Hadoop
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

COPY config/hadoop/*.xml ${HADOOP_CONF}

RUN \
  sed -i "s%<HADOOP_HOME>%${HADOOP_HOME}%" ${HADOOP_CONF}/*.xml \
  && sed -i "s%\${JAVA_HOME}%${JAVA_HOME}%" ${HADOOP_CONF}/hadoop-env.sh

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
  hadoop namenode -format

# Required by sshd
RUN \
  mkdir /var/run/sshd

# Required by Spark
ENV \
  HADOOP_CONF_DIR=${HADOOP_CONF}

# Required by Tensorflow
ENV \
  HADOOP_HDFS_HOME=${HADOOP_HOME}

#ENV \
#  CLASSPATH=$(${HADOOP_HDFS_HOME}/bin/hadoop classpath --glob)

ENV \
  LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${JAVA_HOME}/jre/lib/amd64/server

COPY config/spark/ conf/spark/
COPY config/hdfs/ conf/hdfs/
#COPY config/hadoop/ conf/hadoop/

#RUN \
#  mv ${HADOOP_CONF}/core-site.xml ${HADOOP_CONF}/core-site.xml.orig \
#  && cd ${HADOOP_CONF} \
#  && ln -s ~/conf/hadoop/core-site.xml

#RUN \
#  mv ${HADOOP_CONF}/hdfs-site.xml ${HADOOP_CONF}/hdfs-site.xml.orig \
#  && cd ${HADOOP_CONF} \
#  && ln -s ~/conf/hadoop/hdfs-site.xml

RUN \
  cd ${SPARK_HOME}/conf \
  && ln -s ~/conf/spark/core-site.xml

RUN \
  cd ${SPARK_HOME}/conf \
  && ln -s ~/conf/spark/hdfs-site.xml

# Airflow
ENV \ 
  AIRFLOW_HOME=/root/airflow

RUN \
  mkdir -p $AIRFLOW_HOME \
  && cd $AIRFLOW_HOME \
  && ln -s $SOURCE_HOME/airflow/dags 

COPY config/airflow/airflow.cfg $AIRFLOW_HOME/airflow.cfg 

COPY data $DATA_HOME
COPY models $MODELS_HOME
COPY src $SOURCE_HOME
COPY config/jupyter/jupyter_notebook_config.py /root/.jupyter/
COPY run run

RUN \
  ln -s $SOURCE_HOME/notebooks

RUN \
  cd $MODELS_HOME/inception_model \
  && tar -xzvf 00157585.tgz

# Kubernetes
ENV KUBERNETES_VERSION=1.5.1
ENV KUBERNETES_HOME=/root/kubernetes/

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/kubernetes.tar.gz \
  && tar -xzvf kubernetes.tar.gz \
  && rm kubernetes.tar.gz

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/bin/linux/amd64/kubectl

RUN \
  chmod a+x kubectl \
  && mv kubectl /usr/local/bin/kubectl

RUN \
  echo -en "\n" | $KUBERNETES_HOME/cluster/get-kube-binaries.sh

ENV \
  PATH=$KUBERNETES_HOME/client/bin:$PATH

EXPOSE 9000 36006 38080 38888 50070

CMD ["supervise", "."]
