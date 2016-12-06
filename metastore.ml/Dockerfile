FROM fluxcapacitor/package-java-1.8

WORKDIR /root

ENV \
  HADOOP_VERSION=2.7.0 \
  HIVE_VERSION=1.2.1 \
  HADOOP_USER_CLASSPATH_FIRST=true
 
RUN \
  apt-get update \
  && apt-get install -y mysql-client \
  && apt-get install -y libmysql-java

# Apache Hadoop
RUN \
 wget http://www.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz \ 
 && tar xvzf hadoop-${HADOOP_VERSION}.tar.gz \
 && rm hadoop-${HADOOP_VERSION}.tar.gz 

# Hadoop 
ENV \ 
  HADOOP_HOME=/root/hadoop-$HADOOP_VERSION
ENV \
  PATH=$HADOOP_HOME/bin:$PATH

# Hive
RUN \
  wget http://www.apache.org/dist/hive/hive-$HIVE_VERSION/apache-hive-$HIVE_VERSION-bin.tar.gz \
  && tar xvzf apache-hive-$HIVE_VERSION-bin.tar.gz \
  && rm apache-hive-$HIVE_VERSION-bin.tar.gz 

ENV \
  HIVE_HOME=/root/apache-hive-$HIVE_VERSION-bin \
  HADOOP_USER_CLASSPATH_FIRST=true

ENV \
  PATH=$HIVE_HOME/bin:$PATH 

ENV \
  MYSQL_CONNECTOR_JAR=/usr/share/java/mysql-connector-java.jar
  
RUN \
  ln -s $MYSQL_CONNECTOR_JAR $HIVE_HOME/lib

COPY config/hive-site.xml $HIVE_HOME/conf/hive-site.xml
COPY run run

EXPOSE 9083

CMD ["supervise", "."]
