echo '...**** IGNORE ANY ERRORS RELATED TO THINGS THAT ALREADY EXIST.  THIS IS OK. ****...'

# SSH
echo '...Configuring SSH Part 1 of 2...'
service ssh start
ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
mkdir -p ~/.ssh
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
chmod 600 ~/.ssh/id_rsa

# Adding syntax highlighting to VIM
ln -s $CONFIG_HOME/.vim ~/ 

# Apache Httpd
echo '...Configuring Apache Httpd...'
a2enmod proxy
a2enmod proxy_http
a2dissite 000-default
mv /etc/apache2/apache2.conf /etc/apache2/apache2.conf.orig
ln -s $CONFIG_HOME/apache2/apache2.conf /etc/apache2
mkdir -p $LOGS_HOME/apache2

# Datasets
echo '...Decompressing Datasets (This takes a while)...'
bzip2 -d -k $DATASETS_HOME/dating/genders.json.bz2
bzip2 -d -k $DATASETS_HOME/dating/genders.csv.bz2
bzip2 -d -k $DATASETS_HOME/dating/ratings.json.bz2
bzip2 -d -k $DATASETS_HOME/dating/ratings.csv.bz2
bzip2 -d -k $DATASETS_HOME/movielens/ml-latest/movies.csv.bz2
cat $DATASETS_HOME/movielens/ml-latest/ratings.csv.bz2-part-* > $DATASETS_HOME/movielens/ml-latest/ratings.csv.bz2
bzip2 -d -k $DATASETS_HOME/movielens/ml-latest/ratings.csv.bz2
tar -xjf $DATASETS_HOME/dating/genders-partitioned.parquet.tar.bz2 -C $DATASETS_HOME/dating/
tar -xjf $DATASETS_HOME/dating/genders-unpartitioned.parquet.tar.bz2 -C $DATASETS_HOME/dating
tar -xjf $DATASETS_HOME/dating/ratings-partitioned.parquet.tar.bz2 -C $DATASETS_HOME/dating
tar -xjf $DATASETS_HOME/dating/ratings-unpartitioned.parquet.tar.bz2 -C $DATASETS_HOME/dating/
tar -xjf $DATASETS_HOME/dating/genders-partitioned.orc.tar.bz2 -C $DATASETS_HOME/dating/
tar -xjf $DATASETS_HOME/dating/genders-unpartitioned.orc.tar.bz2 -C $DATASETS_HOME/dating/
tar -xjf $DATASETS_HOME/dating/ratings-partitioned.orc.tar.bz2 -C $DATASETS_HOME/dating/
tar -xjf $DATASETS_HOME/dating/ratings-unpartitioned.orc.tar.bz2 -C $DATASETS_HOME/dating/
tar -xjf $DATASETS_HOME/dating/genders-partitioned.avro.tar.bz2 -C $DATASETS_HOME/dating/
tar -xjf $DATASETS_HOME/dating/genders-unpartitioned.avro.tar.bz2 -C $DATASETS_HOME/dating/
tar -xjf $DATASETS_HOME/dating/ratings-partitioned.avro.tar.bz2 -C $DATASETS_HOME/dating/
tar -xjf $DATASETS_HOME/dating/ratings-unpartitioned.avro.tar.bz2 -C $DATASETS_HOME/dating/
tar --directory $DATASETS_HOME/serving/recommendations/spark-1.6.1/ -xzvf $DATASETS_HOME/serving/recommendations/spark-1.6.1/als.tar.gz
tar --directory $DATASETS_HOME/notmnist/ -xzvf $DATASETS_HOME/notmnist/notMNIST_small.tar.gz

# these part files were created with the following command:
#   split --bytes=100MB --numeric-suffixes --suffix-length=1 lfw-deepfunneled.tgz lfw-deepfunneled.tgz-part-
cat $DATASETS_HOME/eigenface/lfw-deepfunneled.tgz-part-* > $DATASETS_HOME/eigenface/lfw-deepfunneled.tgz
tar --directory $DATASETS_HOME/eigenface/ -xzvf $DATASETS_HOME/eigenface/lfw-deepfunneled.tgz

