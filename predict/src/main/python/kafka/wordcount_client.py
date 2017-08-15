from confluent_kafka import Producer

p = Producer({'bootstrap.servers': 'localhost:9092'})
topic = 'prediction-inputs'
some_data_source = ["a b c", "a b", "a"]
for data in some_data_source:
    print("producing {} to {}".format(data, topic))
    p.produce(topic, data.encode('utf-8'))
p.flush()
