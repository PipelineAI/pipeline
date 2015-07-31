# Convenience
alias ll='ls -al'

# Dev Install
export DEV_INSTALL_HOME=~

# Cassandra
export CASSANDRA_HOME=$DEV_INSTALL_HOME/apache-cassandra-2.2.0
export PATH=$PATH:$CASSANDRA_HOME/bin

# Spark 
export SPARK_HOME=$DEV_INSTALL_HOME/spark-1.4.1-bin-fluxcapacitor
export PATH=$PATH:$SPARK_HOME/bin

# Kafka
export KAFKA_HOME=$DEV_INSTALL_HOME/confluent-1.0
export PATH=$PATH:$KAFKA_HOME/bin

# ZooKeeper
export ZOOKEEPER_HOME=$KAFKA_HOME/bin
export PATH=$PATH:$ZOOKEEPER_HOME/bin

# ElasticSearch 
export ELASTICSEARCH_HOME=$DEV_INSTALL_HOME/elasticsearch-1.7.1
export PATH=$PATH:$ELASTICSEARCH_HOME/bin

# Logstash
export LOGSTASH_HOME=$DEV_INSTALL_HOME/logstash-1.5.3
export PATH=$PATH:$LOGSTASH_HOME/bin

# Kibana
export KIBANA_HOME=$DEV_INSTALL_HOME/kibana-4.1.1-linux-x64
export PATH=$PATH:$KIBANA_HOME/bin

# Hadoop/HDFS
export HADOOP_HOME=$DEV_INSTALL_HOME/hadoop-2.6.0
export PATH=$PATH:$HADOOP_HOME/bin

# Redis
export REDIS_HOME=$DEV_INSTALL_HOME/redis-3.0.3
export PATH=$PATH:$REDIS_HOME/bin

# Tachyon
export TACHYON_HOME=$DEV_INSTALL_HOME/tachyon-0.6.4
export PATH=$PATH:$TACHYON_HOME/bin

# SBT
export SBT_HOME=$DEV_INSTALL_HOME/sbt
export PATH=$PATH:$SBT_HOME/bin
export SBT_OPTS="-Xmx10G -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=2G"

# Zeppelin
export ZEPPELIN_HOME=$DEV_INSTALL_HOME/zeppelin-0.5.1-spark-1.4.2-hadoop-1.6.0
export PATH=$PATH:$ZEPPELIN_HOME/bin

# Spark-Notebook
export SPARK_NOTEBOOK_HOME=$DEV_INSTALL_HOME/spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet
export PATH=$PATH:$SPARK_NOTEBOOK_HOME/bin
