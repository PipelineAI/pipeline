sbt assembly
nohup java -jar target/scala-2.10/finagle-assembly-1.0.jar > $LOGS_HOME/finagle/finagle.log &
