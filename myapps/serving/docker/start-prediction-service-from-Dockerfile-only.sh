#!/bin/bash

###################################################################################################
# NOTE:  THIS SCRIPT MUST NOT EXIT OR ELSE PID 1 WILL DIE AND THE DOCKER CONTAINER WILL DIE
#        RIGHT NOW, WE'RE RLYING ON THE VERY LAST LINE OF start-serving-services.sh TO STAY ALIVE
###################################################################################################

# Derived from the following
#   https://github.com/fluxcapacitor/pipeline/blob/master/config/bash/pipeline.bashrc
#   https://github.com/fluxcapacitor/pipeline/blob/master/bin/setup/config-services-before-starting.sh
#   https://github.com/fluxcapacitor/pipeline/blob/master/bin/service/start-all-services.sh
#
# Get any last minute changes
cd /root/pipeline
git pull

source /root/pipeline/config/bash/pipeline.bashrc

echo '...Configuring Datasets...'
#bzip2 -d -k $DATASETS_HOME/dating/genders.json.bz2
#bzip2 -d -k $DATASETS_HOME/dating/genders.csv.bz2
#bzip2 -d -k $DATASETS_HOME/dating/ratings.json.bz2
#bzip2 -d -k $DATASETS_HOME/dating/ratings.csv.bz2
bzip2 -d -k $DATASETS_HOME/movielens/ml-latest/movies.csv.bz2
cat $DATASETS_HOME/movielens/ml-latest/ratings.csv.bz2-part-* > $DATASETS_HOME/movielens/ml-latest/ratings.csv.bz2
bzip2 -d -k $DATASETS_HOME/movielens/ml-latest/ratings.csv.bz2
#tar -xjf $DATASETS_HOME/dating/genders-partitioned.parquet.tar.bz2 -C $DATASETS_HOME/dating/
#tar -xjf $DATASETS_HOME/dating/genders-unpartitioned.parquet.tar.bz2 -C $DATASETS_HOME/dating
#tar -xjf $DATASETS_HOME/dating/ratings-partitioned.parquet.tar.bz2 -C $DATASETS_HOME/dating
#tar -xjf $DATASETS_HOME/dating/ratings-unpartitioned.parquet.tar.bz2 -C $DATASETS_HOME/dating/
#tar -xjf $DATASETS_HOME/dating/genders-partitioned.orc.tar.bz2 -C $DATASETS_HOME/dating/
#tar -xjf $DATASETS_HOME/dating/genders-unpartitioned.orc.tar.bz2 -C $DATASETS_HOME/dating/
#tar -xjf $DATASETS_HOME/dating/ratings-partitioned.orc.tar.bz2 -C $DATASETS_HOME/dating/
#tar -xjf $DATASETS_HOME/dating/ratings-unpartitioned.orc.tar.bz2 -C $DATASETS_HOME/dating/
#tar -xjf $DATASETS_HOME/dating/genders-partitioned.avro.tar.bz2 -C $DATASETS_HOME/dating/
#tar -xjf $DATASETS_HOME/dating/genders-unpartitioned.avro.tar.bz2 -C $DATASETS_HOME/dating/
#tar -xjf $DATASETS_HOME/dating/ratings-partitioned.avro.tar.bz2 -C $DATASETS_HOME/dating/
#tar -xjf $DATASETS_HOME/dating/ratings-unpartitioned.avro.tar.bz2 -C $DATASETS_HOME/dating/
tar --directory $DATASETS_HOME/serving/recommendations/spark-1.6.1/ -xzvf $DATASETS_HOME/serving/recommendations/spark-1.6.1/als.tar.gz
#tar --directory $DATASETS_HOME/notmnist/ -xzvf $DATASETS_HOME/notmnist/notMNIST_small.tar.gz

# these part files were created with the following command:
#   split --bytes=100MB --numeric-suffixes --suffix-length=1 lfw-deepfunneled.tgz lfw-deepfunneled.tgz-part-
cat $DATASETS_HOME/eigenface/lfw-deepfunneled.tgz-part-* > $DATASETS_HOME/eigenface/lfw-deepfunneled.tgz
tar --directory $DATASETS_HOME/eigenface/ -xzvf $DATASETS_HOME/eigenface/lfw-deepfunneled.tgz

echo '...Configuring Tools...'

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
mkdir -p $LOGS_HOME/serving/tensorflow

echo '...Configuring Redis...'
mkdir -p $LOGS_HOME/redis
mv $REDIS_HOME/redis.conf $REDIS_HOME/redis.conf.orig
ln -s $CONFIG_HOME/redis/redis.conf $REDIS_HOME

echo ...Configuring Dynomite...
mkdir -p $LOGS_HOME/dynomite
mv $DYNOMITE_HOME/conf/dynomite.yml $DYNOMITE_HOME/conf/dynomite.yml.orig
ln -s $CONFIG_HOME/dynomite/dynomite.yml $DYNOMITE_HOME/conf/

