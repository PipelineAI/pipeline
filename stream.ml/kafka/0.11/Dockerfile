FROM fluxcapacitor/package-kafka-0.11:master

WORKDIR /root

ENV \
  LOGS_HOME=/root/logs/

RUN \
  mkdir -p $LOGS_HOME

# Expose Kafka Broker Port
EXPOSE 9092

# Expose Confluent/Kafka REST Proxy
EXPOSE 8082

# Expose Confluent/Kafka Schema Registry
EXPOSE 8081

COPY run run
COPY demos/ demos/

CMD ["supervise", "."]
