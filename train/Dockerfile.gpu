FROM pipelineai/ubuntu-16.04-gpu:1.5.0

WORKDIR /root

# Hadoop
ENV \
  HADOOP_VERSION=2.7.5

RUN \
 wget https://s3.amazonaws.com/fluxcapacitor.com/packages/hadoop-${HADOOP_VERSION}.tar.gz \
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

COPY config/hdfs/ conf/

RUN \
  mv ${HADOOP_CONF}/core-site.xml ${HADOOP_CONF}/core-site.xml.orig \
  && cd ${HADOOP_CONF} \
  && ln -s ~/conf/core-site.xml

RUN \
  mv ${HADOOP_CONF}/hdfs-site.xml ${HADOOP_CONF}/hdfs-site.xml.orig \
  && cd ${HADOOP_CONF} \
  && ln -s ~/conf/hdfs-site.xml

COPY scripts/ scripts/

COPY run run

ENV \
  LOGS_HOME=/root/logs
RUN \
  mkdir -p $LOGS_HOME

ENV \
  LD_LIBRARY_PATH=/usr/lib/x86_64-linux-gnu:/usr/local/cuda/lib64:$LD_LIBRARY_PATH

EXPOSE 2222 6006

ENTRYPOINT ["./run"]
