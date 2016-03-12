# ~/.profile: executed by Bourne-compatible login shells.

if [ "$BASH" ]; then
  if [ -f ~/.bashrc ]; then
    . ~/.bashrc
  fi
fi

mesg n

# All _VERSION env variables are being set in the Dockerfile and carried through to Docker Containers
# You can override the Dockerfile values here for application dependency libraries such as Algebird, Cassandra-Spark Connector, etc
export AKKA_VERSION=2.3.11
export SPARK_CASSANDRA_CONNECTOR_VERSION=1.4.0
export SPARK_ELASTICSEARCH_CONNECTOR_VERSION=2.1.2
export KAFKA_CLIENT_VERSION=0.8.2.2
export SCALATEST_VERSION=2.2.4
export JEDIS_VERSION=2.7.3
export SPARK_CSV_CONNECTOR_VERSION=1.4.0
export SPARK_AVRO_CONNECTOR_VERSION=2.0.1
export ALGEBIRD_VERSION=0.11.0
export STREAMING_MATRIX_FACTORIZATION_VERSION=0.1.0
export SBT_ASSEMBLY_PLUGIN_VERSION=0.14.0
export SBT_SPARK_PACKAGES_PLUGIN_VERSION=0.2.3
export SPARK_NIFI_CONNECTOR_VERSION=0.4.1
export SPARK_XML_VERSION=0.3.1
export JBLAS_VERSION=1.2.4
export GRAPHFRAMES_VERSION=0.1.0-spark1.6

#Dev Install
export DEV_INSTALL_HOME=~

# Pipeline Home
export PIPELINE_HOME=$DEV_INSTALL_HOME/pipeline

# Config Home
export CONFIG_HOME=$PIPELINE_HOME/config

# Scripts Home
export SCRIPTS_HOME=$PIPELINE_HOME/bin

###################################################################
# The following DATA_WORK_HOME and LOGS_HOME 
#   are not always used by apps due to limitations with certain apps
#   and how they resolve exports
#
# In these cases, the configs are usually relative to where the
# service is started 
#   ie. LOGS_DIR=logs/kafka, DATA_DIR=data/zookeeper, etc

# If these paths change, be sure to grep and update the hard coded 
#   versions in all apps including the .tgz packages if their
#   configs are not aleady exposed under pipeline/config/...

# Data Work Home (where active, work data is written)
#   This is for kafka data, cassandra data,
#   and other work data created by users during
#   the lifetime of an application, but will
#   be erased upon environment restart
export DATA_WORK_HOME=$PIPELINE_HOME/data_work

# Datasets Home (where data for apps is read)
export DATASETS_HOME=$PIPELINE_HOME/datasets

# Logs Home (where log data from apps is written)
export LOGS_HOME=$PIPELINE_HOME/logs

# HTML Home
export HTML_HOME=$PIPELINE_HOME/html

###################################################################

# Java Home
export JAVA_HOME=/usr

# Scripts Home
export PATH=$PATH:$SCRIPTS_HOME

# MySQL
export MYSQL_CONNECTOR_JAR=/usr/share/java/mysql-connector-java.jar

# Cassandra
export CASSANDRA_HOME=$DEV_INSTALL_HOME/apache-cassandra-$CASSANDRA_VERSION
export PATH=$PATH:$CASSANDRA_HOME/bin

# Spark
export SPARK_HOME=$DEV_INSTALL_HOME/spark-$SPARK_VERSION-bin-fluxcapacitor
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin
export SPARK_EXAMPLES_JAR=$SPARK_HOME/lib/spark-examples-$SPARK_VERSION-hadoop$HADOOP_VERSION.jar

# Tachyon
export TACHYON_HOME=$SPARK_HOME/tachyon
export PATH=$PATH:$TACHYON_HOME/bin

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

# Webdis
export WEBDIS_HOME=$DEV_INSTALL_HOME/webdis
export PATH=$PATH:$WEBDIS_HOME

# Webdis
export NIFI_HOME=$DEV_INSTALL_HOME/nifi-$NIFI_VERSION
export PATH=$PATH:$NIFI_HOME/bin

# SBT
export SBT_HOME=$DEV_INSTALL_HOME/sbt
export PATH=$PATH:$SBT_HOME/bin
export SBT_OPTS="-Xmx10G -XX:+CMSClassUnloadingEnabled"

# MyApps
export MYAPPS_HOME=$PIPELINE_HOME/myapps

# --packages used to pass into our Spark jobs
export SPARK_SUBMIT_PACKAGES=org.apache.spark:spark-streaming-kafka-assembly_2.10:$SPARK_VERSION,org.elasticsearch:elasticsearch-spark_2.10:$SPARK_ELASTICSEARCH_CONNECTOR_VERSION,com.datastax.spark:spark-cassandra-connector_2.10:$SPARK_CASSANDRA_CONNECTOR_VERSION,redis.clients:jedis:$JEDIS_VERSION,com.twitter:algebird-core_2.10:$ALGEBIRD_VERSION,com.databricks:spark-avro_2.10:$SPARK_AVRO_CONNECTOR_VERSION,com.databricks:spark-csv_2.10:$SPARK_CSV_CONNECTOR_VERSION,org.apache.nifi:nifi-spark-receiver:$SPARK_NIFI_CONNECTOR_VERSION,brkyvz:streaming-matrix-factorization:$STREAMING_MATRIX_FACTORIZATION_VERSION,com.madhukaraphatak:java-sizeof_2.10:0.1,com.databricks:spark-xml_2.10:$SPARK_XML_VERSION,edu.stanford.nlp:stanford-corenlp:$STANFORD_CORENLP_VERSION,org.jblas:jblas:$JBLAS_VERSION,graphframes:graphframes:${GRAPHFRAMES_VERSION}

# We still need to include a reference to a local stanford-corenlp-$STANFORD_CORENLP_VERSION-models.jar because SparkSubmit doesn't support a classifier in --packages
export SPARK_SUBMIT_JARS=$MYSQL_CONNECTOR_JAR,$MYAPPS_HOME/ml/lib/spark-corenlp_2.10-0.1.jar,$MYAPPS_HOME/ml/lib/stanford-corenlp-$STANFORD_CORENLP_VERSION-models.jar,$MYAPPS_HOME/ml/target/scala-2.10/ml_2.10-1.0.jar,$MYAPPS_HOME/sql/target/scala-2.10/sql_2.10-1.0.jar,$MYAPPS_HOME/core/target/scala-2.10/core_2.10-1.0.jar,$MYAPPS_HOME/streaming/target/scala-2.10/streaming_2.10-1.0.jar

# Zeppelin
export ZEPPELIN_HOME=$DEV_INSTALL_HOME/zeppelin-$ZEPPELIN_VERSION
export PATH=$PATH:$ZEPPELIN_HOME/bin

# Jupyter/iPython
export PYSPARK_DRIVER_PYTHON=jupyter
export PYSPARK_DRIVER_PYTHON_OPTS="notebook --config=$CONFIG_HOME/jupyter/jupyter_notebook_config.py"

# Airflow
export AIRFLOW_HOME=$DEV_INSTALL_HOME/airflow
export PATH=$PATH:$AIRFLOW_HOME/bin

# Presto
export PRESTO_HOME=$DEV_INSTALL_HOME/presto-server-$PRESTO_VERSION
export PATH=$PATH:$PRESTO_HOME/bin

# Titan
export TITAN_HOME=$DEV_INSTALL_HOME/titan-$TITAN_VERSION
export PATH=$PATH:$TITAN_HOME/bin
