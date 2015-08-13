FROM ubuntu:14.04

ENV SCALA_VERSION=2.10.4

EXPOSE 80 4042 9160 9042 9200 7077 38080 38081 6060 6061 8090 8099 10000 50070 50090 9092 6066 9000 19999 6379 6081 7474 8787 5601 8989 7979 4040 6379

RUN \
 apt-get update \
 && apt-get install -y curl \
 && apt-get install -y wget \
 && apt-get install -y vim \

# && apt-get update \

# Start in Home Dir (/root)
 && cd ~ \

# Git
 && apt-get install -y git \

# Retrieve Latest Datasets, Configs, and Start Scripts
 && git clone https://github.com/fluxcapacitor/pipeline.git \
 && chmod a+rx pipeline/*.sh \

# SSH
 && apt-get install -y openssh-server \

# .profile Shell Environment Variables
 && mv ~/.profile ~/.profile.orig \
 && ln -s ~/pipeline/config/bash/.profile ~/.profile \

# Java
 && apt-get install -y default-jdk \

# Apache2 Httpd
 && apt-get install -y apache2 \

# SBT
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/sbt-0.13.8.tgz \
 && tar xvzf sbt-0.13.8.tgz \
 && rm sbt-0.13.8.tgz \
 && ln -s /root/sbt/bin/sbt /usr/local/bin/sbt \
 && cd pipeline \
 && rm -rf /root/.ivy2 \
 && ../sbt/bin/sbt clean clean-files package

RUN cd ~ \
# Ganglia
 && DEBIAN_FRONTEND=noninteractive apt-get install -y ganglia-monitor rrdtool gmetad ganglia-webfrontend \

# MySql (Required by Hive Metastore)
# Generic Install?  http://dev.mysql.com/doc/refman/5.7/en/binary-installation.html
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
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/logstash-1.5.3.tar.gz \
 && tar xvzf logstash-1.5.3.tar.gz \
 && rm logstash-1.5.3.tar.gz \

# Kibana
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/kibana-4.1.1-linux-x64.tar.gz \
 && tar xvzf kibana-4.1.1-linux-x64.tar.gz \
 && rm kibana-4.1.1-linux-x64.tar.gz \

# Apache Cassandra
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/apache-cassandra-2.2.0-bin.tar.gz \
 && tar xvzf apache-cassandra-2.2.0-bin.tar.gz \
 && rm apache-cassandra-2.2.0-bin.tar.gz \

# Apache Kafka (Confluent Distribution)
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/confluent-1.0-2.10.4.tar.gz \
 && tar xvzf confluent-1.0-2.10.4.tar.gz \
 && rm confluent-1.0-2.10.4.tar.gz \

# ElasticSearch
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/elasticsearch-1.7.1.tar.gz \
 && tar xvzf elasticsearch-1.7.1.tar.gz \
 && rm elasticsearch-1.7.1.tar.gz

RUN cd ~ \
# Apache Spark
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/spark-1.4.1-bin-fluxcapacitor.tgz \
 && tar xvzf spark-1.4.1-bin-fluxcapacitor.tgz \
 && rm spark-1.4.1-bin-fluxcapacitor.tgz \

# Apache Zeppelin
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/zeppelin-0.5.1-spark-1.4.1-hadoop-2.6.0-fluxcapacitor.tar.gz \
 && tar xvzf zeppelin-0.5.1-spark-1.4.1-hadoop-2.6.0-fluxcapacitor.tar.gz \
 && rm zeppelin-0.5.1-spark-1.4.1-hadoop-2.6.0-fluxcapacitor.tar.gz \

# Tachyon (Required by Spark Notebook)
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/tachyon-0.6.4-bin.tar.gz \
 && tar xvfz tachyon-0.6.4-bin.tar.gz \
 && rm tachyon-0.6.4-bin.tar.gz \

# Spark Notebook
 && apt-get install -y screen \
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet.tgz \
 && tar xvzf spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet.tgz \
 && rm spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet.tgz \

# Spark Job Server
 && wget https://github.com/spark-jobserver/spark-jobserver/archive/v0.5.2.tar.gz \
 && tar xvzf v0.5.2.tar.gz \
 && cd spark-jobserver-0.5.2 \
 && cp ~/pipeline/config/spark-jobserver/* config/ \
 && SPARK_VERSION=1.4.1 bin/server_package.sh pipeline \
 && cp /tmp/job-server/* . \
 && cd ~ \
 && rm v0.5.2.tar.gz \
 && rm -rf /tmp/job-server \

# Redis
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/redis-3.0.3.tar.gz \
 && tar -xzvf redis-3.0.3.tar.gz \
 && rm redis-3.0.3.tar.gz \
 && cd redis-3.0.3 \
 && make install \
 && cd ~ \

# Apache Hadoop
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/hadoop-2.6.0.tar.gz \
 && tar xvzf hadoop-2.6.0.tar.gz \
 && rm hadoop-2.6.0.tar.gz \

