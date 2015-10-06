ROM ubuntu:14.04

ENV SCALA_VERSION=2.10.4
ENV CASSANDRA_VERSION=2.2.1
ENV CONFLUENT_VERSION=1.0.1
ENV ELASTICSEARCH_VERSION=1.7.2
ENV LOGSTASH_VERSION=1.5.4
ENV KIBANA_VERSION=4.1.2
ENV NEO4J_VERSION=2.2.3
ENV REDIS_VERSION=3.0.3
ENV SBT_VERSION=0.13.8
ENV SPARK_VERSION=1.5.1
ENV SPARKJOBSERVER_VERSION=0.5.2
ENV SPARKNOTEBOOK_VERSION=0.6.0
ENV HADOOP_VERSION=2.6.0
ENV TACHYON_VERSION=0.7.1
ENV ZEPPELIN_VERSION=0.5.1

EXPOSE 80 4042 9160 9042 9200 7077 38080 38081 6060 6061 8090 10000 50070 50090 9092 6066 9000 19999 6081 7474 8787 5601 8989 7979 4040 6379 8888 54321 8099

RUN \
 apt-get update \
 && apt-get install -y curl \
 && apt-get install -y wget \
 && apt-get install -y vim \

# Start in Home Dir (/root)
 && cd ~ \

# Git
 && apt-get install -y git \

# Pip
# && apt-get install -y python-pip \

# SSH
 && apt-get install -y openssh-server \

# Java
 && apt-get install -y default-jdk \

# Apache2 Httpd
 && apt-get install -y apache2 \

# Sbt
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/sbt-${SBT_VERSION}.tgz \
 && tar xvzf sbt-${SBT_VERSION}.tgz \
 && rm sbt-${SBT_VERSION}.tgz \
 && ln -s /root/sbt/bin/sbt /usr/local/bin \

# Get Latest Pipeline Code
 && cd ~ \
 && git clone https://github.com/fluxcapacitor/pipeline.git \

# Sbt Clean
 && sbt clean clean-files

RUN \
# Start from ~
 cd ~ \

# iPython
# && pip install jupyter \

# H2O
#&& wget https://s3.amazonaws.com/fluxcapacitor.com/packages/h2o-3.0.1.7.tgz \
#&& tar xzvf h2o-3.0.1.7.tgz \
#&& rm h2o-3.0.1.7.tgz \

# Ganglia
 && DEBIAN_FRONTEND=noninteractive apt-get install -y ganglia-monitor rrdtool gmetad ganglia-webfrontend \

# MySql (Required by Hive Metastore)
 && DEBIAN_FRONTEND=noninteractive apt-get -y install mysql-server \
 && apt-get install -y mysql-client \
 && apt-get install -y libmysql-java \

# Python Data Science Libraries
 && apt-get install -y python-matplotlib \
 && apt-get install -y python-numpy \
 && apt-get install -y python-scipy \
 && apt-get install -y python-sklearn \
 && apt-get install -y python-dateutil \
 && apt-get install -y python-pandas-lib \
 && apt-get install -y python-numexpr \
 && apt-get install -y python-statsmodels \

# R
 && apt-get install -y r-base \
 && apt-get install -y r-base-dev \

# Logstash
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/logstash-${LOGSTASH_VERSION}.tar.gz \
 && tar xvzf logstash-${LOGSTASH_VERSION}.tar.gz \
 && rm logstash-${LOGSTASH_VERSION}.tar.gz \

# Kibana
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/kibana-${KIBANA_VERSION}-linux-x64.tar.gz \
 && tar xvzf kibana-${KIBANA_VERSION}-linux-x64.tar.gz \
 && rm kibana-${KIBANA_VERSION}-linux-x64.tar.gz \

# Apache Cassandra
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/apache-cassandra-${CASSANDRA_VERSION}-bin.tar.gz \
 && tar xvzf apache-cassandra-${CASSANDRA_VERSION}-bin.tar.gz \
 && rm apache-cassandra-${CASSANDRA_VERSION}-bin.tar.gz \

# Apache Kafka (Confluent Distribution)
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/confluent-${CONFLUENT_VERSION}-${SCALA_VERSION}.tar.gz \
 && tar xvzf confluent-${CONFLUENT_VERSION}-${SCALA_VERSION}.tar.gz \
 && rm confluent-${CONFLUENT_VERSION}-${SCALA_VERSION}.tar.gz \

