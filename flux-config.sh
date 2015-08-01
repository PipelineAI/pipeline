. ./flux-setenv.sh

# SSH
echo Configuring SSH
service ssh start 
ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa \
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys \
chmod 600 ~/.ssh/authorized_keys \
chmod 600 ~/.ssh/id_rsa \
service ssh stop

# Apache Httpd
echo Configuring Apache Httpd
a2enmod proxy 
a2enmod proxy_http 
a2dissite 000-default
mv /etc/apache2/apache2.conf /etc/apache2/apache2.conf.orig 
ln -s $PIPELINE_HOME/config/apache2/apache2.conf /etc/apache2 

# Datasets
echo Configuring Datasets
bzip2 -d -k datasets/dating/gender.csv.bz2
bzip2 -d -k datasets/dating/ratings.csv.bz2

# Spark After Dark Sample WebApp
# Configuring Sample WebApp
ln -s $PIPELINE_HOME/config/sparkafterdark/sparkafterdark.conf /etc/apache2/sites-available 
a2ensite sparkafterdark 
ln -s $PIPELINE_HOME/datasets $PIPELINE_HOME/html/sparkafterdark.com 
# Every parent of /html is required to serve up the html
chmod -R a+rx ~ 

# Ganglia
echo Configuring Ganglia
ln -s $PIPELINE_HOME/config/ganglia/ganglia.conf /etc/apache2/sites-available 
a2ensite ganglia 
mv /etc/ganglia/gmetad.conf /etc/ganglia/gmetad.conf.orig 
mv /etc/ganglia/gmond.conf /etc/ganglia/gmond.conf.orig 
ln -s $PIPELINE_HOME/config/ganglia/gmetad.conf /etc/ganglia 
ln -s $PIPELINE_HOME/config/ganglia/gmond.conf /etc/ganglia 

# MySQL (Required by HiveQL Exercises)
echo Configurating MySQL
service mysql start 
mysqladmin -u root password password 
service mysql stop 
export MYSQL_CONNECTOR_JAR=/usr/share/java/mysql-connector-java-5.1.28.jar

# Cassandra
echo Configuring Cassandra
export CASSANDRA_HOME=$DEV_INSTALL_HOME/apache-cassandra-2.2.0
export PATH=$PATH:$CASSANDRA_HOME/bin

# Spark 
echo Configuring Spark
export SPARK_HOME=$DEV_INSTALL_HOME/spark-1.4.1-bin-fluxcapacitor
export SPARK_EXAMPLES_JAR=$SPARK_HOME/lib/spark-examples-1.4.1-hadoop2.6.0.jar
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin
ln -s $PIPELINE_HOME/config/spark/spark-defaults.conf $SPARK_HOME/conf 
ln -s $PIPELINE_HOME/config/spark/spark-env.sh $SPARK_HOME/conf 
ln -s $PIPELINE_HOME/config/spark/metrics.properties $SPARK_HOME/conf 
ln -s $PIPELINE_HOME/config/hadoop/hive-site.xml $SPARK_HOME/conf 
ln -s $MYSQL_CONNECTOR_JAR $SPARK_HOME/lib 

# Kafka
echo Configuring Kafka
export KAFKA_HOME=$DEV_INSTALL_HOME/confluent-1.0
export PATH=$PATH:$KAFKA_HOME/bin

# ZooKeeper
echo Configuring ZooKeeper
export ZOOKEEPER_HOME=$KAFKA_HOME/bin
export PATH=$PATH:$ZOOKEEPER_HOME/bin

# ElasticSearch 
echo Configuring ElasticSearch
export ELASTICSEARCH_HOME=$DEV_INSTALL_HOME/elasticsearch-1.7.1
export PATH=$PATH:$ELASTICSEARCH_HOME/bin

# Logstash
echo Configuring Logstash
export LOGSTASH_HOME=$DEV_INSTALL_HOME/logstash-1.5.3
export PATH=$PATH:$LOGSTASH_HOME/bin

# Kibana
echo Configuring Kibana
export KIBANA_HOME=$DEV_INSTALL_HOME/kibana-4.1.1-linux-x64
export PATH=$PATH:$KIBANA_HOME/bin

# Hadoop HDFS
echo Configuring Hadoop HDFS
export HADOOP_HOME=$DEV_INSTALL_HOME/hadoop-2.6.0
export PATH=$PATH:$HADOOP_HOME/bin

# Redis
#echo Configuring Redis
#export REDIS_HOME=$DEV_INSTALL_HOME/redis-3.0.3
#export PATH=$PATH:$REDIS_HOME/bin

# Tachyon
echo Configuring Tachyon
export TACHYON_HOME=$DEV_INSTALL_HOME/tachyon-0.6.4
export PATH=$PATH:$TACHYON_HOME/bin
ln -s $PIPELINE_HOME/config/tachyon/tachyon-env.sh $TACHYON_HOME/conf

# SBT
echo Configuring SBT
export SBT_HOME=$DEV_INSTALL_HOME/sbt
export PATH=$PATH:$SBT_HOME/bin
export SBT_OPTS="-Xmx10G -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=2G"

# Zeppelin
echo Configuring Zeppelin
export ZEPPELIN_HOME=$DEV_INSTALL_HOME/zeppelin-0.5.1-spark-1.4.1-hadoop-1.6.0
export PATH=$PATH:$ZEPPELIN_HOME/bin
ln -s $PIPELINE_HOME/config/zeppelin/zeppelin-env.sh $ZEPPELIN_HOME/conf 
ln -s $PIPELINE_HOME/config/zeppelin/zeppelin-site.xml $ZEPPELIN_HOME/conf 
ln -s $PIPELINE_HOME/config/zeppelin/interpreter.json $ZEPPELIN_HOME/conf 
ln -s $PIPELINE_HOME/config/hadoop/hive-site.xml $ZEPPELIN_HOME/conf 
ln -s $MYSQL_CONNECTOR_JAR $ZEPPELIN_HOME/lib 

# Spark-Notebook
echo Configuring Spark-Notebook
export SPARK_NOTEBOOK_HOME=$DEV_INSTALL_HOME/spark-notebook-0.6.0-scala-2.10.4-spark-1.4.1-hadoop-2.6.0-with-hive-with-parquet
export PATH=$PATH:$SPARK_NOTEBOOK_HOME/bin
