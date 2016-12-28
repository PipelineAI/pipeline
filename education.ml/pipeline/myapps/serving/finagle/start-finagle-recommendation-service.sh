sbt assembly
cd $MYAPPS_HOME/serving/finagle/
nohup java -jar target/scala-2.10/finagle-assembly-1.0.jar > $LOGS_HOME/serving/finagle/finagle.log &
echo '...tail -f $LOGS_HOME/serving/finagle/finagle.log...'
