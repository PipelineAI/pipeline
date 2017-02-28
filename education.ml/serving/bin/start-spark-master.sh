#!/bin/bash

nohup $SPARK_HOME/sbin/start-master.sh --webui-port 46060 &

tail -f $SPARK_HOME/logs/spark--org.apache.spark.deploy.master.Master-1-$HOSTNAME.out
