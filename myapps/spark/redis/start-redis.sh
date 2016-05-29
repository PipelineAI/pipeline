echo '...Building and Packaging Redis App...'
sbt package

echo '...Starting Spark Redis App...'

nohup spark-submit --jars $SPARK_SUBMIT_JARS --packages $SPARK_SUBMIT_PACKAGES --class com.advancedspark.spark.redis.Redis $PIPELINE_HOME/myapps/spark/redis/target/scala-2.10/redis_2.10-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/spark/redis/spark-redis.log &

echo '...logs available with "tail -f $PIPELINE_HOME/logs/spark/redis/spark-redis.log"'
