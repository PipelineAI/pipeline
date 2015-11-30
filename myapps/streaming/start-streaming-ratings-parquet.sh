echo '...Building and Packaging Streaming App...'
sbt package

echo '...Starting Spark Streaming App:  Store Raw Ratings on Disk in Parquet File Format...'
nohup spark-submit --jars $MYSQL_CONNECTOR_JAR --packages $SPARK_SUBMIT_PACKAGES --class com.advancedspark.streaming.rating.store.Parquet $PIPELINE_HOME/myapps/streaming/target/scala-2.10/streaming_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/streaming/ratings-parquet.log &
echo '...logs available with "tail -f $PIPELINE_HOME/logs/streaming/ratings-parquet.log"'
