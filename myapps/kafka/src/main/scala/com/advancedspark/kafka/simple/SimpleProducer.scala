package com.advancedspark.kafka.simple

import java.util.Properties

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.common.serialization.Serdes


object SimpleProducer extends App {
  val props = new Properties()
   
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "simple_producer")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
  props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName())
  props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName())
   
  val producer = new KafkaProducer[String, String](props)
  (0 to 5).foreach(i => {
    val record = new ProducerRecord[String, String]("simple", s"key${i}", s"(key${i},value${i}")
    producer.send(record)
  })

  producer.close()
}

