FROM ubuntu:14.04

WORKDIR /root

# Notes:
#   The contents ond tools installed in this Dockerfile have only been tested on Ubuntu 14.04.
#   Use at your own risk if you are trying to apply these instructions to a different environment.
#   We've done our best to highlight (Optional) installs - usually around system-level performance monitoring tools like "perf" from the linux-tools package.
#   Feel free to leave out these installs, but you may lose compatibility with future releases of this distribution.
#   It's highly-advised that you run this distributed of Docker/Ubuntu on whatever host system you are running (ie. RHEL, CentOS, etc)

# These are environment variables that match the versions of the sofware tools installed by this Dockerfile.
# We also need to include library dependency versions as we trigger a build of all Scala/Java-based source code 
#  at the end in order to pre-bake the dependencies into the Docker image.  This saves time and network bandwidth later.
#
ENV \ 
 CASSANDRA_VERSION=3.9 \
 CONFLUENT_VERSION=3.0.0 \
 ELASTICSEARCH_VERSION=2.3.0 \
 LOGSTASH_VERSION=2.3.0 \
 KIBANA_VERSION=4.5.0 \
 REDIS_VERSION=3.0.5 \
 SBT_VERSION=0.13.9 \
 HADOOP_VERSION=2.6.0 \
 HIVE_VERSION=1.2.1 \
 ZEPPELIN_VERSION=0.6.0 \
 GENSORT_VERSION=1.5 \
 SCALA_VERSION=2.10.5 \
 SCALA_MAJOR_VERSION=2.10 \
 SPARK_VERSION=1.6.1 \
 SPARK_OTHER_VERSION=2.1.0 \
 STANFORD_CORENLP_VERSION=3.6.0 \
 NIFI_VERSION=0.6.1 \
 PRESTO_VERSION=0.167 \
 TITAN_VERSION=1.0.0-hadoop1 \
 AKKA_VERSION=2.3.11 \
 SPARK_CASSANDRA_CONNECTOR_VERSION=1.6.5 \
 SPARK_ELASTICSEARCH_CONNECTOR_VERSION=2.3.0.BUILD-SNAPSHOT \
 KAFKA_CLIENT_VERSION=0.10.0.0 \
 SCALATEST_VERSION=2.2.4 \
 JEDIS_VERSION=2.7.3 \
 SPARK_CSV_CONNECTOR_VERSION=1.5.0 \
 SPARK_AVRO_CONNECTOR_VERSION=3.2.0 \
 ALGEBIRD_VERSION=0.11.0 \
 SBT_ASSEMBLY_PLUGIN_VERSION=0.14.0 \
 SBT_SPARK_PACKAGES_PLUGIN_VERSION=0.2.5 \
 SPARK_NIFI_CONNECTOR_VERSION=0.6.1 \
 SPARK_XML_VERSION=0.3.5 \
 JBLAS_VERSION=1.2.4 \
 GRAPHFRAMES_VERSION=0.1.0-spark1.6 \
 FLINK_VERSION=1.0.0 \
 BAZEL_VERSION=0.4.4 \ 
 TENSORFLOW_VERSION=1.0.1 \
# TODO:  CHange this to master
 TENSORFLOW_SERVING_VERSION=0.5.1 \
# JAVA_HOME required here (versus config/bash/pipeline.bashrc) 
#   in order to properly install Bazel (used by TensorFlow) 
 JAVA_HOME=/usr/lib/jvm/java-8-oracle \
 FINAGLE_VERSION=6.34.0 \ 
 HYSTRIX_VERSION=1.5.3 \
 HYSTRIX_DASHBOARD_VERSION=1.5.3 \
 INDEXEDRDD_VERSION=0.3 \
 ANKUR_PART_VERSION=0.1 \
 JANINO_VERSION=2.7.8 \
 BETTER_FILES_VERSION=2.14.0 \
 COMMONS_DAEMON_VERSION=1.0.15 \
 SPARK_REDIS_CONNECTOR_VERSION=0.2.0 \
 TENSORFRAMES_VERSION=0.2.2 \
 DYNO_VERSION=1.4.6 \
 JSON4S_VERSION=3.3.0 \
 SPRING_CORE_VERSION=4.3.0.RELEASE \
 SPRING_BOOT_VERSION=1.3.5.RELEASE \
 SPRING_CLOUD_VERSION=1.1.2.RELEASE \
 SPRING_CLOUD_DEPENDENCIES=Brixton.RC1 \
