echo '...Building and Packaging Streaming App...'
sbt streaming/package

echo '...Starting Spark Streaming App:  Store Exact Likes in Cassandra and Redis...'
nohup spark-submit --packages $PACKAGES --class com.fluxcapacitor.pipeline.spark.streaming.LikesExact $PIPELINE_HOME/myapps/streaming/target/scala-2.10/streaming_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/streaming/likes-exact.log &
echo '...logs available with "tail -f $PIPELINE_HOME/logs/streaming/likes-exact.log"'
