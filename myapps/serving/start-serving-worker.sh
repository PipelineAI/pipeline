echo '...Starting Prediction Service...'
# TODO:  Start this in the background with nohup &
cd $MYAPPS_HOME/serving/prediction
sbt package
java -Djava.security.egd=file:/dev/./urandom -jar ~/sbt/bin/sbt-launch.jar "run-main com.advancedspark.serving.prediction.PredictionServiceMain"

