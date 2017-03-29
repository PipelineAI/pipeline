FROM fluxcapacitor/package-java-openjdk-1.8:master

WORKDIR /root

ENV \
  HADOOP_VERSION=2.7.2

# Apache Hadoop
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

COPY /etc/hadoop/*.xml ${HADOOP_CONF}

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

RUN \
  apt-get install -y python2.7-dev \
  make \
  libkrb5-dev \
  libxml2-dev \
  libffi-dev \
  libxslt-dev \
  libsqlite3-dev \
  libssl-dev \
  libldap2-dev \
  python-pip

RUN \
  apt-get install -y ant gcc g++ libkrb5-dev libffi-dev libmysqlclient-dev libssl-dev libsasl2-dev libsasl2-modules-gssapi-mit libsqlite3-dev libtidy-0.99-0 libxml2-dev libxslt-dev make libldap2-dev python-dev python-setuptools libgmp3-dev

#RUN \
#  cd hue-3.12.0 && make apps

# Required by sshd
RUN \
  mkdir /var/run/sshd

COPY run run

EXPOSE 50070 9000 
  #8000

CMD ["supervise", "."]
