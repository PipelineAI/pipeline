# ~/.profile: executed by Bourne-compatible login shells.

if [ "$BASH" ]; then
  if [ -f ~/.bashrc ]; then
    . ~/.bashrc
  fi
fi

mesg n

# Versions of various application libraries used by this pipeline
export SPARK_ELASTICSEARCH_CONNECTOR_VERSION=2.1.2
export SPARK_CSV_CONNECTOR_VERSION=1.2.0
export SPARK_AVRO_CONNECTOR_VERSION=2.0.1
export SPARK_CASSANDRA_CONNECTOR_VERSION=1.4.0
export AKKA_VERSION=2.3.11
# This is needed to determine which version of various software tools to be downloaded
# Make this the source of truth and clone/source this file in the Dockerfile
#export SCALA_VERSION=2.10.4
#export SPARK_VERSION=1.5.1
export CASSANDRA_VERSION=2.2.3
export CONFLUENT_VERSION=1.0.1
export ELASTICSEARCH_VERSION=1.7.3
export LOGSTASH_VERSION=2.0.0
export KIBANA_VERSION=4.2.0
export NEO4J_VERSION=2.2.3
export REDIS_VERSION=3.0.5
export SBT_VERSION=0.13.9
export SPARKNOTEBOOK_VERSION=0.6.1
export HADOOP_VERSION=2.6.0
export TACHYON_VERSION=0.7.1
export ZEPPELIN_VERSION=0.6.0
export GENSORT_VERSION=1.5
export SCALATEST_VERSION=2.2.4
export ALGEBIRD_VERSION=0.11.0
export JEDIS_VERSION=2.7.3
export SBT_ASSEMBLY_PLUGIN_VERSION=0.14.0
export KAFKA_CLIENT_VERSION=0.8.2.2
export PYSPARK_DRIVER_PYTHON=ipython
export PYSPARK_DRIVER_PYTHON_OPTS="notebook --no-browser --port=7777"

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
#export H2O_HOME=$DEV_INSTALL_HOME/h2o-3.0.1.7

# MySQL
export MYSQL_CONNECTOR_JAR=/usr/share/java/mysql-connector-java.jar

# Cassandra
export CASSANDRA_HOME=$DEV_INSTALL_HOME/apache-cassandra-$CASSANDRA_VERSION
export PATH=$PATH:$CASSANDRA_HOME/bin

# Spark
export SPARK_HOME=$DEV_INSTALL_HOME/spark-$SPARK_VERSION-bin-fluxcapacitor
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin
export SPARK_EXAMPLES_JAR=$SPARK_HOME/lib/spark-examples-$SPARK_VERSION-hadoop$HADOOP_VERSION.jar

# Kafka
export KAFKA_HOME=$DEV_INSTALL_HOME/confluent-$CONFLUENT_VERSION
export PATH=$PATH:$KAFKA_HOME/bin

# ZooKeeper
export ZOOKEEPER_HOME=$KAFKA_HOME
export PATH=$PATH:$ZOOKEEPER_HOME/bin

# ElasticSearch
export ELASTICSEARCH_HOME=$DEV_INSTALL_HOME/elasticsearch-$ELASTICSEARCH_VERSION
export PATH=$PATH:$ELASTICSEARCH_HOME/bin

# LogStash
export LOGSTASH_HOME=$DEV_INSTALL_HOME/logstash-$LOGSTASH_VERSION
export PATH=$PATH:$LOGSTASH_HOME/bin

# Kibana
export KIBANA_HOME=$DEV_INSTALL_HOME/kibana-$KIBANA_VERSION-linux-x64
export PATH=$PATH:$KIBANA_HOME/bin

# Hadoop HDFS
export HADOOP_HOME=$DEV_INSTALL_HOME/hadoop-$HADOOP_VERSION
export PATH=$PATH:$HADOOP_HOME/bin

# Redis
export REDIS_HOME=$DEV_INSTALL_HOME/redis-$REDIS_VERSION
export PATH=$PATH:$REDIS_HOME/bin

# Tachyon
export TACHYON_HOME=$DEV_INSTALL_HOME/tachyon-$TACHYON_VERSION
export PATH=$PATH:$TACHYON_HOME/bin

# SBT
export SBT_HOME=$DEV_INSTALL_HOME/sbt
export PATH=$PATH:$SBT_HOME/bin
export SBT_OPTS="-Xmx10G -XX:+CMSClassUnloadingEnabled"

# --packages used to pass into our Spark jobs
export PACKAGES=org.apache.spark:spark-streaming-kafka-assembly_2.10:$SPARK_VERSION,org.elasticsearch:elasticsearch-spark_2.10:$SPARK_ELASTICSEARCH_CONNECTOR_VERSION,com.datastax.spark:spark-cassandra-connector_2.10:$SPARK_CASSANDRA_CONNECTOR_VERSION,redis.clients:jedis:$JEDIS_VERSION,com.twitter:algebird-core_2.10:$ALGEBIRD_VERSION,com.databricks:spark-avro_2.10:$SPARK_AVRO_CONNECTOR_VERSION,com.databricks:spark-csv_2.10:$SPARK_CSV_CONNECTOR_VERSION

# Zeppelin
export ZEPPELIN_HOME=$DEV_INSTALL_HOME/zeppelin-$ZEPPELIN_VERSION-spark-$SPARK_VERSION-hadoop-$HADOOP_VERSION-fluxcapacitor
export PATH=$PATH:$ZEPPELIN_HOME/bin

# Spark Notebook
export SPARK_NOTEBOOK_HOME=$DEV_INSTALL_HOME/spark-notebook-$SPARK_NOTEBOOK_VERSION-scala-$SCALA_VERSION-spark-1.5.0-hadoop-$HADOOP_VERSION-with-hive-with-parquet
export PATH=$PATH:$SPARK_NOTEBOOK_HOME/bin

# MyApps
export MYAPPS_HOME=$PIPELINE_HOME/myapps
