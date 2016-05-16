sbt assembly
cd $MYAPPS_HOME/serving/watcher/
nohup java -jar target/scala-2.10/watcher-assembly-1.0.jar > $LOGS_HOME/watcher/watcher.log &
echo '...tail -f $LOGS_HOME/watcher/watcher.log...'
