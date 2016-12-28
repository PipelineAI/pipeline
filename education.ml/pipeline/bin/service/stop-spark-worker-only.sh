echo '...Stopping Spark Worker...'
$SPARK_HOME/sbin/stop-slave.sh --webui-port 6061

echo '...tail -f $LOGS_HOME/spark/spark--org.apache.spark.deploy.worker.Worker-1-$HOSTNAME.out...'
tail -f $LOGS_HOME/spark/spark--org.apache.spark.deploy.worker.Worker-1-$HOSTNAME.out
