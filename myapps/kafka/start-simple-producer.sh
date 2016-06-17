nohup sbt "run-main org.advancedspark.kafka.simple.SimpleProducer" 2>&1 1>$PIPELINE_HOME/logs/kafka/kafka-producer.log &
echo '...logs available with "tail -f $PIPELINE_HOME/logs/kafka/kafka-producer.log"' 
