#!/bin/bash
source /root/pipeline/config/bash/pipeline.bashrc

echo '...Configuring Tools...'
export DEV_INSTALL_HOME=~

# Pipeline Home
export PIPELINE_HOME=$DEV_INSTALL_HOME/pipeline

# Config Home
export CONFIG_HOME=$PIPELINE_HOME/config

# Logs Home
export LOGS_HOME=$PIPELINE_HOME/logs
mkdir -p $LOGS_HOME

# Dynomite Home
export DYNOMITE_HOME=$DEV_INSTALL_HOME/dynomite

echo '...Configuring Redis...'
export REDIS_HOME=$DEV_INSTALL_HOME/redis-$REDIS_VERSION
export PATH=$REDIS_HOME/bin:$PATH
mkdir -p $LOGS_HOME/redis
mv $REDIS_HOME/redis.conf $REDIS_HOME/redis.conf.orig
ln -s $CONFIG_HOME/redis/redis.conf $REDIS_HOME

echo '...Starting Redis...'
cd $PIPELINE_HOME
nohup redis-server $REDIS_HOME/redis.conf & 

echo ...Configuring Dynomite...
export DYNOMITE_HOME=$DEV_INSTALL_HOME/dynomite
export PATH=$DYNOMITE_HOME:$PATH
mkdir -p $LOGS_HOME/dynomite
mv $DYNOMITE_HOME/conf/dynomite.yml $DYNOMITE_HOME/conf/dynomite.yml.orig
ln -s $CONFIG_HOME/dynomite/dynomite.yml $DYNOMITE_HOME/conf/

echo '...Starting Dynomite...'
cd $PIPELINE_HOME
dynomite -d -c $DYNOMITE_HOME/conf/dynomite.yml

cd $MYAPPS_HOME/serving/prediction/
sbt assembly
java -Djava.security.egd=file:/dev/./urandom -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.serving.prediction.PredictionServiceMain"
