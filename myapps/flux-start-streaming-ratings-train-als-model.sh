echo '...Building and Packaging Streaming App...'
sbt streaming/package

echo '...Starting Spark Streaming App:  Use Ratings to Train ALS Model...'
nohup spark-submit --packages $PACKAGES --class com.fluxcapacitor.pipeline.spark.streaming.RatingsTrainALSModel $PIPELINE_HOME/myapps/streaming/target/scala-2.10/streaming_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/streaming/ratings-train-als-model.log &
echo '...logs available with "tail -f $PIPELINE_HOME/logs/streaming/ratings-train-als-model.log"'
