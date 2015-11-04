echo '...Building and Packaging Streaming App...'
sbt streaming/package

echo '...Starting Spark Streaming App:  Store Likes in CountMin Sketch...'
nohup spark-submit --packages $PACKAGES --class com.fluxcapacitor.pipeline.spark.streaming.LikesCountMinSketch $PIPELINE_HOME/myapps/streaming/target/scala-2.10/streaming_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/streaming/likes-countminsketch.log &
echo '...logs available with "tail -f $PIPELINE_HOME/logs/streaming/likes-countminsketch.log"'
