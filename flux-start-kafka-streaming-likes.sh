cd $PIPELINE_HOME/myapps
echo '...Building and Packaging Streaming App...'
sbt streaming/package

echo '...Starting Spark Likes Streaming App...'
nohup spark-submit --packages org.apache.spark:spark-streaming-kafka-assembly_2.10:1.4.1,org.elasticsearch:elasticsearch-spark_2.10:2.1.0,com.datastax.spark:spark-cassandra-connector_2.10:1.4.0,redis.clients:jedis:2.7.3 --class com.fluxcapacitor.pipeline.spark.streaming.StreamingLikes $PIPELINE_HOME/myapps/streaming/target/scala-2.10/streaming_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/streaming/streaming-likes-out.log &
echo '...logs available with tail -f $PIPELINE_HOME/logs/streaming/streaming-likes-out.log...'
cd $PIPELINE_HOME
