# Dev Install Home (Tools)
export DEV_INSTALL_HOME=~

# Pipeline Home
export PIPELINE_HOME=$DEV_INSTALL_HOME/pipeline

# Config Home
export CONFIG_HOME=$PIPELINE_HOME/config

# Scripts Home
export SCRIPTS_HOME=$PIPELINE_HOME/bin

###################################################################
# The following WORK_HOME and LOGS_HOME 
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
export WORK_HOME=$PIPELINE_HOME/work

# Datasets Home (where data for apps is read)
export DATASETS_HOME=$PIPELINE_HOME/datasets

# Logs Home (where log data from apps is written)
export LOGS_HOME=$PIPELINE_HOME/logs

# MyApps
export MYAPPS_HOME=$PIPELINE_HOME/myapps

# HTML Home
export HTML_HOME=$MYAPPS_HOME/html

###################################################################

# Initialize PATH
export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

# Java Home
export JAVA_HOME=/usr/lib/jvm/java-8-oracle
export PATH=$JAVA_HOME/bin:$PATH
export JAVA_OPTS="-Xmx10G -XX:+CMSClassUnloadingEnabled"

# Bazel Home
export BAZEL_HOME=$DEV_INSTALL_HOME/bazel-$BAZEL_VERSION
export PATH=$BAZEL_HOME/bin:$PATH

# TensorFlow Serving Home (not required on PATH)
export TENSORFLOW_SERVING_HOME=$DEV_INSTALL_HOME/serving

# TensorFlow Home (not required on PATH)
export TENSORFLOW_HOME=$DEV_INSTALL_HOME/tensorflow

# Scripts Home
export PATH=$SCRIPTS_HOME/cli:$SCRIPTS_HOME/cluster:$SCRIPTS_HOME/docker:$SCRIPTS_HOME/initial:$SCRIPTS_HOME/kafka:$SCRIPTS_HOME/rest:$SCRIPTS_HOME/service:$SCRIPTS_HOME/util:$PATH

# MySQL
export MYSQL_CONNECTOR_JAR=/usr/share/java/mysql-connector-java.jar

# Cassandra
export CASSANDRA_HOME=$DEV_INSTALL_HOME/apache-cassandra-$CASSANDRA_VERSION
export PATH=$CASSANDRA_HOME/bin:$PATH

# Spark
export SPARK_HOME=$DEV_INSTALL_HOME/spark-$SPARK_VERSION-bin-fluxcapacitor
export PATH=$SPARK_HOME/bin:$SPARK_HOME/sbin:$PATH
export SPARK_EXAMPLES_JAR=$SPARK_HOME/lib/spark-examples-$SPARK_VERSION-hadoop$HADOOP_VERSION.jar

# Tachyon
export TACHYON_HOME=$SPARK_HOME/tachyon
export PATH=$TACHYON_HOME/bin:$PATH

# Kafka/Confluent
export CONFLUENT_HOME=$DEV_INSTALL_HOME/confluent-$CONFLUENT_VERSION
export PATH=$CONFLUENT_HOME/bin:$PATH

# ZooKeeper
export ZOOKEEPER_HOME=$CONFLUENT_HOME
export PATH=$ZOOKEEPER_HOME/bin:$PATH

# ElasticSearch
export ELASTICSEARCH_HOME=$DEV_INSTALL_HOME/elasticsearch-$ELASTICSEARCH_VERSION
export PATH=$ELASTICSEARCH_HOME/bin:$PATH

# LogStash
export LOGSTASH_HOME=$DEV_INSTALL_HOME/logstash-$LOGSTASH_VERSION
export PATH=$LOGSTASH_HOME/bin:$PATH

# Kibana
export KIBANA_HOME=$DEV_INSTALL_HOME/kibana-$KIBANA_VERSION-linux-x64
export PATH=$KIBANA_HOME/bin:$PATH

# Hadoop HDFS
export HADOOP_HOME=$DEV_INSTALL_HOME/hadoop-$HADOOP_VERSION
export PATH=$HADOOP_HOME/bin:$PATH

# Hadoop Hive
export HIVE_HOME=$DEV_INSTALL_HOME/apache-hive-$HIVE_VERSION-bin
export PATH=$HIVE_HOME/bin:$PATH
export HADOOP_USER_CLASSPATH_FIRST=true

# Redis
export REDIS_HOME=$DEV_INSTALL_HOME/redis-$REDIS_VERSION
export PATH=$REDIS_HOME/bin:$PATH

# Webdis
export WEBDIS_HOME=$DEV_INSTALL_HOME/webdis
export PATH=$WEBDIS_HOME:$PATH

# NiFi 
export NIFI_HOME=$DEV_INSTALL_HOME/nifi-$NIFI_VERSION
export PATH=$NIFI_HOME/bin:$PATH

