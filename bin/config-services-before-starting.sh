# SSH

cd $PIPELINE_HOME

echo '...**** MAKE SURE YOU HAVE SOURCE ~/.profile OR ELSE THIS WILL THROW A LOT OF ERRORS ****...'
echo '...**** IGNORE ANY ERRORS RELATED TO THINGS THAT ALREADY EXIST.  THIS IS OK. ****...'

echo '...Configuring SSH Part 1 of 2...'
service ssh start
ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
mkdir -p ~/.ssh
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
chmod 600 ~/.ssh/id_rsa

# Adding syntax highlighting to VIM
ln -s $PIPELINE_HOME/config/.vim ~/ 

# Apache Httpd
echo '...Configuring Apache Httpd...'
a2enmod proxy
a2enmod proxy_http
a2dissite 000-default
mv /etc/apache2/apache2.conf /etc/apache2/apache2.conf.orig
#rm /etc/apache2/apache2.conf
ln -s $PIPELINE_HOME/config/apache2/apache2.conf /etc/apache2
mkdir -p $PIPELINE_HOME/logs/apache2

# Datasets
echo '...Decompressing Datasets (This takes a while)...'
bzip2 -d -k $DATASETS_HOME/dating/genders.json.bz2
bzip2 -d -k $DATASETS_HOME/dating/genders.csv.bz2
bzip2 -d -k $DATASETS_HOME/dating/ratings.json.bz2
bzip2 -d -k $DATASETS_HOME/dating/ratings.csv.bz2
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
cat $DATASETS_HOME/movielens/ml-latest/ratings.csv.bz2-part-* > $DATASETS_HOME/movielens/ml-latest/ratings.csv.bz2
bzip2 -d -k $DATASETS_HOME/movielens/ml-latest/ratings.csv.bz2
cat $DATASETS_HOME/sort/sort.txt.bz2-part-* > $DATASETS_HOME/sort/sort.txt.bz2
bzip2 -d -k $DATASETS_HOME/sort/sort.txt.bz2

