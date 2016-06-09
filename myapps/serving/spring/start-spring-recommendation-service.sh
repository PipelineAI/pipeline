cd $MYAPPS_HOME/serving/spring/
nohup java -jar target/scala-2.10/spring-assembly-1.0.jar > $LOGS_HOME/serving/spring/spring.log &
echo '...tail -f $LOGS_HOME/serving/spring/spring.log...'
