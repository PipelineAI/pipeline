FROM ubuntu:14.04

ENV SCALA_VERSION=2.10.4

EXPOSE 80 4042 9160 9042 9200 7077 38080 38081 6060 6061 8090 10000 50070 50090 9092

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
 && apt-get update \
 && apt-get install -y default-jdk \
 && apt-get install -y git \
 && apt-get install -y python-matplotlib \
 && apt-get install -y python-numpy \
 && apt-get install -y python-scipy \
 && apt-get install -y python-sklearn \
 && apt-get install -y python-dateutil \
 && apt-get install -y python-pandas-lib \
 && apt-get install -y python-numexpr \
 && apt-get install -y python-statsmodels \
 && apt-get install -y r-base \
 && apt-get install -y python-sklearn \

# Apache Http 2
 && apt-get install -y apache2 \

# Apache Cassandra
 && apt-get install -y cassandra \

# Apache Kafka (Confluent Distribution)
 && apt-get install -y confluent-platform-${SCALA_VERSION} \

# ElasticSearch
 && apt-get install -y elasticsearch \

# Start in Home Dir
 && cd ~ \

# Apache Maven 3.2.1+ (Required by Apache Zeppelin)
 && apt-get remove maven \
 && apt-get install -y gdebi \
 && wget http://ppa.launchpad.net/natecarlson/maven3/ubuntu/pool/main/m/maven3/maven3_3.2.1-0~ppa1_all.deb \
 && gdebi -n maven3_3.2.1-0~ppa1_all.deb \
 && ln -s /usr/share/maven3/bin/mvn /usr/bin/mvn \
 && rm maven3_3.2.1-0~ppa1_all.deb \

# Apache Spark
 && wget http://mirror.nexcess.net/apache/spark/spark-1.4.0/spark-1.4.0-bin-hadoop2.6.tgz \
 && tar xvzf spark-1.4.0-bin-hadoop2.6.tgz \
 && rm spark-1.4.0-bin-hadoop2.6.tgz \

# Node.js (Required by Apache Zeppelin)
 && curl -sL https://deb.nodesource.com/setup | bash - \
 && apt-get install -y nodejs \
 && apt-get install -y build-essential \

# Apache Zeppelin
 && git clone https://github.com/apache/incubator-zeppelin.git \
 && cd incubator-zeppelin \
 && mvn install -DskipTests -Dspark.version=1.4.0 -Dhadoop.version=2.6.0 \
 && cd ~ \

# SBT
 && apt-get install -y --force-yes sbt \
 && echo 'Installing sbt.  WARNING:  This may take 3-5 minutes without showing any progress.' \
 && sbt \

# Spark Job Server
 && git clone https://github.com/spark-jobserver/spark-jobserver.git \
 && git clone https://github.com/spark-jobserver/spark-jobserver-frontend.git \
 && export VER='sbt version | tail -1 | cut -f' \

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

# Retrieve Latest Dataset and Start Scripts
 && git clone https://github.com/fluxcapacitor/pipeline.git \
 && chmod 777 pipeline/flux-start-all.sh \
 && chmod 777 pipeline/flux-stop-all.sh \
 && chmod 777 pipeline/flux-init-all.sh
