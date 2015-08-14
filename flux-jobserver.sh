#!/bin/bash
echo ...Starting Spark JobServer...
$SPARK_JOBSERVER_HOME/server_start.sh --packages org.apache.spark:spark-streaming-kafka-assembly_2.10:1.4.1,com.datastax.spark:spark-cassandra-connector-java_2.10:1.4.0-M3

echo ...Waiting for JobServer to Start...
sleep 30

echo ...Submitting Streaming Jar to Spark JobServer...
curl --data-binary @$SPARK_JOBSERVER_HOME/job-server-tests/target/scala-2.10/job-server-tests_2.10-0.5.2.jar localhost:8099/jars/test
curl --data-binary @$PIPELINE_HOME/streaming/target/scala-2.10/streaming_2.10-1.0.jar localhost:8099/jars/streaming
