nohup sbt "run-main org.advancedspark.kafka.simple.SimpleConsumer" 2>&1 1>$PIPELINE_HOME/logs/kafka/kafka-consumer.log &
echo '...logs available with "tail -f $PIPELINE_HOME/logs/kafka/kafka-consumer.log"' 
