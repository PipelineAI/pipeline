echo '...Building and Packaging Streaming App...'
sbt streaming/package

echo '...Starting Spark Streaming App:  Store Approx Rating Counts using Algebird HyperLogLog (distinct count) and CountMin Sketch (count)...'
nohup spark-submit --packages $SPARK_SUBMIT_PACKAGES --class com.advancedspark.pipeline.spark.streaming.RatingsCountMinSketch $PIPELINE_HOME/myapps/streaming/target/scala-2.10/streaming_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/streaming/ratings-algebird.log &
echo '...logs available with "tail -f $PIPELINE_HOME/logs/streaming/ratings-algebird.log"'
