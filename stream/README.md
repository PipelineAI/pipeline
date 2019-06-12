```
1. Deploy a kafka client pod with configuration:

    apiVersion: v1
    kind: Pod
    metadata:
      name: kafka-client
      namespace: default
    spec:
      containers:
      - name: kafka-client
        image: confluentinc/cp-kafka:5.0.0
        command:
          - sh
          - -c
          - "exec tail -f /dev/null"

2. Log into the Pod

  kubectl exec -it kafka-client -- /bin/bash

3. Explore with kafka commands:

  # Create the topic
  kafka-topics --zookeeper kafka-cp-zookeeper-headless:2181 --topic kafka-topic --create --partitions 1 --replication-factor 1 --if-not-exists

  # Create a message
  MESSAGE="`date -u`"

  # Produce a test message to the topic
  echo "$MESSAGE" | kafka-console-producer --broker-list kafka-cp-kafka-headless:9092 --topic kafka-topic

  # Consume a test message from the topic
  kafka-console-consumer --bootstrap-server kafka-cp-kafka-headless:9092 --topic kafka-topic --from-beginning --timeout-ms 2000 --max-messages 1 | grep "$MESSAGE"
  
```

```
# Kafka
#helm repo add confluentinc https://confluentinc.github.io/cp-helm-charts/
#helm repo update

#helm install confluentinc/cp-helm-charts --name kafka --set cp-zookeeper.enabled=true,cp-zookeeper.persistence.enabled=false,cp-kafka.enabled=true,cp-kafka.persistence.enabled=false,cp-schema-registry.enabled=false,cp-kafka-rest.enabled=true,cp-kafka-connect.enabled=false,cp-ksql-server.enabled=false

#kubectl create -f %s/cluster/yaml/kafka/kafka-rest-svc.yaml
#kubectl create -f %s/cluster/yaml/kafka/kafka-svc.yaml
```

https://docs.confluent.io/current/kafka-rest/docs/intro.html#produce-and-consume-json-messages