# these part files were created with the following command:
#   split --bytes=100MB --numeric-suffixes --suffix-length=1 notMNIST_large.tar.gz notMNIST_large.tar.gz-part-
#cat $DATASETS_HOME/notmnist/notMNIST_large.tar.gz-part-* > $DATASETS_HOME/notmnist/notMNIST_large.tar.gz
#tar --directory $DATASETS_HOME/notmnist/ -xzvf $DATASETS_HOME/notmnist/notMNIST_large.tar.gz

# Sample WebApp
echo '...Configuring Example WebApp...'
ln -s $CONFIG_HOME/advancedspark.com/advancedspark.conf /etc/apache2/sites-available
a2ensite advancedspark.conf
# We're just copying these under /var/www/html for now
# Ideally, a symlink would be more appropriate, but Apache is being a pain with permissions
cp -R $MYAPPS_HOME/html/advancedspark.com/* /var/www/html

# Log Dirs
echo '...Configuring Log Dirs...'
mkdir -p $LOGS_HOME/akka/feeder
mkdir -p $LOGS_HOME/spark/streaming
mkdir -p $LOGS_HOME/spark/ml
mkdir -p $LOGS_HOME/spark/sql
mkdir -p $LOGS_HOME/spark/core
mkdir -p $LOGS_HOME/spark/redis
mkdir -p $LOGS_HOME/jupyterhub
mkdir -p $LOGS_HOME/flink/streaming
mkdir -p $LOGS_HOME/kafka/streams
mkdir -p $LOGS_HOME/serving/discovery
mkdir -p $LOGS_HOME/serving/prediction
mkdir -p $LOGS_HOME/serving/finagle
mkdir -p $LOGS_HOME/serving/flask
mkdir -p $LOGS_HOME/serving/watcher
mkdir -p $LOGS_HOME/serving/hystrix
mkdir -p $LOGS_HOME/serving/atlas
mkdir -p $LOGS_HOME/serving/tensorflow/

# Ganglia
echo '...Configuring Ganglia...'
ln -s $CONFIG_HOME/ganglia/ganglia.conf /etc/apache2/sites-available
a2ensite ganglia
mv /etc/ganglia/gmetad.conf /etc/ganglia/gmetad.conf.orig
mv /etc/ganglia/gmond.conf /etc/ganglia/gmond.conf.orig
ln -s $CONFIG_HOME/ganglia/gmetad.conf /etc/ganglia
ln -s $CONFIG_HOME/ganglia/gmond.conf /etc/ganglia

# MySQL (Required by HiveQL Exercises)
echo '...Configuring MySQL...'
service mysql start
mysqladmin -u root password "password"
echo '...****Ignore the ERROR 2002s Below****...'
service mysql stop
echo '...****Ignore any ERROR 2002s Above****...'

# Cassandra
echo '...Configuring Cassandra...'
mv $CASSANDRA_HOME/conf/cassandra-env.sh $CASSANDRA_HOME/conf/cassandra-env.sh.orig
mv $CASSANDRA_HOME/conf/cassandra.yaml $CASSANDRA_HOME/conf/cassandra.yaml.orig
ln -s $CONFIG_HOME/cassandra/cassandra-env.sh $CASSANDRA_HOME/conf
ln -s $CONFIG_HOME/cassandra/cassandra.yaml $CASSANDRA_HOME/conf
mkdir -p $WORK_HOME/cassandra/data
mkdir -p $WORK_HOME/cassandra/commitlog
mkdir -p $WORK_HOME/cassandra/saved_caches

# Spark
echo '...Configuring Spark...'
mkdir -p $LOGS_HOME/spark/spark-events
ln -s $CONFIG_HOME/spark/spark-defaults.conf $SPARK_HOME/conf
ln -s $CONFIG_HOME/spark/spark-env.sh $SPARK_HOME/conf
ln -s $CONFIG_HOME/spark/slaves $SPARK_HOME/conf
ln -s $CONFIG_HOME/spark/metrics.properties $SPARK_HOME/conf
ln -s $CONFIG_HOME/spark/fairscheduler.xml $SPARK_HOME/conf
ln -s $CONFIG_HOME/spark/hive-site.xml $SPARK_HOME/conf
ln -s $MYSQL_CONNECTOR_JAR $SPARK_HOME/lib

# Flink
echo '...Configuring Flink...'
mkdir -p $LOGS_HOME/flink
mkdir -p $WORK_HOME/flink/tmp
mkdir -p $WORK_HOME/flink/zookeeper/recovery
mv $FLINK_HOME/conf/slaves $FLINK_HOME/conf/slaves.orig
mv $FLINK_HOME/conf/masters $FLINK_HOME/conf/masters.orig
mv $FLINK_HOME/conf/flink-conf.yaml $FLINK_HOME/conf/flink-conf.yaml.orig
mv $FLINK_HOME/conf/zoo.cfg $FLINK_HOME/conf/zoo.cfg.orig
ln -s $CONFIG_HOME/flink/slaves $FLINK_HOME/conf
ln -s $CONFIG_HOME/flink/masters $FLINK_HOME/conf
ln -s $CONFIG_HOME/flink/hive-site.xml $FLINK_HOME/conf
ln -s $CONFIG_HOME/flink/flink-conf.yaml $FLINK_HOME/conf
ln -s $CONFIG_HOME/flink/zoo.cfg $FLINK_HOME/conf

# Tachyon
echo '...Configuring Tachyon...'
ln -s $CONFIG_HOME/tachyon/tachyon-env.sh $TACHYON_HOME/conf

# Kafka
echo '...Configuring Confluent Kafka...'
mv $CONFLUENT_HOME/etc/ $CONFLUENT_HOME/etc.orig
ln -s $CONFIG_HOME/kafka/etc $CONFLUENT_HOME/etc

# ZooKeeper
echo '...Configuring ZooKeeper...'

# ElasticSearch
echo '...Configuring ElasticSearch...'
mv $ELASTICSEARCH_HOME/config/elasticsearch.yml $ELASTICSEARCH_HOME/config/elasticsearch.yml.orig
mv $ELASTICSEARCH_HOME/config/logging.yml $ELASTICSEARCH_HOME/config/logging.yml.orig
mv $ELASTICSEARCH_HOME/config/scripts $ELASTICSEARCH_HOME/config/scripts.orig
ln -s $CONFIG_HOME/elasticsearch/elasticsearch.yml $ELASTICSEARCH_HOME/config
ln -s $CONFIG_HOME/elasticsearch/logging.yml $ELASTICSEARCH_HOME/config
ln -sf $CONFIG_HOME/elasticsearch/scripts $ELASTICSEARCH_HOME/config

# Logstash
echo '...Configuring Logstash...'
ln -s $CONFIG_HOME/logstash/logstash.conf $LOGSTASH_HOME

# Kibana
echo '...Configuring Kibana...'

# Hadoop HDFS
echo '...Configuring Hadoop HDFS...'

# Hadoop Hive
echo '...Configuring Hadoop Hive...'
ln -s $CONFIG_HOME/hive/hive-site.xml $HIVE_HOME/conf
ln -s $MYSQL_CONNECTOR_JAR $HIVE_HOME/lib

# Redis
echo '...Configuring Redis...'
mv $REDIS_HOME/redis.conf $REDIS_HOME/redis.conf.orig
ln -s $CONFIG_HOME/redis/redis.conf $REDIS_HOME

# Webdis
echo '...Configuring Webdis...'

# SBT
echo '...Configuring SBT...'

# Zeppelin
echo '...Configuring Zeppelin...'
chmod a+x $ZEPPELIN_HOME/bin/*.sh
ln -s $CONFIG_HOME/zeppelin/zeppelin-env.sh $ZEPPELIN_HOME/conf
ln -s $CONFIG_HOME/zeppelin/zeppelin-site.xml $ZEPPELIN_HOME/conf
ln -s $CONFIG_HOME/zeppelin/interpreter.json $ZEPPELIN_HOME/conf
ln -s $CONFIG_HOME/spark/hive-site.xml $ZEPPELIN_HOME/conf
ln -s $MYSQL_CONNECTOR_JAR $ZEPPELIN_HOME/lib

# iPython/Jupyter
echo '...Configuring iPython/Jupyter...'
mkdir -p $WORK_HOME/jupyter
mkdir -p ~/.jupyter

# JupyterHub
echo '...Configuring JupyterHub...'
mkdir -p $WORK_HOME/jupyterhub

# Nifi
echo '...Configuring NiFi...'
mv $NIFI_HOME/conf/nifi.properties $NIFI_HOME/conf/nifi.properties.orig
mv $NIFI_HOME/conf/logback.xml $NIFI_HOME/conf/logback.xml.orig
mv $NIFI_HOME/conf/bootstrap.conf $NIFI_HOME/conf/bootstrap.conf.orig
mv $NIFI_HOME/conf/state-management.xml $NIFI_HOME/conf/state-management.xml.orig
ln -s $CONFIG_HOME/nifi/nifi.properties $NIFI_HOME/conf
ln -s $CONFIG_HOME/nifi/logback.xml $NIFI_HOME/conf
ln -s $CONFIG_HOME/nifi/bootstrap.conf $NIFI_HOME/conf
ln -s $CONFIG_HOME/nifi/state-management.xml $NIFI_HOME/conf
mkdir -p $LOGS_HOME/nifi
mkdir -o $WORK_HOME/nifi

# Airflow
echo '...Configuring Airflow...'
mkdir -p $AIRFLOW_HOME
mkdir -p $MYAPPS_HOME/airflow
mv $AIRFLOW_HOME/airflow.cfg $AIRFLOW_HOME/airflow.cfg.orig
ln -s $CONFIG_HOME/airflow/airflow.cfg $AIRFLOW_HOME
echo '...****Ignore the ERROR 2002s Below****...'
service mysql start
mysql --user=root --password=password -e "CREATE DATABASE IF NOT EXISTS airflow"
airflow initdb
service mysql stop
echo '...****Ignore the ERROR 2002s Above****...'

# Presto
echo '...Configuring Presto...'
mkdir -p $WORK_HOME/presto
mkdir -p $PRESTO_HOME/etc
ln -s $CONFIG_HOME/presto/* $PRESTO_HOME/etc

# Titan
echo '...Configuring Titan...'
mv $TITAN_HOME/conf/titan-cassandra-es.properties $TITAN_HOME/conf/titan-cassandra-es.properties.orig
mv $TITAN_HOME/bin/cassandra $TITAN_HOME/bin/cassandra.orig
mv $TITAN_HOME/bin/cassandra.in.sh $TITAN_HOME/bin/cassandra.in.sh.orig
mv $TITAN_HOME/bin/elasticsearch $TITAN_HOME/bin/elasticsearch.orig
mv $TITAN_HOME/bin/elasticsearch.in.sh $TITAN_HOME/bin/elasticsearch.in.sh.orig
mv $TITAN_HOME/bin/nodetool $TITAN_HOME/bin/nodetool.orig
ln -s $CONFIG_HOME/titan/gremlin-server/gremlin-server-modern.yaml $TITAN_HOME/conf/gremlin-server
ln -s $CONFIG_HOME/titan/gremlin-server/gremlin-server-neo4j.yaml $TITAN_HOME/conf/gremlin-server
ln -s $CONFIG_HOME/titan/gremlin-server/gremlin-server-rest-modern.yaml $TITAN_HOME/conf/gremlin-server
ln -s $CONFIG_HOME/titan/gremlin-server/gremlin-server-spark.yaml $TITAN_HOME/conf/gremlin-server
ln -s $CONFIG_HOME/titan/gremlin-server/neo4j-empty.properties $TITAN_HOME/conf/gremlin-server
ln -s $CONFIG_HOME/titan/gremlin-server/tinkergraph-empty.properties $TITAN_HOME/conf/gremlin-server
ln -s $CONFIG_HOME/titan/titan-cassandra-es.properties $TITAN_HOME/conf/
mkdir -p $LOGS_HOME/gremlin

# TensorFlow
echo ...Configuring TensorFlow...
mkdir -p $LOGS_HOME/tensorflow/serving/
cd /root/pipeline/datasets/tensorflow/serving/inception_model
tar -xvzf 00157585.tgz
cd $PIPELINE_HOME

# Dynomite 
echo ...Configuring Dynomite... 
mkdir -p $LOGS_HOME/dynomite
mv $DYNOMITE_HOME/conf/dynomite.yml $DYNOMITE_HOME/conf/dynomite.yml.orig
ln -s $CONFIG_HOME/dynomite/dynomite.yml $DYNOMITE_HOME/conf/
cd $PIPELINE_HOME

# Atlas 
echo ...Configuring Atlas... 
mkdir -p $LOGS_HOME/atlas
mkdir -p $ATLAS_HOME/conf
ln -s $CONFIG_HOME/atlas/atlas.conf $ATLAS_HOME/conf/
cd $PIPELINE_HOME

# SSH (Part 2/2)
echo '...Configuring SSH Part 2 of 2...'
# We need to keep the SSH service running for other services to be configured above
service ssh stop
