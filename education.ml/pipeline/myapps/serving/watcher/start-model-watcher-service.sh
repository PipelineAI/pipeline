echo '...Starting Watcher...'
nohup java -jar target/scala-2.10/watcher-assembly-1.0.jar > $LOGS_HOME/serving/watcher/watcher.log &
echo '...tail -f $LOGS_HOME/serving/watcher/watcher.log...'
