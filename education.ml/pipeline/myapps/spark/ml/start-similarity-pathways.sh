#!/bin/bash
#
echo '...Starting Similarity Pathways...'
nohup spark-submit --repositories $SPARK_REPOSITORIES --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES --class com.advancedspark.ml.graph.SimilarityPathway %MYAPPS_HOME/spark/ml/target/scala-2.10/ml_2.10-1.0.jar 2>&1 1>$LOGS_HOME/spark/ml/similarity-pathway.log &

echo '...logs available with "tail -f $LOGS_HOME/spark/ml/similarity-pathway.log"'
