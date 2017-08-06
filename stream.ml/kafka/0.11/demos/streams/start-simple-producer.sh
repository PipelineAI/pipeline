nohup sbt "run-main io.pipeline.kafka.simple.SimpleProducer" 2>&1 1>$LOGS_HOME/kafka-producer.log &
echo "logs available at 'tail -f $LOGS_HOME/kafka-producer.log'"
