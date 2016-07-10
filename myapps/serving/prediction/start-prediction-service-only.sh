#nohup java -Djava.security.egd=file:/dev/./urandom -jar target/prediction-service-0.0.1-SNAPSHOT.jar > $LOGS_HOME/serving/prediction.log &
java -Djava.security.egd=file:/dev/./urandom -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.serving.prediction.PredictionServiceMain"
#echo '...tail -f $LOGS_HOME/serving/prediction.log...'
