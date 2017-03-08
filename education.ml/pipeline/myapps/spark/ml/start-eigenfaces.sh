#!/bin/bash
#
echo '...Starting Eigenfaces App...'

# Notes:  
#  --executor-memory is PER NODE
#    If you specify a value that exceeds the maximum available on any single node
#    the Spark App will not start
#  --total-executor-cores is across the whole cluster (not per node)
spark-submit --repositories $SPARK_REPOSITORIES --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES --class com.advancedspark.ml.image.Eigenfaces $MYAPPS_HOME/spark/ml/target/scala-2.10/ml_2.10-1.0.jar /root/pipeline/datasets/eigenface/lfw-deepfunneled/George_W_Bush/ /tmp/George_W_Bush/ 50 50 10 2>&1 1>$LOGS_HOME/spark/ml/images-eigenfaces.log 
echo '...'
echo '...logs available with "tail -f $LOGS_HOME/spark/ml/images-eigenfaces.log"'
echo '...'
