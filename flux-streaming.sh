echo Starting Feeder
spark-submit --class com.fluxcapacitor.pipeline.spark.streaming.StreamingRatings $PIPELINE_HOME/target/scala-1.10/feedsimulator_2.10-1.0.jar
