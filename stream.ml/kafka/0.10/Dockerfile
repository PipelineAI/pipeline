FROM fluxcapacitor/package-kafka-0.10

WORKDIR /root

# Expose Kafka Broker Port
EXPOSE 9092

# Expose Confluent/Kafka REST Proxy
EXPOSE 8082

# Expose Confluent/Kafka Schema Registry
EXPOSE 8081

COPY run run
COPY demos/ demos/

CMD ["supervise", "."]
