nohup java -Djava.security.egd=file:/dev/./urandom -jar target/turbine-0.0.1-SNAPSHOT.jar > $LOGS_HOME/serving/turbine.log & 
echo '...tail -f $LOGS_HOME/serving/turbine.log...'
