nohup java -Djava.security.egd=file:/dev/./urandom -jar target/config-service-0.0.1-SNAPSHOT.jar > $LOGS_HOME/serving/config.log &
echo '...tail -f $LOGS_HOME/serving/config.log...'
