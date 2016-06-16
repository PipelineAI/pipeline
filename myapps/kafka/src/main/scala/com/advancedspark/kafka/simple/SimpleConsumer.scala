package org.advancedspark.kafka.simple

import java.util.Arrays
import java.util.Properties

import scala.collection.JavaConversions._

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.clients.consumer.ConsumerConfig

object SimpleConsumer extends App {
  val props = new Properties()

  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "simple_consumer")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
  props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName())
  props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName())
  props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  props.put(ConsumerConfig.GROUP_ID_CONFIG, "simple_consumer_group")
  props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")

  val consumer = new KafkaConsumer[String, String](props)
  consumer.subscribe(Arrays.asList("simple"))

  while (true) {
    val records = consumer.poll(100)
    System.out.println(records)
    records.iterator().toList.map( record => 
      System.out.println("offset = " + record.offset() + ", key = " + record.key() + ", value = " + record.value())
    )
  }
}
