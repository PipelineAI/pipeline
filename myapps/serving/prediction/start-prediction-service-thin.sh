#!/bin/bash
source /root/pipeline/config/bash/pipeline.bashrc

echo '...Configuring Redis...'
mv $REDIS_HOME/redis.conf $REDIS_HOME/redis.conf.orig
ln -s $CONFIG_HOME/redis/redis.conf $REDIS_HOME

echo '...Starting Redis...'
cd $PIPELINE_HOME
nohup redis-server $REDIS_HOME/redis.conf & 

echo ...Configuring Dynomite...
mkdir -p $LOGS_HOME/dynomite
mv $DYNOMITE_HOME/conf/dynomite.yml $DYNOMITE_HOME/conf/dynomite.yml.orig
ln -s $CONFIG_HOME/dynomite/dynomite.yml $DYNOMITE_HOME/conf/
cd $PIPELINE_HOME

echo '...Starting Dynomite...'
cd $PIPELINE_HOME
dynomite -d -c $DYNOMITE_HOME/conf/dynomite.yml

cd $MYAPPS_HOME/serving/prediction/
sbt assembly
java -Djava.security.egd=file:/dev/./urandom -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.serving.prediction.PredictionServiceMain"
