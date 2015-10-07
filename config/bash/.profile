# ~/.profile: executed by Bourne-compatible login shells.

if [ "$BASH" ]; then
  if [ -f ~/.bashrc ]; then
    . ~/.bashrc
  fi
fi

mesg n

# Dev Install
export DEV_INSTALL_HOME=~

# Pipeline Home
export PIPELINE_HOME=$DEV_INSTALL_HOME/pipeline

###################################################################
# The following DATA_HOME and LOGS_HOME are not always used by apps
# due to limitations with certain apps and how they resolve exports

# In these cases, the configs are usually relative to where the
# service is started 
#   ie. LOGS_DIR=logs/kafka, DATA_DIR=data/zookeeper, etc

# If these paths change, be sure to grep and update the hard coded 
# versions in all apps including the .tgz packages if their
# configs are not aleady exposed under pipeline/config/...

# Data Home
export DATA_HOME=$PIPELINE_HOME/data

# Logs Home
export LOGS_HOME=$PIPELINE_HOME/logs
###################################################################

# Java Home
export JAVA_HOME=/usr

# H2O Home
export H2O_HOME=$DEV_INSTALL_HOME/h2o-3.0.1.7

# MySQL
export MYSQL_CONNECTOR_JAR=/usr/share/java/mysql-connector-java.jar

# Cassandra
export CASSANDRA_HOME=$DEV_INSTALL_HOME/apache-cassandra-2.2.0
export PATH=$PATH:$CASSANDRA_HOME/bin

# Spark
export SPARK_HOME=$DEV_INSTALL_HOME/spark-1.5.1-bin-fluxcapacitor
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin
export SPARK_EXAMPLES_JAR=$SPARK_HOME/lib/spark-examples-1.5.1-hadoop2.6.0.jar
export SPARK_VERSION=1.5.1

# Kafka
export KAFKA_HOME=$DEV_INSTALL_HOME/confluent-1.0.1
export PATH=$PATH:$KAFKA_HOME/bin

# ZooKeeper
export ZOOKEEPER_HOME=$KAFKA_HOME
export PATH=$PATH:$ZOOKEEPER_HOME/bin

# ElasticSearch
export ELASTICSEARCH_HOME=$DEV_INSTALL_HOME/elasticsearch-1.7.2
export PATH=$PATH:$ELASTICSEARCH_HOME/bin

# LogStash
export LOGSTASH_HOME=$DEV_INSTALL_HOME/logstash-1.5.4
export PATH=$PATH:$LOGSTASH_HOME/bin

# Kibana
export KIBANA_HOME=$DEV_INSTALL_HOME/kibana-4.1.2-linux-x64
export PATH=$PATH:$KIBANA_HOME/bin

# Hadoop HDFS
export HADOOP_HOME=$DEV_INSTALL_HOME/hadoop-2.6.0
export PATH=$PATH:$HADOOP_HOME/bin

# Redis
export REDIS_HOME=$DEV_INSTALL_HOME/redis-3.0.3
export PATH=$PATH:$REDIS_HOME/bin

# Tachyon
export TACHYON_HOME=$DEV_INSTALL_HOME/tachyon-0.7.1
export PATH=$PATH:$TACHYON_HOME/bin

# SBT
export SBT_HOME=$DEV_INSTALL_HOME/sbt
export PATH=$PATH:$SBT_HOME/bin
export SBT_OPTS="-Xmx10G -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=2G"

# Zeppelin
export ZEPPELIN_HOME=$DEV_INSTALL_HOME/zeppelin-0.6.0-spark-1.5.1-hadoop-2.6.0
export PATH=$PATH:$ZEPPELIN_HOME/bin

# Spark Notebook
export SPARK_NOTEBOOK_HOME=$DEV_INSTALL_HOME/spark-notebook-0.6.1-scala-2.10.4-spark-1.5.0-hadoop-2.6.0-with-hive-with-parquet
export PATH=$PATH:$SPARK_NOTEBOOK_HOME/bin

# Spark JobServer
export SPARK_JOBSERVER_HOME=$DEV_INSTALL_HOME/spark-jobserver-0.5.2
export PATH=$PATH:$SPARK_JOBSERVER_HOME/bin
