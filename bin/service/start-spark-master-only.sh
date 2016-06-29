#!/bin/bash

cd $PIPELINE_HOME

echo '...Starting Spark Master...'
nohup $SPARK_HOME/sbin/start-master.sh --webui-port 6060 -h 0.0.0.0

echo '...tail -f $LOGS_HOME/spark/spark--org.apache.spark.deploy.master.Master-1-$HOSTNAME.out...'
tail -f $LOGS_HOME/spark/spark--org.apache.spark.deploy.master.Master-1-$HOSTNAME.out
