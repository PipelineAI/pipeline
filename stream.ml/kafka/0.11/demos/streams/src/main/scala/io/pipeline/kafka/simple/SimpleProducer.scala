package io.pipeline.kafka.simple

import java.util.Properties

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.ProducerConfig

object SimpleProducer extends App {
  val props = new Properties()
   
  props.put(ProducerConfig.CLIENT_ID_CONFIG, "simple_producer")
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
 
  val producer = new KafkaProducer[String, String](props)

  (0 until 100).foreach(i => {
    val record = new ProducerRecord[String, String]("simple", s"key${i}", s"(key${i},value${i})")
    producer.send(record)
  })

  producer.close()
}
