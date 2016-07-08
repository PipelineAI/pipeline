nohup java -Djava.security.egd=file:/dev/./urandom -jar target/sidecar-0.0.1-SNAPSHOT.jar > $LOGS_HOME/serving/sidecar.log & 
echo '...tail -f $LOGS_HOME/serving/sidecar.log...'
