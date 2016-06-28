#!/bin/bash
source /root/pipeline/config/bash/pipeline.bashrc

mkdir -p $LOGS_HOME/setup

cd $PIPELINE_HOME
$SCRIPTS_HOME/setup/config-services-before-starting.sh

echo '...Starting Redis...'
cd $PIPELINE_HOME
nohup redis-server & 

echo '...Starting Dynomite...'
cd $PIPELINE_HOME
dynomite -d -c $DYNOMITE_HOME/conf/dynomite.yml

cd $MYAPPS_HOME/serving/prediction/
sbt assembly
java -Djava.security.egd=file:/dev/./urandom -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.serving.prediction.PredictionServiceMain"
