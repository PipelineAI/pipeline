#!/bin/sh
cd $MYAPPS_HOME/serving/prediction/

#nohup sbt run > $LOGS_HOME/serving/prediction/prediction.log &

nohup java -Djava.security.egd=file:/dev/./urandom -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.serving.prediction.PredictionServiceMain" > $LOGS_HOME/serving/prediction/prediction.log &
echo '...tail -f $LOGS_HOME/serving/prediction/prediction.log...'
