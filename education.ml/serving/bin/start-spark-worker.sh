#!/bin/bash

SPARK_WORKER_WEBUI_PORT=46060 $SPARK_HOME/sbin/start-slave.sh --cores 1 --memory 512m --webui-port 46061 spark://127.0.0.1:47077 &

#tail -f $SPARK_HOME/logs/spark--org.apache.spark.deploy.worker.Worker-1-$HOSTNAME.out
