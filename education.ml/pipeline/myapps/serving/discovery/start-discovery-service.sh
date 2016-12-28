nohup java -Djava.security.egd=file:/dev/./urandom -jar target/discovery-service-0.0.1-SNAPSHOT.jar > $LOGS_HOME/serving/discovery.log & 
echo '...tail -f $LOGS_HOME/serving/discovery.log...'
