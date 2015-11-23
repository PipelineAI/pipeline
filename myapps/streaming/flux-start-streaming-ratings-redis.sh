echo '...Building and Packaging Streaming App...'
sbt streaming/package

echo '...Starting Spark Streaming App:  Store Ratings in Redis as both Exact and Approx (HyperLogLog)...'
nohup spark-submit --packages $SPARK_SUBMIT_PACKAGES --jars $SPARK_SUBMIT_JARS --class com.fluxcapacitor.pipeline.spark.streaming.RatingsRedis $PIPELINE_HOME/myapps/streaming/target/scala-2.10/streaming_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/streaming/ratings-redis.log &
echo '...logs available with "tail -f $PIPELINE_HOME/logs/streaming/ratings-redis.log"'
