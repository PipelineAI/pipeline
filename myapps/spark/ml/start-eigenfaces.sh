#!/bin/bash
#
echo '...Building Eigenfaces App...'
sbt package 
ech '...Starting Eigenfaces App...'

spark-submit --executor-memory 32g --total-executor-cores 8 --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES --class com.advancedspark.ml.image.Eigenfaces $MYAPPS_HOME/spark/ml/target/scala-2.10/ml_2.10-1.0.jar /root/pipeline/datasets/eigenface/lfw-deepfunneled/Jennifer_Capriati/ /tmp/Jennifer_Capriati/ 50 50 10 2>&1 1>$LOGS_HOME/spark/ml/images-eigenfaces.log 
echo '...'
echo '...logs available with "tail -f $LOGS_HOME/spark/ml/images-eigenfaces.log"'
echo '...'
