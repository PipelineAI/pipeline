package com.advancedspark.feeder.rating

import java.util.Properties

import akka.actor._
import org.apache.kafka.clients.producer.{KafkaProducer,ProducerConfig}

object FeederExtension extends ExtensionKey[FeederExtension]

class FeederExtension(system: ExtendedActorSystem) extends Extension {

  val systemConfig = system.settings.config

  val file = systemConfig.getString("pipeline.file")
  val kafkaHost = systemConfig.getString("pipeline.kafkaHost")
  val kafkaTopic = systemConfig.getString("pipeline.kafkaTopic")

  val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost)
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

  val producer = new KafkaProducer[String, String](props)

}

trait FeederExtensionActor { this: Actor =>
  val feederExtension: FeederExtension = FeederExtension(context.system)
}
