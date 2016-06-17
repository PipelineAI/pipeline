package com.advancedspark.kafka.simple

import java.util.Properties

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.clients.consumer.ConsumerConfig

object SimpleProducer extends App {
  val props = new Properties()
   
  props.put(ConsumerConfig.CLIENT_ID_CONFIG, "simple_producer")
  props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
  props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")  
 
  val producer = new KafkaProducer[String, String](props)

  (0 until 100).foreach(i => {
    val record = new ProducerRecord[String, String]("simple", s"key${i}", s"(key${i},value${i}")
    producer.send(record)
  })

  producer.close()
}
