nohup sbt "run-main com.advancedspark.kafka.simple.SimpleConsumer" 2>&1 1>$PIPELINE_HOME/logs/kafka/kafka-consumer.log &
echo 'logs available at "tail -f $PIPELINE_HOME/logs/kafka/kafka-consumer.log"'
