package com.advancedspark.kafka.streams

//import java.util.Properties
// TODO:  add imports

class ItemRatingsStreamConsumer {

//  val streamsConfiguration = new Properties()
//  streamsConfiguration.put(StreamsConfig.JOB_ID_CONFIG, "item_ratings_stream_consumer")
//  streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
//  streamsConfiguration.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, "127.0.0.1:2181")
//  streamsConfiguration.put(StreamsConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
//  streamsConfiguration.put(StreamsConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
//  streamsConfiguration.put(StreamsConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
//  streamsConfiguration.put(StreamsConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])

//  val stringSerializer = new StringSerializer()
//  val stringDeserializer = new StringDeserializer()
//  val longSerializer = new LongSerializer()
//  val longDeserializer = new LongDeserializer()

//  val builder = new KStreamBuilder()

//  val ratings = builder.stream(stringDeserializer, stringDeserializer, "item_ratings")

// TODO:  Proper logic to extract ratings
//  val itemRatings = ratings
//    .flatMapValues(rating => Arrays.asList(rating.split("\\W+")))
//    .map((key, value) => new KeyValue<>(value, value))
//    .countByKey(stringSerializer, longSerializer, stringDeserializer, longDeserializer, "Counts")
//    .toStream()
//  wordCounts.to("WordsWithCountsTopic", stringSerializer, longSerializer)

//  val streams = new KafkaStreams(builder, streamsConfiguration)
//  streams.start()
}
