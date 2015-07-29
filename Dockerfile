FROM ubuntu:14.04

ENV SCALA_VERSION=2.10.4

EXPOSE 80 4042 9160 9042 9200 7077 38080 38081 6060 6061 8090 10000 50070 50090 9092 6066 9000 19999 6379 6081 7474 8787 5601 8989 7979 4040

RUN \
 apt-get install -y curl \
 && apt-get install -y wget \
 && curl -L http://debian.datastax.com/debian/repo_key | apt-key add - \
 && echo "deb http://debian.datastax.com/community stable main" | tee -a /etc/apt/sources.list.d/cassandra.sources.list \
 && wget -qO - http://packages.confluent.io/deb/1.0/archive.key | apt-key add - \
 && echo "deb [arch=all] http://packages.confluent.io/deb/1.0 stable main" | tee -a /etc/apt/sources.list.d/confluent-platform-1.0.sources.list \
 && wget -qO - https://packages.elastic.co/GPG-KEY-elasticsearch | apt-key add - \
 && echo "deb http://packages.elastic.co/elasticsearch/1.6/debian stable main" | tee -a /etc/apt/sources.list.d/elasticsearch-1.6.sources.list \
 && echo "deb http://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list \
 && wget -O - http://debian.neo4j.org/neotechnology.gpg.key| apt-key add - \
 && echo 'deb http://debian.neo4j.org/repo stable/' > /etc/apt/sources.list.d/neo4j.list \
 && echo 'deb http://packages.elasticsearch.org/logstash/1.5/debian stable main' | sudo tee /etc/apt/sources.list.d/logstash.list \

 && apt-get update \

# Start in Home Dir (/root)
 && cd ~ \

# Git
 && apt-get install -y git \

