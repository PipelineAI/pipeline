FROM fluxcapacitor/package-java-openjdk-1.8

WORKDIR /root

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
  TENSORFLOW_VERSION=1.0.0

RUN \
  pip install --ignore-installed --upgrade pip setuptools \
  && pip install --upgrade tensorflow==$TENSORFLOW_VERSION

ENV \ 
  AIRFLOW_HOME=/root/airflow

# MySql Python Adapter (Used by SQLAlchemy/Airflow)
RUN \
  apt-get update \
  && apt-get install -y python-mysqldb \
  && apt-get install -y mysql-client \
  && apt-get install -y libmysql-java \
  && apt-get install -y libmysqlclient-dev 

RUN \
  pip install mysqlclient 
#  && conda install --yes -c anaconda mysql-connector-python=2.0.4 \
#  && conda install --yes pymysql=0.7.6
#  && conda install --yes -c anaconda mysql-connector-python \
#  && conda install --yes pymysql

#RUN \
#  conda install --yes -c phumke airflow=1.6.2

RUN \
  echo "deb http://cran.rstudio.com/bin/linux/ubuntu trusty/" >> /etc/apt/sources.list \
  && gpg --keyserver keyserver.ubuntu.com --recv-key E084DAB9 \
  && gpg -a --export E084DAB9 | apt-key add - \
  && apt-get update \
  && apt-get install -y r-base \
  && apt-get install -y r-base-dev

ENV SPARK_VERSION=2.0.1
ENV PYSPARK_VERSION=0.10.3

RUN \
  # This is not a custom version of Spark.  It's merely a version with all the desired -P profiles enabled.
  wget https://s3.amazonaws.com/fluxcapacitor.com/packages/spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz \
  && tar xvzf spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz \
  && rm spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz

ENV SPARK_HOME=/root/spark-${SPARK_VERSION}-bin-fluxcapacitor
ENV PATH=$SPARK_HOME/bin:$PATH

RUN \
  conda install --yes -c conda-forge gitpython=2.1.1

#RUN \
#  wget https://apt.dockerproject.org/repo/pool/main/d/docker-engine/docker-engine_1.12.6-0~ubuntu-trusty_amd64.deb \
#  && dpkg -i docker-engine_1.12.6-0~ubuntu-trusty_amd64.deb \
#  && apt-get -f install

RUN \
#  curl -sSL https://get.docker.com/ | sh
  curl -fsSLO https://get.docker.com/builds/Linux/x86_64/docker-1.11.2.tgz \
  && tar --strip-components=1 -xvzf docker-1.11.2.tgz -C /usr/local/bin

#RUN \
#  /usr/local/bin/dockerd

RUN \
  git clone http://github.com/fluxcapacitor/pipeline

ENV \
  KUBERNETES_VERSION=1.5.1

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/bin/linux/amd64/kubectl

RUN \
  sudo chmod a+x kubectl \
  && sudo mv kubectl /usr/local/bin/kubectl

COPY .kube/ .kube/

#COPY dags/ $AIRFLOW_HOME/dags/
RUN \
  mkdir -p $AIRFLOW_HOME \
  && cd $AIRFLOW_HOME \
  && ln -s /root/pipeline/scheduler.ml/airflow/dags

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

RUN \
  pip install airflow[celery,mysql]==1.7.1.3 \
  && pip install celery[redis] \
  && pip install flower

# Temp workaround for running Celery worker as root
ENV \
  C_FORCE_ROOT=true

COPY config/spark/ $SPARK_HOME/conf/
COPY config/airflow/airflow.cfg $AIRFLOW_HOME/airflow.cfg 

COPY github_webhook github_webhook

COPY run run

EXPOSE 8080 5000 5555 8793

CMD ["supervise", "."]
