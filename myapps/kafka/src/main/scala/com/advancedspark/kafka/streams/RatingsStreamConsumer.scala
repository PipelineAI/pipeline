package com.advancedspark.kafka.streams

import java.util.Properties
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.KStreamBuilder
import org.apache.kafka.common.serialization.Serdes

// TODO:  add imports

object RatingsStreamConsumer {
  def main(args: Array[String]) {
    val streamsConfiguration = new Properties()
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "item_ratings_stream_consumer")
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
    streamsConfiguration.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, "127.0.0.1:2181")
    streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, classOf[Serdes.StringSerde])
    streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, classOf[Serdes.StringSerde])

    val builder = new KStreamBuilder()

    val ratings = builder.stream("item_ratings")
/*
        KTable<String, Long> counts = source
                .flatMapValues(new ValueMapper<String, Iterable<String>>() {
                    @Override
                    public Iterable<String> apply(String value) {
                        return Arrays.asList(value.toLowerCase(Locale.getDefault()).split(" "));
                    }
                }).map(new KeyValueMapper<String, String, KeyValue<String, String>>() {
                    @Override
                    public KeyValue<String, String> apply(String key, String value) {
                        return new KeyValue<>(value, value);
                    }
                })
                .countByKey("Counts");

        // need to override value serde to Long type
        counts.to(Serdes.String(), Serdes.Long(), "streams-wordcount-output");

        KafkaStreams streams = new KafkaStreams(builder, props);
        streams.start();

        // usually the stream application would be running forever,
        // in this example we just let it run for some time and stop since the input data is finite.
        Thread.sleep(5000L);

        streams.close();
*/
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
}
