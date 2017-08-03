nohup sbt "run-main com.advancedspark.kafka.simple.SimpleProducer" 2>&1 1>$PIPELINE_HOME/logs/kafka/kafka-producer.log &
echo 'logs available at "tail -f $PIPELINE_HOME/logs/kafka/kafka-producer.log"'
