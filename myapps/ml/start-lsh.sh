#!/bin/bash
#
echo '...Building LSH App...'
sbt package 
echo '...Starting LSH App...'
nohup spark-submit --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES --class com.advancedspark.ml.lsh.ItemLSH $PIPELINE_HOME/myapps/ml/target/scala-2.10/ml_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/ml/lsh-item.log &
echo '...logs available with "tail -f $PIPELINE_HOME/logs/ml/lsh-item.log"'