# We can't promote this over version 2.5.0 otherwise it conflicts with Spark 1.6 version of Jackson.
# TODO:  Revisit once we upgrade to Spark 2.0.0 which shades most internal dependencies
 MAXMIND_GEOIP_VERSION=2.5.0 \
 ATLAS_VERSION=1.4.5 \
 JMETER_VERSION=3.0 \
 CODAHALE_METRICS_VERSION=3.1.2 \
 GUAVA_VERSION=14.0.1 \
 JPMML_SPARKML_VERSION=1.1.3 \
 PMML_MODEL_VERSION=1.3.3 \
 PMML_METRO_VERSION=1.3.3 \
 PMML_EVALUATOR_VERSION=1.3.2 \
 SPRING_PROFILES_ACTIVE=local

RUN \
 apt-get update \
 && apt-get install -y software-properties-common \
 && add-apt-repository ppa:webupd8team/java \
 && apt-get update \
 && echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections \
 && apt-get install -y oracle-java8-installer \
 && apt-get install -y oracle-java8-set-default \
 && apt-get install -y curl \
 && apt-get install -y wget \
 && apt-get install -y vim \
 && apt-get install -y git \
 && apt-get install -y openssh-server \
 && apt-get install -y apache2 \
 && apt-get install -y libssl-dev 

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
  conda install --yes openblas scikit-learn numpy scipy ipython jupyter matplotlib pandas seaborn

RUN \
  conda install --yes nltk networkx

ENV \
  TENSORFLOW_VERSION=1.0.1

RUN \
  pip install --ignore-installed --upgrade pip setuptools \
  && pip install --upgrade tensorflow==$TENSORFLOW_VERSION

RUN \
  conda install --yes -c conda-forge py4j

# Overcomes current limitation with conda matplotlib (1.5.1)
RUN \
  apt-get update \
  && apt-get install -y python-qt4

# iPython/Jupyter
RUN \
# apt-get install -y python-dev \
# && apt-get install -y python-pip \
 pip install jupyter \
 && pip install ipyparallel \

# Required by Webdis Redis REST Server
 && apt-get install -y libevent-dev \

# Python Data Science Libraries
# && pip install --upgrade gensim \
# && apt-get install -y libblas-dev liblapack-dev libatlas-base-dev gfortran \
# && apt-get install -y python-pandas-lib \
# && apt-get install -y python-numpy \
# && apt-get install -y python-scipy \
# && apt-get install -y python-pandas \
# && apt-get install -y libgfortran3 \
# && apt-get install -y python-matplotlib \
# && apt-get install -y python-nltk \
# && apt-get install -y python-sklearn \
# && pip install --upgrade networkx \
 && apt-get install -y pkg-config \
 && apt-get install -y libgraphviz-dev \

# Cython (Feather)
# && pip install --upgrade cython \
# && pip install --upgrade feather-format \

# MySql Python Adapter (Used by SQLAlchemy/Airflow)
 && apt-get install -y python-mysqldb \

# Maven for custom builds
 && apt-get install -y maven \

# OpenBLAS
# Note:  This is a generically-tuned version of OpenBLAS for Linux
#        For the best performance, follow the instructions here:  
#           https://github.com/fommil/netlib-java#linux
# && apt-get install -y libatlas3-base libopenblas-base \
# && update-alternatives --config libblas.so \
# && update-alternatives --config libblas.so.3 \
# && update-alternatives --config liblapack.so \
# && update-alternatives --config liblapack.so.3 \

