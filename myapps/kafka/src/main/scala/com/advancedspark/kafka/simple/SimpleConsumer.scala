package org.advancedspark.kafka.simple

import java.util.Arrays
import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer


object SimpleConsumer {
  val props = new Properties()

  props.put("bootstrap.servers", "localhost:9092")
  props.put("group.id", "mygroup")
  props.put("enable.auto.commit", "true")
  props.put("auto.commit.interval.ms", "1000")
  props.put("session.timeout.ms", "30000")
  props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

  val consumer = new KafkaConsumer[String, String](props)
  consumer.subscribe(Arrays.asList("sample_helloworld"))

  while (true) {
    val records = consumer.poll(100)
    System.out.println(records)
//    records.map( record => 
//      System.out.printf("offset = %d, key = %s, value = %s\n", record.offset(), record.key(), record.value())
//    )
  }
}