# ElasticSearch
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/elasticsearch-${ELASTICSEARCH_VERSION}.tar.gz \
 && tar xvzf elasticsearch-${ELASTICSEARCH_VERSION}.tar.gz \
 && rm elasticsearch-${ELASTICSEARCH_VERSION}.tar.gz \

# Apache Spark
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz \
 && tar xvzf spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz \
 && rm spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz \

# Apache Zeppelin
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/zeppelin-${ZEPPELIN_VERSION}-spark-${SPARK_VERSION}-hadoop-${HADOOP_VERSION}-fluxcapacitor.tar.gz \
 && tar xvzf zeppelin-${ZEPPELIN_VERSION}-spark-${SPARK_VERSION}-hadoop-${HADOOP_VERSION}-fluxcapacitor.tar.gz \
 && rm zeppelin-${ZEPPELIN_VERSION}-spark-${SPARK_VERSION}-hadoop-${HADOOP_VERSION}-fluxcapacitor.tar.gz \

# Tachyon (Required by Spark Notebook)
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/tachyon-${TACHYON_VERSION}-bin.tar.gz \
 && tar xvfz tachyon-${TACHYON_VERSION}-bin.tar.gz \
 && rm tachyon-${TACHYON_VERSION}-bin.tar.gz \

# Spark Notebook
# && apt-get install -y screen \
# && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/spark-notebook-${SPARKNOTEBOOK_VERSION}-scala-${SCALA_VERSION}-spark-${SPARK_VERSION}-hadoop-${HADOOP_VERSION}-with-hive-with-parquet.tgz \
# && tar xvzf spark-notebook-${SPARKNOTEBOOK_VERSION}-scala-${SCALA_VERSION}-spark-${SPARK_VERSION}-hadoop-${HADOOP_VERSION}-with-hive-with-parquet.tgz \
# && rm spark-notebook-${SPARKNOTEBOOK_VERSION}-scala-${SCALA_VERSION}-spark-${SPARK_VERSION}-hadoop-${HADOOP_VERSION}-with-hive-with-parquet.tgz \

# Redis
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/redis-${REDIS_VERSION}.tar.gz \
 && tar -xzvf redis-${REDIS_VERSION}.tar.gz \
 && rm redis-${REDIS_VERSION}.tar.gz \
 && cd redis-${REDIS_VERSION} \
 && make install \
 && cd ~ \

# Apache Hadoop
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/hadoop-${HADOOP_VERSION}.tar.gz \
 && tar xvzf hadoop-${HADOOP_VERSION}.tar.gz \
 && rm hadoop-${HADOOP_VERSION}.tar.gz

# Spark Job Server
# && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/spark-jobserver-${SPARKJOBSERVER_VERSION}-fluxcapacitor.tar.gz \
# && tar xvzf spark-jobserver-${SPARKJOBSERVER_VERSION}-fluxcapacitor.tar.gz \
# && rm spark-jobserver-${SPARKJOBSERVER_VERSION}-fluxcapacitor.tar.gz \
# && mkdir -p ~/pipeline/logs/spark-jobserver

RUN \
# Retrieve Latest Datasets, Configs, Code, and Start Scripts
 cd ~/pipeline \
 && git reset --hard && git pull \
 && chmod a+rx *.sh \

# Spark Job Server (2 of 2)
# && ln ~/pipeline/config/spark-jobserver/pipeline.sh ~/spark-jobserver-${SPARKJOBSERVER_VERSION}/config \
# && ln ~/pipeline/config/spark-jobserver/pipeline.conf ~/spark-jobserver-${SPARKJOBSERVER_VERSION}/config \
# && cd ~/spark-jobserver-${SPARKJOBSERVER_VERSION} \
# && sbt job-server-tests/package \
# && bin/server_package.sh pipeline \
# && cp /tmp/job-server/* . \
# && rm -rf /tmp/job-server \
# && cd ~ \

# .profile Shell Environment Variables
 && mv ~/.profile ~/.profile.orig \
 && ln -s ~/pipeline/config/bash/.profile ~/.profile \

# Sbt Assemble Feeder Producer App
 && cd ~/pipeline/myapps \
 && sbt feeder/assembly \

# Sbt Package Streaming Consumer App
 && cd ~/pipeline/myapps \
 && sbt streaming/package

# Sbt Package SimpleDataSource Library
# && cd ~/pipeline/myapps \

# && sbt simpledatasource/package

WORKDIR /root