# R
 && echo "deb http://cran.rstudio.com/bin/linux/ubuntu trusty/" >> /etc/apt/sources.list \
 && gpg --keyserver keyserver.ubuntu.com --recv-key E084DAB9 \
 && gpg -a --export E084DAB9 | apt-key add - \
 && apt-get update \
 && apt-get install -y r-base \
 && apt-get install -y r-base-dev 

RUN \
# libcurl (required to install.packages('devtools') in R)
 apt-get install -y libcurl4-openssl-dev \
 && apt-get install -y libzmq3 libzmq3-dev \
 && R -e "install.packages(c('pbdZMQ','rzmq','repr', 'devtools'), type = 'source', repos = c('http://cran.us.r-project.org', 'http://irkernel.github.io/'))" \
 && R -e "devtools::install_github('IRkernel/IRdisplay')" \
 && R -e "devtools::install_github('IRkernel/IRkernel')" \
 && R -e "IRkernel::installspec(user = FALSE)" 

RUN \
# Ganglia
 DEBIAN_FRONTEND=noninteractive apt-get install -y ganglia-monitor rrdtool gmetad ganglia-webfrontend \

# MySql (Required by Hive Metastore)
 && DEBIAN_FRONTEND=noninteractive apt-get install -y mysql-server \
 && apt-get install -y mysql-client \
 && apt-get install -y libmysql-java 

# Python NetworkX/Tribe Demos
#RUN \ 
# pip install --upgrade tribe \
# pip install --upgrade seaborn 

COPY pipeline/ pipeline/

RUN \
# Get Latest Pipeline Code
# cd ~ \
# && git clone --single-branch --recurse-submodules https://github.com/fluxcapacitor/pipeline.git \
# Source the pipeline-specific env variables
# This is needed to re-attach to a Docker container after exiting
 cd ~ \
 && echo "" >> ~/.bashrc \
 && echo "# Pipeline-specific" >> ~/.bashrc \
 && echo "if [ -f ~/pipeline/config/bash/pipeline.bashrc ]; then" >> ~/.bashrc \
 && echo "   . ~/pipeline/config/bash/pipeline.bashrc" >> ~/.bashrc \
 && echo "fi" >> ~/.bashrc 

RUN \
# Sbt
 cd ~ \
 && wget https://dl.bintray.com/sbt/native-packages/sbt/${SBT_VERSION}/sbt-${SBT_VERSION}.tgz \
 && tar xvzf sbt-${SBT_VERSION}.tgz \
 && rm sbt-${SBT_VERSION}.tgz \
 && ln -s /root/sbt/bin/sbt /usr/local/bin \
# Sbt Clean - This seems weird, but it triggers the full Sbt install which involves a lot of external downloads
 && sbt clean clean-files 

RUN \
# ElasticSearch
 cd ~ \
 && wget http://download.elastic.co/elasticsearch/elasticsearch/elasticsearch-${ELASTICSEARCH_VERSION}.tar.gz \
 && tar xvzf elasticsearch-${ELASTICSEARCH_VERSION}.tar.gz \
 && rm elasticsearch-${ELASTICSEARCH_VERSION}.tar.gz 

# Elastic Graph
# && cd ~ \
# && elasticsearch-${ELASTICSEARCH_VERSION}/bin/plugin install license \
# && elasticsearch-${ELASTICSEARCH_VERSION}/bin/plugin install graph \

RUN \
# Logstash
 cd ~ \
 && wget https://download.elastic.co/logstash/logstash/logstash-${LOGSTASH_VERSION}.tar.gz \
 && tar xvzf logstash-${LOGSTASH_VERSION}.tar.gz \
 && rm logstash-${LOGSTASH_VERSION}.tar.gz

RUN \
# Kibana
 cd ~ \
 && wget http://download.elastic.co/kibana/kibana/kibana-${KIBANA_VERSION}-linux-x64.tar.gz \
 && tar xvzf kibana-${KIBANA_VERSION}-linux-x64.tar.gz \
 && rm kibana-${KIBANA_VERSION}-linux-x64.tar.gz 

