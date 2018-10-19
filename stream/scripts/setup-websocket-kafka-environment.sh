# Note:  if #!/bin/bash is used here, the RUN command will need to export all vars as a child process is created

echo ""
echo "Setting up WebSocket Kafka into Conda Environment '$PIPELINE_WEBSOCKET_KAFKA_CONDA_ENV_NAME' with '$PIPELINE_WEBSOCKET_KAFKA_SERVER_PATH/requirements/pipeline_ws_kafka_topic_stream_conda_environment.yml'..."
echo ""
conda env update --name $PIPELINE_WEBSOCKET_KAFKA_CONDA_ENV_NAME --file $PIPELINE_WEBSOCKET_KAFKA_SERVER_PATH/requirements/pipeline_ws_kafka_topic_stream_conda_environment.yml
echo ""
echo "...WebSocket Kafka Server Installed!"
echo ""