# Sample WebApp
echo '...Configuring Example WebApp...'
#rm /etc/apache2/sites-available/advancedspark.conf
ln -s $PIPELINE_HOME/config/advancedspark.com/advancedspark.conf /etc/apache2/sites-available
a2ensite advancedspark.conf
# We're just copying these under /var/www/html for now
# Ideally, a symlink would be more appropriate, but Apache is being a pain with permissions
cp -R $PIPELINE_HOME/html/advancedspark.com/* /var/www/html
cp -R $PIPELINE_HOME/datasets/items /var/www/html/items

# My Apps
echo '...Configuring  Apps...'
mkdir -p $LOGS_HOME/feeder
mkdir -p $LOGS_HOME/streaming
mkdir -p $LOGS_HOME/nlp
mkdir -p $DATA_HOME/core

# Ganglia
echo '...Configuring Ganglia...'
#rm /etc/apache2/sites-available/ganglia.conf
ln -s $PIPELINE_HOME/config/ganglia/ganglia.conf /etc/apache2/sites-available
a2ensite ganglia
mv /etc/ganglia/gmetad.conf /etc/ganglia/gmetad.conf.orig
mv /etc/ganglia/gmond.conf /etc/ganglia/gmond.conf.orig
#rm /etc/ganglia/gmetad.conf
#rm /etc/ganglia/gmond.conf
ln -s $PIPELINE_HOME/config/ganglia/gmetad.conf /etc/ganglia
ln -s $PIPELINE_HOME/config/ganglia/gmond.conf /etc/ganglia

# MySQL (Required by HiveQL Exercises)
echo '...Configurating MySQL...'
service mysql start
mysqladmin -u root password "password"
echo '...****Ignore the ERROR 2002s Below****...'
nohup service mysql stop
echo '...****Ignore any ERROR 2002s Above****...'

# Cassandra
echo '...Configuring Cassandra...'
mv $CASSANDRA_HOME/conf/cassandra-env.sh $CASSANDRA_HOME/conf/cassandra-env.sh.orig
mv $CASSANDRA_HOME/conf/cassandra.yaml $CASSANDRA_HOME/conf/cassandra.yaml.orig
#rm $CASSANDRA_HOME/conf/cassandra-env.sh
ln -s $PIPELINE_HOME/config/cassandra/cassandra-env.sh $CASSANDRA_HOME/conf
#rm $CASSANDRA_HOME/conf/cassandra.yaml
ln -s $PIPELINE_HOME/config/cassandra/cassandra.yaml $CASSANDRA_HOME/conf
mkdir -p $DATA_HOME/cassandra/data
mkdir -p $DATA_HOME/cassandra/commitlog
mkdir -p $DATA_HOME/cassandra/saved_caches

# Spark
echo '...Configuring Spark...'
mkdir -p $LOGS_HOME/spark/spark-events
#rm $SPARK_HOME/conf/spark-defaults.conf
ln -s $PIPELINE_HOME/config/spark/spark-defaults.conf $SPARK_HOME/conf
#rm $SPARK_HOME/conf/spark-env.sh
ln -s $PIPELINE_HOME/config/spark/spark-env.sh $SPARK_HOME/conf
#rm $SPARK_HOME/conf/slaves
ln -s $PIPELINE_HOME/config/spark/slaves $SPARK_HOME/conf
#rm $SPARK_HOME/conf/metrics.properties
ln -s $PIPELINE_HOME/config/spark/metrics.properties $SPARK_HOME/conf
#rm $SPARK_HOME/conf/hive-site.xml
ln -s $PIPELINE_HOME/config/hadoop/hive-site.xml $SPARK_HOME/conf
#rm $SPARK_HOME/lib/$MYSQL_CONNECTOR_JAR
ln -s $MYSQL_CONNECTOR_JAR $SPARK_HOME/lib

# Kafka
echo '...Configuring Kafka...'

# ZooKeeper
echo '...Configuring ZooKeeper...'

# ElasticSearch
echo '...Configuring ElasticSearch...'

# Logstash
echo '...Configuring Logstash...'
ln -s $PIPELINE_HOME/config/logstash/logstash.conf $LOGSTASH_HOME

# Kibana
echo '...Configuring Kibana...'

# Hadoop HDFS
#echo '...Configuring Docker-local Hadoop HDFS...'

# Redis
echo '...Configuring Redis...'

# Webdis
echo '...Configuring Webdis...'

# Tachyon
echo '...Configuring Tachyon...'
mv $TACHYON_HOME/conf/log4j.properties $TACHYON_HOME/conf/log4j.properties.orig
#rm $TACHYON_HOME/conf/log4j.properties
ln -s $PIPELINE_HOME/config/tachyon/log4j.properties $TACHYON_HOME/conf
#rm $TACHYON_HOME/conf/tachyon-env.sh
ln -s $PIPELINE_HOME/config/tachyon/tachyon-env.sh $TACHYON_HOME/conf
tachyon format
# The following command requies the SSH daemon to be running
# If we switch to use HDFS as the underfs, we'll need the HDFS daemon to be running
# We need to chmod the keys again - not sure why, but it works so let's keep it
chmod 600 ~/.ssh/authorized_keys
chmod 600 ~/.ssh/id_rsa

# SBT
echo '...Configuring SBT...'

# Zeppelin
echo '...Configuring Zeppelin...'
chmod a+x $ZEPPELIN_HOME/bin/*.sh
#rm $ZEPPELIN_HOME/conf/zeppelin-env.sh
ln -s $PIPELINE_HOME/config/zeppelin/zeppelin-env.sh $ZEPPELIN_HOME/conf
#rm $ZEPPELIN_HOME/conf/zeppelin-site.xml
ln -s $PIPELINE_HOME/config/zeppelin/zeppelin-site.xml $ZEPPELIN_HOME/conf
#rm $ZEPPELIN_HOME/conf/interpreter.json
ln -s $PIPELINE_HOME/config/zeppelin/interpreter.json $ZEPPELIN_HOME/conf
#rm $ZEPPELIN_HOME/conf/hive-site.xml
ln -s $PIPELINE_HOME/config/hadoop/hive-site.xml $ZEPPELIN_HOME/conf
#rm $ZEPPELIN_HOME/lib/$MYSQL_CONNECTOR_JAR
ln -s $MYSQL_CONNECTOR_JAR $ZEPPELIN_HOME/lib

# Spark-Notebook
echo '...Configuring Spark-Notebook...'
#rm $SPARK_NOTEBOOK_HOME/notebooks/pipeline
ln -s $PIPELINE_HOME/notebooks/spark-notebook/pipeline $SPARK_NOTEBOOK_HOME/notebooks

# iPython/Jupyter
echo '...Configuring iPython/Jupyter...'

# Spark Job Server
#echo '...Configuring Spark Job Server...'
#rm $SPARK_JOBSERVER_HOME/config/pipeline.conf
#rm $SPARK_JOBSERVER_HOME/config/pipeline.sh
#ln -s $PIPELINE_HOME/config/spark-jobserver/pipeline.conf $SPARK_JOBSERVER_HOME/config 
#ln -s $PIPELINE_HOME/config/spark-jobserver/pipeline.sh $SPARK_JOBSERVER_HOME/config

# SSH (Part 2/2)
echo '...Configuring SSH Part 2 of 2...'
# We need to keep the SSH service running for other services to be configured above
service ssh stop
