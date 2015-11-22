echo '...Building and Packaging Streaming App...'
sbt streaming/package

echo '...Starting Spark Streaming App:  Store Exact Ratings to Cassandra and Redis...'
nohup spark-submit --jars $MYSQL_CONNECTOR_JAR --packages $SPARK_SUBMIT_PACKAGES --class com.fluxcapacitor.pipeline.spark.streaming.RatingsExact $PIPELINE_HOME/myapps/streaming/target/scala-2.10/streaming_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/streaming/ratings-exact.log &
echo '...logs available with "tail -f $PIPELINE_HOME/logs/streaming/ratings-exact.log"'
