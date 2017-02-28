#!/bin/bash

SPARK_MASTER_PORT=47077 SPARK_MASTER_WEBUI_PORT=46060 $SPARK_HOME/sbin/start-master.sh &

tail -f $SPARK_HOME/logs/spark--org.apache.spark.deploy.master.Master-1-$HOSTNAME.out
