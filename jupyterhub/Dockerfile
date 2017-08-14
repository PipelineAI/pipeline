FROM fluxcapacitor/package-tensorflow-16d39e9-d690fdd-cpu:master

WORKDIR /root

# Based on the following:  https://github.com/jupyterhub/jupyterhub/blob/master/Dockerfile

# install nodejs, utf8 locale
ENV DEBIAN_FRONTEND noninteractive

RUN \
  apt-get -y update \
#  apt-get -y upgrade && \
  && apt-get -y install npm nodejs nodejs-legacy wget locales git

# libav-tools for matplotlib anim
RUN \
  apt-get install -y --no-install-recommends libav-tools 

# Install JupyterHub dependencies
RUN \
  npm install -g configurable-http-proxy && rm -rf ~/.npm


# Downgrade Python 3.6 (from the base FROM Docker image built with Python 3.6)
#   to Python 3.5 since Spark doesn't like Python 3.6
RUN \
  conda update conda \
  && conda install python=3.5

RUN \
  pip install jupyterhub==0.7.2 \
  && pip install ipykernel==4.6.1 \
  && pip install notebook==5.0.0 \
  && pip install ipywidgets==6.0.0 \
  && pip install findspark==1.1.0 \
  && pip install tabulate==0.7.7

#RUN \
#  pip install jupyterlab==0.26.0 
#  && jupyter serverextension enable --py jupyterlab --sys-prefix

#RUN \
#  pip install jupyterlab_widgets==0.6.15 \
#  && pip install widgetslabextension==0.1.0

#RUN \
#  jupyter labextension install jupyterlab-hub 

RUN \
  pip install jupyterhub-dummyauthenticator==0.3.1 \
              oauthenticator==0.5.1 \
              jupyterhub-simplespawner==0.1 
RUN \
  pip install git+https://github.com/jupyterhub/kubespawner

RUN \
  pip install nbconvert==5.2.1 \
  && conda install --yes graphviz==2.38.0 \
  && conda install --yes seaborn==0.7.1 \
  && conda install --yes dill==0.2.5

RUN \
  conda config --add channels r && \
  conda install --quiet --yes \
    'r-base=3.3.2' \
    'r-irkernel=0.7*' \
    'r-plyr=1.8*' \
    'r-devtools=1.12*' \
    'r-tidyverse=1.0*' \
    'r-shiny=0.14*' \
    'r-rmarkdown=1.2*' \
    'r-forecast=7.3*' \
    'r-rsqlite=1.1*' \
    'r-reshape2=1.4*' \
    'r-nycflights13=0.2*' \
    'r-caret=6.0*' \
    'r-rcurl=1.95*' \
    'r-crayon=1.3*' \
    'r-randomforest=4.6*' && conda clean -tipsy

RUN \
  mkdir -p /root/tensorboard

RUN \
  mkdir -p /root/models

RUN \
  mkdir -p /root/logs

# Apache2
RUN \
  apt-get update \
  && apt-get install -y apache2

RUN \
  a2enmod proxy \
  && a2enmod proxy_http \
  && a2dissite 000-default

RUN \
  mv /var/www/html /var/www/html.orig

RUN \
  mv /etc/apache2/apache2.conf /etc/apache2/apache2.conf.orig

COPY config/apache2/ config/apache2/

COPY config/jupyterhub/ config/jupyterhub/
COPY profiles/ /root/.ipython/
COPY img/ img/

COPY share/ share/
RUN \
  mv /opt/conda/share/jupyter/hub/templates/login.html /opt/conda/share/jupyter/hub/templates/login.html.orig \
  && mv /opt/conda/share/jupyter/hub/templates/page.html /opt/conda/share/jupyter/hub/templates/page.html.orig \
  && cd /opt/conda/share/jupyter/hub/templates/ \
  && ln -s ~/share/jupyter/hub/templates/login.html \
  && ln -s ~/share/jupyter/hub/templates/page.html 

COPY notebook/ notebook/
RUN \
  mv /opt/conda/lib/python3.5/site-packages/notebook/static/custom/ /opt/conda/lib/python3.5/site-packages/notebook/static/custom.orig \
  && cd /opt/conda/lib/python3.5/site-packages/notebook/static/ \
  && ln -s ~/notebook/static/custom

