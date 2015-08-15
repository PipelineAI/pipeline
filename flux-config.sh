# SSH
echo ...Configuring SSH Part 1 of 2...
service ssh start
ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
mkdir -p ~/.ssh
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
chmod 600 ~/.ssh/id_rsa

# Apache Httpd
echo ...Configuring Apache Httpd...
a2enmod proxy
a2enmod proxy_http
a2dissite 000-default
mv /etc/apache2/apache2.conf /etc/apache2/apache2.conf.orig
ln -s $PIPELINE_HOME/config/apache2/apache2.conf /etc/apache2

# Datasets
echo ...Decompressing Datasets...
bzip2 -d -k datasets/dating/gender.json.bz2
bzip2 -d -k datasets/dating/gender.csv.bz2
bzip2 -d -k datasets/dating/ratings.json.bz2
bzip2 -d -k datasets/dating/ratings.csv.bz2

# Sample WebApp
echo ...Configuring Sample WebApp...
ln -s $PIPELINE_HOME/config/sparkafterdark/sparkafterdark.conf /etc/apache2/sites-available
a2ensite sparkafterdark
ln -s $PIPELINE_HOME/datasets $PIPELINE_HOME/html/sparkafterdark.com
# Every parent of /html is required to serve up the html
chmod -R a+rx ~

# Ganglia
echo ...Configuring Ganglia...
ln -s $PIPELINE_HOME/config/ganglia/ganglia.conf /etc/apache2/sites-available
a2ensite ganglia
mv /etc/ganglia/gmetad.conf /etc/ganglia/gmetad.conf.orig
mv /etc/ganglia/gmond.conf /etc/ganglia/gmond.conf.orig
ln -s $PIPELINE_HOME/config/ganglia/gmetad.conf /etc/ganglia
ln -s $PIPELINE_HOME/config/ganglia/gmond.conf /etc/ganglia

# MySQL (Required by HiveQL Exercises)
echo ...Configurating MySQL...
echo ...Ignore any ERROR 2002's Below...
service mysql start
mysqladmin -u root password "password"
nohup service mysql stop

# Cassandra
echo ...Configuring Cassandra...
mv $CASSANDRA_HOME/conf/cassandra-env.sh $CASSANDRA_HOME/conf/cassandra-env.sh.orig
mv $CASSANDRA_HOME/conf/cassandra.yaml $CASSANDRA_HOME/conf/cassandra.yaml.orig
ln -s $PIPELINE_HOME/config/cassandra/cassandra-env.sh $CASSANDRA_HOME/conf
ln -s $PIPELINE_HOME/config/cassandra/cassandra.yaml $CASSANDRA_HOME/conf

# Spark
echo ...Configuring Spark...
ln -s $PIPELINE_HOME/config/spark/spark-defaults.conf $SPARK_HOME/conf
ln -s $PIPELINE_HOME/config/spark/spark-env.sh $SPARK_HOME/conf
ln -s $PIPELINE_HOME/config/spark/metrics.properties $SPARK_HOME/conf
ln -s $PIPELINE_HOME/config/hadoop/hive-site.xml $SPARK_HOME/conf
ln -s $MYSQL_CONNECTOR_JAR $SPARK_HOME/lib

# Kafka
echo ...Configuring Kafka...

# ZooKeeper
echo ...Configuring ZooKeeper...

# ElasticSearch
echo ...Configuring ElasticSearch...

# Logstash
echo ...Configuring Logstash...

# Kibana
echo ...Configuring Kibana...

# Hadoop HDFS
echo ...Configuring Docker-local Hadoop HDFS...

# Redis
echo ...Configuring Redis...

# Tachyon
echo ...Configuring Tachyon...
mv $TACHYON_HOME/conf/tachyon/log4j.properties $TACHYON_HOME/conf/tachyon/log4j.properties.orig
ln -s $PIPELINE_HOME/config/tachyon/log4j.properties $TACHYON_HOME/conf
ln -s $PIPELINE_HOME/config/tachyon/tachyon-env.sh $TACHYON_HOME/conf
# The following command requies the SSH daemon to be running
# If we switch to use HDFS as the underfs, we'll need the HDFS daemon to be running
# We need to chmod the keys again - not sure why, but it works so let's keep it
chmod 600 ~/.ssh/authorized_keys
chmod 600 ~/.ssh/id_rsa
tachyon format

# SBT
echo ...Configuring SBT...

# Zeppelin
echo ...Configuring Zeppelin...
ln -s $PIPELINE_HOME/config/zeppelin/zeppelin-env.sh $ZEPPELIN_HOME/conf
ln -s $PIPELINE_HOME/config/zeppelin/zeppelin-site.xml $ZEPPELIN_HOME/conf
ln -s $PIPELINE_HOME/config/zeppelin/interpreter.json $ZEPPELIN_HOME/conf
ln -s $PIPELINE_HOME/config/hadoop/hive-site.xml $ZEPPELIN_HOME/conf
ln -s $MYSQL_CONNECTOR_JAR $ZEPPELIN_HOME/lib

# Spark-Notebook
echo ...Configuring Spark-Notebook...
ln -s $PIPELINE_HOME/notebooks/spark-notebook/pipeline $SPARK_NOTEBOOK_HOME/notebooks

# Spark Job Server
echo ...Configuring Spark Job Server...
#ln -s $PIPELINE_HOME/config/spark-jobserver/pipeline.conf $SPARK_JOBSERVER_HOME/config 
#ln -s $PIPELINE_HOME/config/spark-jobserver/pipeline.sh $SPARK_JOBSERVER_HOME/config

# SSH (Part 2/2)
echo ...Configuring SSH Part 2 of 2...
# We need to keep the SSH service running for other services to be configured above
service ssh stop
