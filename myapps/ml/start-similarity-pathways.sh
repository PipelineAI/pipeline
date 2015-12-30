#!/bin/bash
#
echo '...Building...'
sbt package 
echo '...Starting...'
nohup spark-submit --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES --class com.advancedspark.ml.graph.SimilarityPathways $PIPELINE_HOME/myapps/ml/target/scala-2.10/ml_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/ml/similarity-pathways.log &
echo '...logs available with "tail -f $PIPELINE_HOME/logs/ml/similarity-pathways.log"'
