nohup sbt "run-main io.pipeline.kafka.simple.SimpleConsumer" 2>&1 1>$LOGS_HOME/kafka-consumer.log &
echo "logs available at 'tail -f $LOGS_HOME/kafka-consumer.log'"
