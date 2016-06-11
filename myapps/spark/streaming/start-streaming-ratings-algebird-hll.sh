#!/bin/bash
#
echo '...Starting Spark Streaming App:  Store Approx Rating Counts using Algebird HyperLogLog (distinct count)...'
nohup spark-submit --repositories $SPARK_REPOSITORIES --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES --class com.advancedspark.streaming.rating.approx.AlgebirdHyperLogLog $PIPELINE_HOME/myapps/spark/streaming/target/scala-2.10/streaming_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/spark/streaming/ratings-algebird-hll.log &
echo '...logs available with tail -f $PIPELINE_HOME/logs/spark/streaming/ratings-algebird-hll.log | grep HLL'
