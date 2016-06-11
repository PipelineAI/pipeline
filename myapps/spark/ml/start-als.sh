#!/bin/bash
#
echo '...Starting ALS...'
echo '...Note:  This is using a small ratings-sm.csv dataset to speed up the demo...'
echo '...       For more accurate predictions, change the dataset in the code to use the full ratings.csv...'
echo '...       However, expect the job to run MUCH longer with the full ratings.csv...'
nohup spark-submit --repositories $SPARK_REPOSITORIES --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES --class com.advancedspark.ml.recommendation.ALS $MYAPPS_HOME/spark/ml/target/scala-2.10/ml_2.10-1.0.jar 2>&1 1>$LOGS_HOME/spark/ml/als.log &

echo '...logs available with "tail -f $LOGS_HOME/spark/ml/als.log"'
