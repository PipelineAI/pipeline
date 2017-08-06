package io.pipeline.kafka.simple

import java.util.Arrays
import java.util.Properties

import scala.collection.JavaConversions._

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.clients.consumer.ConsumerConfig

object SimpleConsumer extends App {
  val props = new Properties()

  props.put(ConsumerConfig.CLIENT_ID_CONFIG, "simple_consumer")
  props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
  props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  props.put(ConsumerConfig.GROUP_ID_CONFIG, "simple_consumer_group")
  props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
  props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")

  val consumer = new KafkaConsumer[String, String](props)
  val topic = "simple"
 
  consumer.subscribe(Arrays.asList(topic))
  while (true) {
    System.out.println("Polling...")
    val records = consumer.poll(100)

    records.iterator().toList.map( record => 
      System.out.println("Message received:  offset = " + record.offset() + ", key = " + record.key() + ", value = " + record.value())
    )
  }
}
