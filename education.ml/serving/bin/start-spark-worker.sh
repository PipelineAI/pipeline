#!/bin/bash

SPARK_WORKER_WEBUI_PORT=46061 $SPARK_HOME/sbin/start-slave.sh --cores 4 --memory 2g -h 0.0.0.0 spark://127.0.0.1:47077 &

#tail -f $SPARK_HOME/logs/spark--org.apache.spark.deploy.worker.Worker-1-$HOSTNAME.out
