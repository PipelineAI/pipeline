FROM fluxcapacitor/package-tensorflow-master-gpu:master

WORKDIR /root

RUN \
  conda install --yes -c conda-forge jupyterhub=0.7.2 \
  && conda install --yes -c conda-forge ipykernel=4.5.2 \
  && conda install --yes -c conda-forge notebook=4.4.1 \
  && conda install --yes -c conda-forge findspark=1.0.0 \
  && conda install --yes -c conda-forge jupyter_contrib_nbextensions=0.2.4 \
  && conda install --yes -c conda-forge ipywidgets=6.0.0 \
  && conda install --yes -c anaconda-nb-extensions anaconda-nb-extensions=1.0.0 

RUN \
  pip install jupyterlab \
  && jupyter serverextension enable --py jupyterlab --sys-prefix

RUN \
  jupyter nbextension enable --py widgetsnbextension --sys-prefix

# Install non-secure dummyauthenticator for jupyterhub (dev purposes only)
RUN \
  pip install jupyterhub-dummyauthenticator

RUN \
  pip install jupyterhub-simplespawner

RUN \
  conda install --yes nbconvert \
  && conda install --yes graphviz

RUN \
  pip install grpcio \
  && pip install git+https://github.com/wiliamsouza/hystrix-py.git

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

# Required by Tensorflow
ENV \
  HADOOP_HDFS_HOME=${HADOOP_HOME}

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

RUN \
  git clone https://github.com/fluxcapacitor/pipeline.git

RUN \
  ln -s pipeline/oreilly.ml/high-performance-tensorflow/src \ 
  && ln -s pipeline/oreilly.ml/high-performance-tensorflow/notebooks \
  && mkdir -p /root/.ipython \
  && cd /root/.ipython \
  && ln -s pipeline/oreilly.ml/high-performance-tensorflow/profiles \
  && cd ~ \
  && ln -s pipeline/oreilly.ml/high-performance-tensorflow/html \
  && ln -s pipeline/oreilly.ml/high-performance-tensorflow/run  \
  && ln -s pipeline/oreilly.ml/high-performance-tensorflow/lib \
  && ln -s pipeline/oreilly.ml/high-performance-tensorflow/apache-jmeter-3.1 \
  && ln -s pipeline/oreilly.ml/high-performance-tensorflow/tests \
  && ln -s pipeline/oreilly.ml/high-performance-tensorflow/datasets \
  && ln -s pipeline/oreilly.ml/high-performance-tensorflow/scripts

RUN \
  cd ~/lib/jni \
  && ln -s ~/lib/jni/libtensorflow_jni-gpu.so libtensorflow_jni.so

ENV \
  TF_CPP_MIN_LOG_LEVEL=0 \
  TF_XLA_FLAGS=--xla_generate_hlo_graph=.*

ENV \
  PATH=$TENSORFLOW_HOME/bazel-bin/tensorflow/tools/graph_transforms:$TENSORFLOW_HOME/bazel-bin/tensorflow/python/tools:$TENSORFLOW_HOME/bazel-bin/tensorflow/tools/benchmark/:$TENSORFLOW_HOME/bazel-bin/tensorflow/compiler/aot:$TENSORFLOW_HOME/bazel-bin/tensorflow/compiler/tests:$TENSORFLOW_HOME/bazel-bin/tensorflow/examples/tutorials/word2vec:$TENSORFLOW_HOME/bazel-bin/tensorflow/examples/tutorials/mnist:/root/apache-jmeter-3.1/bin/:/root/scripts:$PATH

RUN \
  conda install --yes graphviz

COPY apache-jmeter-3.1/ apache-jmeter-3.1/
COPY tests/ tests/
COPY datasets/ datasets/ 

RUN \
  mkdir -p /root/tensorboard

RUN \
  mkdir -p /root/models

RUN \
  mkdir -p /root/logs

# Apache2
RUN \
  apt-get install -y apache2

RUN \
  a2enmod proxy \
  && a2enmod proxy_http \
  && a2dissite 000-default

RUN \
  mv /var/www/html /var/www/html.orig

RUN \
  mv /etc/apache2/apache2.conf /etc/apache2/apache2.conf.orig

# All paths (dirs, not files) up to and including /root must have +x permissions.
# It's just the way linux works.  Don't fight it.
# http://askubuntu.com/questions/537032/how-to-configure-apache2-with-symbolic-links-in-var-www
RUN \
  chmod a+x /root

EXPOSE 80 50070 39000 9000 9001 9002 9003 9004 6006 8754 7077 6066 6060 6061 4040 4041 4042 4043 4044
  # 8000
CMD ["supervise", "."]
