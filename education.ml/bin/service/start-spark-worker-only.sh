#!/bin/bash
echo 'Usage:  start-spark-worker-only.sh <master-ip>'
cd $PIPELINE_HOME

stop-all-services.sh

echo '...Starting Spark Worker Pointing to Common Spark Master...'
nohup $SPARK_HOME/sbin/start-slave.sh --cores 8 --memory 48g --webui-port 6061 -h 0.0.0.0 spark://$1:7077

echo '...tail -f $LOGS_HOME/spark/spark--org.apache.spark.deploy.worker.Worker-1-$HOSTNAME.out...'
tail -f $LOGS_HOME/spark/spark--org.apache.spark.deploy.worker.Worker-1-$HOSTNAME.out
