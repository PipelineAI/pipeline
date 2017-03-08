#!/bin/bash
#
echo '...Starting Spark Streaming App:  Store Approx Rating Counts using Algebird CountMin Sketch (count)...'
nohup spark-submit --repositories $SPARK_REPOSITORIES --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES --class com.advancedspark.streaming.rating.approx.AlgebirdCountMinSketchTopK $PIPELINE_HOME/myapps/spark/streaming/target/scala-2.10/streaming_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/spark/streaming/ratings-algebird-cms-topk.log &
echo '...logs available with tail -f $PIPELINE_HOME/logs/spark/streaming/ratings-algebird-cms-topk.log | grep CMS'

