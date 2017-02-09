FROM fluxcapacitor/package-anaconda-4.1.11

WORKDIR /root

RUN \
  apt-get update 

ENV \
  TENSORFLOW_VERSION=0.12.1

RUN \
#  pip install https://storage.googleapis.com/tensorflow/linux/cpu/tensorflow-$TENSORFLOW_VERSION-cp35-cp35m-linux_x86_64.whl
  conda install -c conda-forge tensorflow=$TENSORFLOW_VERSION

# Java
RUN \
  apt-get update \
  && apt-get install -y software-properties-common \
  && add-apt-repository -y ppa:openjdk-r/ppa \
  && apt-get update \
  && apt-get install -y --no-install-recommends openjdk-8-jdk openjdk-8-jre-headless \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

ENV \
  JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/

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

# Required by Tensorflow
ENV \
  HADOOP_HDFS_HOME=${HADOOP_HOME}

#ENV \
#  CLASSPATH=$(${HADOOP_HDFS_HOME}/bin/hadoop classpath --glob)

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
  
COPY bin/ bin/
COPY data/ data/

COPY run run

EXPOSE 2222

CMD ["supervise", "."]
