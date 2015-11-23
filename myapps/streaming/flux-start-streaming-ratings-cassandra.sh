echo '...Building and Packaging Streaming App...'
sbt streaming/package

echo '...Starting Spark Streaming App:  Store Raw Ratings in Cassandra...'
nohup spark-submit --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES --class com.advancedspark.pipeline.spark.streaming.RatingsCassandra $PIPELINE_HOME/myapps/streaming/target/scala-2.10/streaming_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/streaming/ratings-cassandra.log &
echo '...logs available with "tail -f $PIPELINE_HOME/logs/streaming/ratings-cassandra.log"'