# SBT
export SBT_HOME=$DEV_INSTALL_HOME/sbt
export PATH=$SBT_HOME/bin:$PATH
export SBT_OPTS=$JAVA_OPTS

# --repositories used to resolve --packages
export SPARK_REPOSITORIES=http://dl.bintray.com/spark-packages/maven,https://oss.sonatype.org/content/repositories/snapshots,https://repository.apache.org/content/groups/snapshots

# --packages used to pass into our Spark jobs
export SPARK_SUBMIT_PACKAGES=tjhunter:tensorframes:$TENSORFRAMES_VERSION-s_2.10,com.maxmind.geoip2:geoip2:$MAXMIND_GEOIP_VERSION,com.netflix.dyno:dyno-jedis:$DYNO_VERSION,org.json4s:json4s-jackson_2.10:$JSON4S_VERSION,amplab:spark-indexedrdd:$INDEXEDRDD_VERSION,org.apache.spark:spark-streaming-kafka-assembly_2.10:$SPARK_VERSION,org.elasticsearch:elasticsearch-spark_2.10:$SPARK_ELASTICSEARCH_CONNECTOR_VERSION,com.datastax.spark:spark-cassandra-connector_2.10:$SPARK_CASSANDRA_CONNECTOR_VERSION,redis.clients:jedis:$JEDIS_VERSION,com.twitter:algebird-core_2.10:$ALGEBIRD_VERSION,com.databricks:spark-avro_2.10:$SPARK_AVRO_CONNECTOR_VERSION,com.databricks:spark-csv_2.10:$SPARK_CSV_CONNECTOR_VERSION,org.apache.nifi:nifi-spark-receiver:$SPARK_NIFI_CONNECTOR_VERSION,com.madhukaraphatak:java-sizeof_2.10:0.1,com.databricks:spark-xml_2.10:$SPARK_XML_VERSION,edu.stanford.nlp:stanford-corenlp:$STANFORD_CORENLP_VERSION,org.jblas:jblas:$JBLAS_VERSION,graphframes:graphframes:$GRAPHFRAMES_VERSION

# We still need to include a reference to a local stanford-corenlp-$STANFORD_CORENLP_VERSION-models.jar because SparkSubmit doesn't support a classifier in --packages
# MYAPPS_HOME/codegen/spark/1.6.1/target/scala-2.10/codegen-spark-1-6-1_2.10-1.0.jar
export SPARK_SUBMIT_JARS=$MYAPPS_HOME/spark/redis/lib/spark-redis_2.10-$SPARK_REDIS_CONNECTOR_VERSION.jar,$MYSQL_CONNECTOR_JAR,$MYAPPS_HOME/spark/ml/lib/spark-corenlp_2.10-0.1.jar,$MYAPPS_HOME/spark/ml/lib/stanford-corenlp-$STANFORD_CORENLP_VERSION-models.jar,$MYAPPS_HOME/spark/ml/target/scala-2.10/ml_2.10-1.0.jar,$MYAPPS_HOME/spark/sql/target/scala-2.10/sql_2.10-1.0.jar,$MYAPPS_HOME/spark/core/target/scala-2.10/core_2.10-1.0.jar,$MYAPPS_HOME/spark/streaming/target/scala-2.10/streaming_2.10-1.0.jar,$MYAPPS_HOME/serving/spark/target/scala-2.10/spark-serving_2.10-1.0.jar

# Zeppelin
export ZEPPELIN_HOME=$DEV_INSTALL_HOME/zeppelin-$ZEPPELIN_VERSION
export PATH=$ZEPPELIN_HOME/bin:$PATH

# Flink
export FLINK_HOME=$DEV_INSTALL_HOME/flink-$FLINK_VERSION
export PATH=$FLINK_HOME/bin:$PATH

# Jupyter/iPython

# Airflow
export AIRFLOW_HOME=$DEV_INSTALL_HOME/airflow
export PATH=$AIRFLOW_HOME/bin:$PATH

# Presto
export PRESTO_HOME=$DEV_INSTALL_HOME/presto-server-$PRESTO_VERSION
export PATH=$PRESTO_HOME/bin:$PATH

# Titan
export TITAN_HOME=$DEV_INSTALL_HOME/titan-$TITAN_VERSION
export PATH=$TITAN_HOME/bin:$PATH

# JMeter
export JMETER_HOME=$DEV_INSTALL_HOME/apache-jmeter-$JMETER_VERSION
export PATH=$JMETER_HOME/bin:$PATH

# Dynomite
export DYNOMITE_HOME=$DEV_INSTALL_HOME/dynomite
export PATH=$DYNOMITE_HOME:$PATH

# Hystrix Dashboard
export HYSTRIX_DASHBOARD_HOME=$DEV_INSTALL_HOME/hystrix-dashboard-$HYSTRIX_DASHBOARD_VERSION

# Atlas 
export ATLAS_HOME=$DEV_INSTALL_HOME/atlas-$ATLAS_VERSION
