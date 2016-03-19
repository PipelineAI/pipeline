echo '...Building and Packaging Streaming App...'
sbt package

echo '...Starting Spark Streaming App:  TopK Items By Rating Count...'
nohup spark-submit --jars $MYSQL_CONNECTOR_JAR --packages $SPARK_SUBMIT_PACKAGES --class com.advancedspark.streaming.rating.agg.TopKItemsByRatingCount $PIPELINE_HOME/myapps/spark/streaming/target/scala-2.10/streaming_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/spark/streaming/ratings-topk-items-by-rating-count.log &
echo '...logs available with "tail -f $PIPELINE_HOME/logs/spark/streaming/ratings-topk-items-by-rating-count.log"'
