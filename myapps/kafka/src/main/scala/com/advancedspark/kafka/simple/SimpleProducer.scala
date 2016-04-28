package com.advancedspark.kafka.simple

import java.util.Properties

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

object SimpleProducer extends App {
  val props = new Properties()
   
  props.put("bootstrap.servers", "localhost:9092")
   
  props.put("key.serializer",
      "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer",
      "org.apache.kafka.common.serialization.StringSerializer")
   
  val producer = new KafkaProducer[String, String](props)
  (0 to 5).foreach(i => {
    val record = new ProducerRecord[String, String]("sample_helloworld", s"key${i}", s"(key${i},value${i}")
    producer.send(record)
  })

  producer.close()
}

