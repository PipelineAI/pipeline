FROM ubuntu:16.04 

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

# Utilites for optimizing/simplifying models for inference
RUN \
  cd $TENSORFLOW_HOME \
  && bazel build tensorflow/python/tools:optimize_for_inference

RUN \
  cd $TENSORFLOW_HOME \
  && bazel build tensorflow/python/tools:freeze_graph

RUN \
  cd $TENSORFLOW_HOME \
  && bazel build tensorflow/python/tools:strip_unused

RUN \
  cd $TENSORFLOW_HOME \
  && bazel build tensorflow/tools/graph_transforms:summarize_graph

RUN \
  cd $TENSORFLOW_HOME \
  && bazel build tensorflow/tools/graph_transforms:transform_graph

ENV \
 TENSORFLOW_VERSION=1.0.1

RUN \
  pip install /tmp/tensorflow_pkg/tensorflow-${TENSORFLOW_VERSION}-cp35-cp35m-linux_x86_64.whl

RUN \
  apt-get install -y vim \
  && apt-get install -y apache2

RUN \
  conda install --yes nbconvert

ENV \
  LOGS_HOME=/root/logs \
  DATASETS_HOME=/root/datasets \
  BIN_HOME=/root/bin \
  TENSORBOARD_HOME=/root/tensorboard \
  SOURCE_HOME=/root/src

# TODO:  Remove this once we have a Spark sample
#RUN \
#  mkdir -p $SOURCE_HOME/spark

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

RUN \
  git clone https://github.com/fluxcapacitor/pipeline.git

RUN \
  cd ${HADOOP_CONF} \
  && mv core-site.xml core-site.xml.orig \
  && ln -s ~/pipeline/education.ml/serving/config/hadoop/core-site.xml \
  && mv hdfs-site.xml hdfs-site.xml.orig \
  && ln -s ~/pipeline/education.ml/serving/config/hadoop/hdfs-site.xml

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

RUN \
  echo 'export CLASSPATH=$(${HADOOP_HDFS_HOME}/bin/hadoop classpath --glob)' >> /root/.bashrc

ENV \
  LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${JAVA_HOME}/jre/lib/amd64/server

RUN \
  mkdir -p ~/conf

RUN \
  cd ~/conf \
  && ln -s ~/pipeline/education.ml/serving/config/spark \
  && ln -s ~/pipeline/education.ml/serving/config/hdfs

RUN \
  cd ${SPARK_HOME}/conf \
  && ln -s ~/conf/spark/core-site.xml

RUN \
  cd ${SPARK_HOME}/conf \
  && ln -s ~/conf/spark/hdfs-site.xml

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

RUN \
  echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list \
  && sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 \
  && sudo apt-get update \
  && sudo apt-get install -y sbt

RUN \
  sbt clean clean-files

RUN \
  wget http://apache.mirrors.tds.net/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz \
  && tar -xvzf apache-maven-3.3.9-bin.tar.gz \
  && rm apache-maven-3.3.9-bin.tar.gz

ENV \
  MAVEN_HOME=/root/apache-maven-3.3.9/

ENV \
  PATH=$MAVEN_HOME/bin:$PATH

# Airflow
ENV \ 
  AIRFLOW_HOME=/root/airflow

RUN \
  pip install airflow==1.7.1.3

ENV \
  JMETER_VERSION=3.1

RUN \
  wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz \
  && tar xvzf apache-jmeter-${JMETER_VERSION}.tgz \
  && rm apache-jmeter-${JMETER_VERSION}.tgz

RUN \
  mkdir -p $AIRFLOW_HOME 

#COPY config/airflow/airflow.cfg $AIRFLOW_HOME/airflow.cfg 
RUN \
  cd $AIRFLOW_HOME \
  && ln -s ~/pipeline/education.ml/serving/config/airflow/airflow.cfg

ENV \
  JMETER_HOME=/root/apache-jmeter-${JMETER_VERSION} \
  NOTEBOOKS_HOME=$SOURCE_HOME/notebooks \
  LIB_HOME=/root/lib \
  TESTS_HOME=$SOURCE_HOME/tests \
  PREDICTION_HOME=$SOURCE_HOME/prediction.ml

RUN \
  cd ~ \
  && ln -s ~/pipeline/education.ml/serving/bin

RUN \
  cd ~ \
  && ln -s ~/pipeline/education.ml/serving/datasets

RUN \
  cd ~ \
  && ln -s ~/pipeline/education.ml/serving/src

RUN \
  cd ~ \
  && ln -s ~/pipeline/education.ml/serving/lib

RUN \
  mkdir -p ~/.jupyter/ \
  && cd ~/.jupyter \
  && ln -s ~/pipeline/education.ml/serving/config/jupyter/jupyter_notebook_config.py

RUN \
  cd $PREDICTION_HOME/codegen \
  && sbt clean package

RUN \
  cd $PREDICTION_HOME/pmml \
  && sbt clean package

RUN \
  cd $PREDICTION_HOME/keyvalue \
  && sbt clean package

RUN \
  cd $PREDICTION_HOME/tensorflow \
  && sbt clean package

ENV \
  PATH=$JMETER_HOME/bin:$BIN_HOME:$SPARK_HOME/bin:$SPARK_HOME/sbin:$PATH

RUN \
  /usr/sbin/sshd \
  && start-dfs.sh \
  && hadoop fs -copyFromLocal $DATASETS_HOME /

RUN \
  cd ~ \
  && ln -s ~/pipeline/education.ml/serving/run

RUN \
  mkdir -p ~/.ipython/profile_default/startup \
  && cd ~/.ipython/profile_default/startup \
  && ln -s ~/config/jupyter/00-setup-spark.py

EXPOSE 39000 39040 39041 39042 39043 46006 38080 38888 50070 46060 46061 46066 47077 44040 44041 44042 44043 44044 47979

CMD ["supervise", "."]
