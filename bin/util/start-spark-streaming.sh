cd $MYAPPS_HOME/spark/streaming

./start-streaming-ratings-kafka-cassandra.sh

tail -f $PIPELINE_HOME/logs/spark/streaming/ratings-kafka-cassandra.log