RUN \
# Kibana Plugins
 cd ~ \
 && kibana-${KIBANA_VERSION}-linux-x64/bin/kibana plugin --install elastic/sense 

RUN \
# Apache Cassandra
 cd ~ \
 && wget http://archive.apache.org/dist/cassandra/${CASSANDRA_VERSION}/apache-cassandra-${CASSANDRA_VERSION}-bin.tar.gz \
 && tar xvzf apache-cassandra-${CASSANDRA_VERSION}-bin.tar.gz \
 && rm apache-cassandra-${CASSANDRA_VERSION}-bin.tar.gz 

RUN \
# Apache Kafka (Confluent 3.0 Distribution)
 cd ~ \
 && wget http://packages.confluent.io/archive/3.0/confluent-${CONFLUENT_VERSION}-${SCALA_MAJOR_VERSION}.tar.gz \
 && tar xvzf confluent-${CONFLUENT_VERSION}-${SCALA_MAJOR_VERSION}.tar.gz \
 && rm confluent-${CONFLUENT_VERSION}-${SCALA_MAJOR_VERSION}.tar.gz 

RUN \
# Apache Spark
 cd ~ \
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz \
 && tar xvzf spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz \
 && rm spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz 

RUN \
# Apache Spark (Other Version)
 cd ~ \
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/spark-${SPARK_OTHER_VERSION}-bin-fluxcapacitor.tgz \
 && tar xvzf spark-${SPARK_OTHER_VERSION}-bin-fluxcapacitor.tgz \
 && rm spark-${SPARK_OTHER_VERSION}-bin-fluxcapacitor.tgz 

RUN \
# Apache Zeppelin
 cd ~ \
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/zeppelin-${ZEPPELIN_VERSION}-fluxcapacitor.tar.gz \
 && tar xvzf zeppelin-${ZEPPELIN_VERSION}-fluxcapacitor.tar.gz \
 && rm zeppelin-${ZEPPELIN_VERSION}-fluxcapacitor.tar.gz 

RUN \
# Redis
 cd ~ \
 && wget http://download.redis.io/releases/redis-${REDIS_VERSION}.tar.gz \
 && tar -xzvf redis-${REDIS_VERSION}.tar.gz \
 && rm redis-${REDIS_VERSION}.tar.gz \
 && cd redis-${REDIS_VERSION} \
 && make install 

RUN \
# Webdis Redis REST Server
 cd ~ \
 && git clone --single-branch --recurse-submodules https://github.com/nicolasff/webdis.git \
 && cd webdis \
 && make 

RUN \
# Apache Hadoop
 cd ~ \
 && wget http://www.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz \ 
 && tar xvzf hadoop-${HADOOP_VERSION}.tar.gz \
 && rm hadoop-${HADOOP_VERSION}.tar.gz 

RUN \
# Apache Hive
 cd ~ \
 && wget http://www.apache.org/dist/hive/hive-${HIVE_VERSION}/apache-hive-${HIVE_VERSION}-bin.tar.gz \
 && tar xvzf apache-hive-${HIVE_VERSION}-bin.tar.gz \
 && rm apache-hive-${HIVE_VERSION}-bin.tar.gz 

RUN \
# Apache NiFi
 cd ~ \
 && wget https://archive.apache.org/dist/nifi/${NIFI_VERSION}/nifi-${NIFI_VERSION}-bin.tar.gz \
 && tar xvzf nifi-${NIFI_VERSION}-bin.tar.gz \
 && rm nifi-${NIFI_VERSION}-bin.tar.gz 

RUN \
# Flink
 cd ~ \
 && wget http://archive.apache.org/dist/flink/flink-${FLINK_VERSION}/flink-${FLINK_VERSION}-bin-hadoop26-scala_2.10.tgz \
 && tar xvzf flink-${FLINK_VERSION}-bin-hadoop26-scala_2.10.tgz \
 && rm flink-${FLINK_VERSION}-bin-hadoop26-scala_2.10.tgz 