RUN \
  mv /opt/conda/lib/python3.5/site-packages/notebook/templates/tree.html /opt/conda/lib/python3.5/site-packages/notebook/templates/tree.html.orig \
  && cd /opt/conda/lib/python3.5/site-packages/notebook/templates/ \
  && ln -s ~/notebook/templates/tree.html

COPY oauthenticator/ oauthenticator/
RUN \
  cd oauthenticator \
  && pip install -e .

RUN \
  conda install -c conda-forge xgboost=0.6a2

RUN \
  pip install sklearn_pandas==1.4.0 \
  && pip install git+https://github.com/jpmml/sklearn2pmml.git@0.20.3

RUN \
#  apt-get update \
  apt-get install -y default-jdk

ENV \
  JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/

COPY sysutils/ sysutils/

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

# Required by Spark
ENV \
  HADOOP_CONF_DIR=${HADOOP_CONF}

COPY config/spark/ config/spark/
RUN \
  cd ${SPARK_HOME}/conf \
  && ln -s /root/config/spark/core-site.xml \
  && ln -s /root/config/spark/fairscheduler.xml \
  && ln -s /root/config/spark/hdfs-site.xml \
  && ln -s /root/config/spark/hive-site.xml \
  && ln -s /root/config/spark/spark-defaults.conf

COPY config/hadoop/ config/hadoop/
RUN \
  mv ${HADOOP_CONF}/core-site.xml ${HADOOP_CONF}/core-site.xml.orig \
  && cd ${HADOOP_CONF} \
  && ln -s /root/config/hadoop/core-site.xml

RUN \
  mv ${HADOOP_CONF}/hdfs-site.xml ${HADOOP_CONF}/hdfs-site.xml.orig \
  && cd ${HADOOP_CONF} \
  && ln -s /root/config/hadoop/hdfs-site.xml

# Required by Tensorflow
ENV \
  HADOOP_HDFS_HOME=${HADOOP_HOME}

# Required by Tensorflow for HDFS
RUN \
  echo 'export CLASSPATH=$(${HADOOP_HDFS_HOME}/bin/hadoop classpath --glob)' >> /root/.bashrc

ENV \
  LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${JAVA_HOME}/jre/lib/amd64/server

ENV \
  PATH=/root/scripts:$PATH

ENV \
  PATH=$TENSORFLOW_HOME/bazel-bin/tensorflow/tools/graph_transforms:$TENSORFLOW_HOME/bazel-bin/tensorflow/python/tools:$TENSORFLOW_HOME/bazel-bin/tensorflow/tools/benchmark/:$TENSORFLOW_HOME/bazel-bin/tensorflow/compiler/aot:$TENSORFLOW_HOME/bazel-bin/tensorflow/compiler/tests:$TENSORFLOW_HOME/bazel-bin/tensorflow/examples/tutorials/word2vec:$TENSORFLOW_HOME/bazel-bin/tensorflow/examples/tutorials/mnist:/root/apache-jmeter-3.1/bin/:$PATH

RUN \
  apt-get install -y ssh

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

# Required by sshd
RUN \
  mkdir /var/run/sshd

COPY apache-jmeter-3.1/ apache-jmeter-3.1/

RUN \
  pip install tensorboard

RUN \
  pip install keras==2.0.5

COPY notebooks/ notebooks/
COPY lib/ lib/
#COPY datasets/ datasets/
COPY scripts/ scripts/
COPY src/ src/
COPY tests/ tests/
COPY html/ html/

RUN \
  pip install pio-cli==0.71

ENV \
  TF_CPP_MIN_LOG_LEVEL=0 \
  TF_XLA_FLAGS=--xla_generate_hlo_graph=.*

# This will install tensorflow for Python 3.5 since we overrode the Python way up top.
RUN \
  pip install tensorflow==1.3.0-rc2

RUN \
  pip install pipeline-ai-cli==0.44

EXPOSE 80 50070 39000 9000 9001 9002 9003 9004 6006 8754 7077 6066 6060 6061 4040 4041 4042 4043 4044 2222 10254

COPY run run

CMD ["supervise", "."]
