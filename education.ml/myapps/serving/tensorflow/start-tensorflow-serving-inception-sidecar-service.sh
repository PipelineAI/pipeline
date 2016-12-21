cd $MYAPPS_HOME/serving/tensorflow
nohup java -Djava.security.egd=file:/dev/./urandom -jar target/tensorflow-serving-service-0.0.1-SNAPSHOT.jar > $LOGS_HOME/serving/tensorflow-serving-service.log & 
echo '...tail -f $LOGS_HOME/serving/tensorflow-serving-service.log...'