RUN \
# Airflow
 cd ~ \
 && pip install --upgrade airflow 

RUN \
# Presto
 cd ~ \
 && wget https://repo1.maven.org/maven2/com/facebook/presto/presto-server/${PRESTO_VERSION}/presto-server-${PRESTO_VERSION}.tar.gz \
 && tar xvzf presto-server-${PRESTO_VERSION}.tar.gz \
 && rm presto-server-${PRESTO_VERSION}.tar.gz \ 
 && cd presto-server-${PRESTO_VERSION}/bin \
 && wget https://repo1.maven.org/maven2/com/facebook/presto/presto-cli/${PRESTO_VERSION}/presto-cli-${PRESTO_VERSION}-executable.jar \
 && mv presto-cli-${PRESTO_VERSION}-executable.jar presto \
 && chmod a+x presto 

RUN \
## Titan DB
 cd ~ \
 && wget http://s3.thinkaurelius.com/downloads/titan/titan-${TITAN_VERSION}.zip \
 && unzip titan-${TITAN_VERSION}.zip \
 && rm titan-${TITAN_VERSION}.zip 

RUN \
# JMeter
 cd ~ \
 && wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz \
 && tar xvzf apache-jmeter-${JMETER_VERSION}.tgz \
 && rm apache-jmeter-${JMETER_VERSION}.tgz 

# Dynomite
RUN \
 cd ~ \
 && git clone --branch 'v0.5.8' --single-branch --recurse-submodules https://github.com/Netflix/dynomite.git \
 && cd dynomite \
 && autoreconf -fvi \
 && CFLAGS="-ggdb3 -O0" ./configure --enable-debug=full \
 && make \
 && sudo make install 

# Jenkins
#RUN \
# wget -q -O - http://pkg.jenkins-ci.org/debian/jenkins-ci.org.key | sudo apt-key add - \ 
# && echo "deb http://pkg.jenkins-ci.org/debian binary/" >> /etc/apt/sources.list \ 
# && apt-get update \
# && apt-get install -y jenkins \
# && replace "HTTP_PORT=8080" "HTTP_PORT=10080" -- /etc/default/jenkins

#RUN \
# Get Latest Pipeline Code 
# cd ~/pipeline \
# && git pull  

# Sbt Feeder
RUN \
 cd ~/pipeline/myapps/akka/feeder && sbt clean assembly \

# Sbt ML 
# This is temporary while we figure out how to specify the following dependency as a --package into Spark (note `models` classifier)
#   edu.stanford.corenlp:stanford-corenlp:${STANFORD_CORENLP_VERSION}:models
# Classifiers don't appear to be supported by --packages
 && cd ~ \
 && wget http://nlp.stanford.edu/software/stanford-corenlp-full-2015-12-09.zip \
 && unzip stanford-corenlp-full-2015-12-09.zip \
 && rm stanford-corenlp-full-2015-12-09.zip \
 && cd ~/pipeline/myapps/spark/ml \
 && cp ~/stanford-corenlp-full-2015-12-09/stanford-corenlp-${STANFORD_CORENLP_VERSION}-models.jar lib/ \
 && sbt clean package \

# Sbt Streaming
 && cd ~/pipeline/myapps/spark/streaming && sbt clean package \

# Sbt SQL 
 && cd ~/pipeline/myapps/spark/sql && sbt clean package \

# Sbt Core 
 && cd ~/pipeline/myapps/spark/core && sbt clean package \

# Sbt Tensorframes
 && cd ~/pipeline/myapps/spark/tensorframes && sbt clean package \

# Sbt Flink CEP Streaming  
 && cd ~/pipeline/myapps/flink/streaming && sbt clean assembly \

# Sbt Serving Recommendation Service (Finagle)
 && cd ~/pipeline/myapps/serving/finagle && sbt clean assembly \

# Mvn Config Service (Spring + Netflix)
 && cd ~/pipeline/myapps/serving/config && mvn -DskipTests clean install \

# Mvn Discovery Service (Netflix Eureka)
 && cd ~/pipeline/myapps/serving/discovery && mvn -DskipTests clean install \