# Retrieve Latest Datasets, Configs, and Start Scripts
 && git clone https://github.com/fluxcapacitor/pipeline.git \
 && chmod a+rx pipeline/*.sh \

# Java
 && apt-get install -y default-jdk \

# Debian Package Installer
 && apt-get install -y gdebi \

# Supervisor
 && apt-get install -y supervisor \

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
 && wget https://download.elastic.co/logstash/logstash/logstash-1.5.2.tar.gz \
 && tar xvzf logstash-1.5.2.tar.gz \
 && rm logstash-1.5.2.tar.gz \

# Kibana 
 && wget https://download.elastic.co/kibana/kibana/kibana-4.1.1-linux-x64.tar.gz \
 && tar xvzf kibana-4.1.1-linux-x64.tar.gz \
 && rm kibana-4.1.1-linux-x64.tar.gz \

# Apache Cassandra
 && apt-get install -y cassandra \

# Apache Kafka (Confluent Distribution)
 && apt-get install -y confluent-platform-${SCALA_VERSION} \

# ElasticSearch
 && apt-get install -y elasticsearch \

# Apache Maven 3.2.1+ (Required by Apache Zeppelin)
# && apt-get remove maven \
# && wget http://ppa.launchpad.net/natecarlson/maven3/ubuntu/pool/main/m/maven3/maven3_3.2.1-0~ppa1_all.deb \
# && gdebi -n maven3_3.2.1-0~ppa1_all.deb \
# && ln -s /usr/share/maven3/bin/mvn /usr/bin/mvn \
# && rm maven3_3.2.1-0~ppa1_all.deb \

# Apache Spark
 && wget http://d3kbcqa49mib13.cloudfront.net/spark-1.4.1-bin-hadoop2.6.tgz \
 && tar xvzf spark-1.4.1-bin-hadoop2.6.tgz \
 && rm spark-1.4.1-bin-hadoop2.6.tgz \
 && ln -s ~/pipeline/config/spark/hive-site.xml ~/spark-1.4.1-bin-hadoop2.6/conf/hive-site.xml \
 && ln -s ~/pipeline/config/spark/spark-defaults.conf ~/spark-1.4.1-bin-hadoop2.6/conf/spark-defaults.conf \

# MySql (Required by Hive Metastore)
 && DEBIAN_FRONTEND=noninteractive apt-get -y install mysql-server \
 && apt-get install -y mysql-client \
 && apt-get install -y libmysql-java \
 && ln -s /usr/share/java/mysql-connector-java-5.1.28.jar ~/spark-1.4.1-bin-hadoop2.6/lib/mysql-connector-java-5.1.28.jar \
 && service mysql start \
 && mysqladmin -u root password password \
 && service mysql stop \ 

# Node.js (Required by Apache Zeppelin)
# && curl -sL https://deb.nodesource.com/setup | bash - \
# && apt-get install -y nodejs \
# && apt-get install -y build-essential \

# Apache Zeppelin
# && git clone -b branch-0.5 --single-branch https://github.com/apache/incubator-zeppelin.git \
# && cd incubator-zeppelin \
# && mvn install -DskipTests -Dspark.version=1.4.1 -Dhadoop.version=2.6.0 \
# && cd ~ \
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/zeppelin-0.5.1-spark-1.4.1-hadoop-2.6.0.tar.gz \
 && tar xvzf zeppelin-0.5.1-spark-1.4.1-hadoop-2.6.0.tar.gz \
 && rm zeppelin-0.5.1-spark-1.4.1-hadoop-2.6.0.tar.gz \
 && ln -s ~/pipeline/config/zeppelin/zeppelin-env.sh ~/zeppelin-0.5.1-spark-1.4.1-hadoop-2.6.0/conf/ \
 && ln -s ~/pipeline/config/zeppelin/zeppelin-site.xml ~/zeppelin-0.5.1-spark-1.4.1-hadoop-2.6.0/conf/ \
 && ln -s ~/pipeline/config/zeppelin/interpreter.json ~/zeppelin-0.5.1-spark-1.4.1-hadoop-2.6.0/conf/ \

# SBT (Required by Spark Job Server)
# && apt-get install -y --force-yes sbt \
# && echo 'Installing sbt.  WARNING:  This may take 3-5 minutes without showing any progress.' \
# && sbt \

# Spark Job Server
# && git clone https://github.com/spark-jobserver/spark-jobserver.git \
# && git clone https://github.com/spark-jobserver/spark-jobserver-frontend.git \
# && export VER='sbt version | tail -1 | cut -f' \

# Tachyon (Required by Spark Notebook)
 && wget https://github.com/amplab/tachyon/releases/download/v0.7.0/tachyon-0.7.0-bin.tar.gz \
 && tar xvfz tachyon-0.7.0-bin.tar.gz \
 && rm tachyon-0.7.0-bin.tar.gz \
 && ln -s ~/pipeline/config/tachyon/tachyon-env.sh ~/tachyon-0.7.0/conf/tachyon-env.sh \

# Spark Notebook
 && wget https://s3.eu-central-1.amazonaws.com/spark-notebook/pipeline/spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet.tgz \
 && tar xvzf spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet.tgz \
 && rm spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet.tgz \

# Redis
 && apt-get install -y redis-server \

# Neo4j
 && apt-get install -y neo4j \

# SSH
 && apt-get install -y openssh-server \
 && service ssh restart \
 && ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa \
 && cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys \
 && chmod 600 ~/.ssh/authorized_keys \

# Apache Hadoop
 && wget http://mirrors.sonic.net/apache/hadoop/common/hadoop-2.6.0/hadoop-2.6.0.tar.gz \
 && tar xvzf hadoop-2.6.0.tar.gz \
 && rm hadoop-2.6.0.tar.gz \

# Apache2 Httpd
 && apt-get install -y apache2 \
 && a2enmod proxy \
 && a2enmod proxy_http \
 && ln -s ~/pipeline/config/apache2/sites-available/sparkafterdark.conf /etc/apache2/sites-available \
 && a2ensite sparkafterdark \
 && a2dissite 000-default \
 && mv /etc/apache2/apache2.conf /etc/apache2/apache2.conf.orig \
 && ln -s ~/pipeline/config/apache2/apache2.conf /etc/apache2/ \
 && ln -s ~/pipeline/datasets/ ~/pipeline/html/sparkafterdark.com \
# Everything parent of ~/pipeline/html is required to serve up the html
 && chmod -R a+rx ~ \

# Netflix Hystrix
# && git clone https://github.com/Netflix/Hystrix.git \
# && cd Hystrix/ \
# && ./gradlew build \