echo '...Configuring Spark...'
mkdir -p $LOGS_HOME/spark
mkdir -p $LOGS_HOME/spark/spark-events
ln -s $CONFIG_HOME/spark/spark-defaults.conf $SPARK_HOME/conf
ln -s $CONFIG_HOME/spark/spark-env.sh $SPARK_HOME/conf
ln -s $CONFIG_HOME/spark/slaves $SPARK_HOME/conf
ln -s $CONFIG_HOME/spark/metrics.properties $SPARK_HOME/conf
ln -s $CONFIG_HOME/spark/fairscheduler.xml $SPARK_HOME/conf
ln -s $CONFIG_HOME/spark/hive-site.xml $SPARK_HOME/conf
ln -s $MYSQL_CONNECTOR_JAR $SPARK_HOME/lib

echo '...Configuring Hive...'
ln -s $CONFIG_HOME/hive/hive-site.xml $HIVE_HOME/conf
ln -s $MYSQL_CONNECTOR_JAR $HIVE_HOME/lib

################################################################################################
# TODO:  Make the startup of this configurable based on docker run -e ENV argument (standalone or cluster)
#echo '...Starting Spark Master...'
#nohup $SPARK_HOME/sbin/start-master.sh --webui-port 6060 -h 0.0.0.0 
#echo '...tail -f $LOGS_HOME/spark/spark--org.apache.spark.deploy.master.Master-1-$HOSTNAME.out...'
#tail -f $LOGS_HOME/spark/spark--org.apache.spark.deploy.master.Master-1-$HOSTNAME.out

#echo '...Starting Jupyter Notebook Server...'
# Note:  We are using pipeline-pyspark-shell.sh to pick up the --repositories, --jars, --packages of the rest of the environment
#PYSPARK_DRIVER_PYTHON="jupyter" PYSPARK_DRIVER_PYTHON_OPTS="notebook --config=$CONFIG_HOME/jupyter/jupyter_notebook_config.py" nohup pipeline-pyspark-shell.sh &

#echo '...Starting Zeppelin...'
#nohup $ZEPPELIN_HOME/bin/zeppelin-daemon.sh start

#echo '...Starting Redis...'
#cd $PIPELINE_HOME
#nohup redis-server $REDIS_HOME/redis.conf &

#echo '...Starting Dynomite...'
#cd $PIPELINE_HOME
#dynomite -d -c $DYNOMITE_HOME/conf/dynomite.yml
################################################################################################

echo '...Starting Spark Worker...'
# $SPARK_MASTER_HOST and $SPARK_MASTER_PORT must be passed in using `docker run -e SPARK_MASTER_HOST=<ip> -e SPARK_MASTER_PORT=<port>
# TODO:  Add a flag to disable Spark - or just remove altogether - since it's not needed for Spark or TensorFlow Serving
#        Though it is kinda fun for large cluster demos 
cd $PIPELINE_HOME
#nohup $SPARK_HOME/sbin/start-slave.sh --cores $SPARK_WORKER_CORES --memory $SPARK_WORKER_MEMORY --webui-port 6061 -h 0.0.0.0 spark://$SPARK_MASTER_HOST:$SPARK_MASTER_PORT
#nohup $SPARK_HOME/sbin/start-slave.sh --cores 8 --memory 50g --webui-port 6061 -h 0.0.0.0 spark://<master-ip>
echo '...tail -f $LOGS_HOME/spark/spark--org.apache.spark.deploy.worker.Worker-1-$HOSTNAME.out...'

echo '...Configuring TensorFlow...'
mkdir -p $LOGS_HOME/tensorflow/serving/
cd $DATASETS_HOME/tensorflow/serving/inception_model
tar -xvzf 00157585.tgz

echo '...Starting TensorFlow Serving Inception Service...'
$MYAPPS_HOME/serving/tensorflow/start-tensorflow-serving-inception-service.sh

echo '...Starting TensorFlow Serving Inception Service Proxy...'
$MYAPPS_HOME/serving/tensorflow/start-tensorflow-serving-inception-service-proxy.sh

echo '...Starting TensorFlow Serving Inception Sidecar Inception Service...'
$MYAPPS_HOME/serving/tensorflow/start-tensorflow-serving-inception-sidecar-service.sh

echo '...Starting Prediction Service...'
cd $MYAPPS_HOME/serving/prediction
sbt assembly
java -Djava.security.egd=file:/dev/./urandom -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.serving.prediction.PredictionServiceMain"

###################################################################################################
# NOTE:  THIS SCRIPT MUST NOT EXIT OR ELSE PID 1 WILL DIE AND THE DOCKER CONTAINER WILL DIE
#        RIGHT NOW, WE'RE RiELYING ON THE VERY LAST LINE TO STAY ALIVE
###################################################################################################