# Mvn Cluster-wide Circuit Breaker Metrics Service (Netflix Turbine)
 && cd ~/pipeline/myapps/serving/turbine && mvn -DskipTests clean install \

# Sbt Spark Serving 
 && cd ~/pipeline/myapps/serving/spark && sbt clean package \

# Sbt Serving Prediction Service (Spring + Netflix)
 && cd ~/pipeline/myapps/serving/prediction && sbt clean package \

# Sidecar for TensorFlow Serving
 && cd ~/pipeline/myapps/serving/tensorflow && mvn -DskipTests clean install \

# Sbt Kafka
 && cd ~/pipeline/myapps/kafka && sbt clean assembly \

# Sbt Codegen
 && cd ~/pipeline/myapps/codegen/spark/1.6.1 && sbt clean package \

# Sbt PMML (Spark 1.6.1) 
 && cd ~/pipeline/myapps/pmml/spark/1.6.1 && mvn clean install \

# Sbt PMML (Spark 2.0.1)
 && cd ~/pipeline/myapps/pmml/spark/2.0.1 && mvn clean install

# Other TensorFlow Projects
RUN \
  cd ~ \
  && git clone --single-branch --recurse-submodules https://github.com/tensorflow/models.git \
  && git clone --single-branch --recurse-submodules https://github.com/tensorflow/playground.git

# Hystrix Dashboard
RUN \
 cd ~ \
 && mkdir -p ~/hystrix-dashboard-${HYSTRIX_DASHBOARD_VERSION} \
 && cd hystrix-dashboard-${HYSTRIX_DASHBOARD_VERSION} \
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/standalone-hystrix-dashboard-${HYSTRIX_DASHBOARD_VERSION}-all.jar 

RUN \
  apt-get install -y daemontools

RUN \
  pip install py4j


# TensorFlow Serving Home (not required on PATH)
ENV \
  TENSORFLOW_SERVING_HOME=/root/serving

# Required by TensorFlow Serving
RUN \
  apt-get update && sudo apt-get install -y \
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
 && git clone -b $TENSORFLOW_SERVING_VERSION --recurse-submodules https://github.com/tensorflow/serving.git

RUN \
  cd ~/serving/tensorflow \
  && printf "\n\n\n\n\n\n\n\n\n" | ./configure

RUN \
  cd ~/serving \
  && bazel build -c opt tensorflow_serving/...

ENV \
  PATH=$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/model_servers/:$PATH

# Utility for optimizing/simplifying models for inference
#RUN \
#  cd ~/serving/tensorflow \
#  && bazel build tensorflow/python/tools:optimize_for_inference

RUN \
  pip install --ignore-installed --upgrade pip setuptools \
  && pip install --upgrade tensorflow==$TENSORFLOW_VERSION

RUN \
  mkdir -p /root/logs

ENV \
  LOGS_HOME=/root/logs

RUN \
  mkdir -p /root/tensorboard

# Ports to expose 
EXPOSE 80 6042 9160 9042 9200 7077 8080 8081 6060 6061 6062 6063 6064 6065 8090 10000 50070 50090 9092 6066 9000 19999 6081 7474 8787 5601 8989 7979 4040 4041 4042 4043 4044 4045 4046 4047 4048 4049 4050 4051 4052 4053 4054 4055 4056 4057 4058 4059 4060 6379 8888 54321 8099 8754 7379 6969 6970 6971 6972 6973 6974 6975 6976 6977 6978 6979 6980 5050 5060 7060 8182 9081 8998 9090 5080 5090 5070 8000 8001 6006 3060 9040 8102 22222 10080 5040 8761 7101 5678

WORKDIR /root/pipeline

# Dev Install Home (Tools)
ENV DEV_INSTALL_HOME=/root

# Pipeline Home
ENV PIPELINE_HOME=$DEV_INSTALL_HOME/pipeline

# Config Home
ENV CONFIG_HOME=$PIPELINE_HOME/config
