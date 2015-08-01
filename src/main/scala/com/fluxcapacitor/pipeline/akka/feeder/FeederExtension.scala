package com.fluxcapacitor.pipeline.akka.feeder

import java.util.Properties

import akka.actor._
import kafka.producer.{Producer, ProducerConfig}

object FeederExtension extends ExtensionKey[FeederExtension]

class FeederExtension(system: ExtendedActorSystem) extends Extension {

  val systemConfig = system.settings.config

  val file = systemConfig.getString("pipeline.file")
  val kafkaHost = systemConfig.getString("pipeline.kafkaHost")
  val kafkaTopic = systemConfig.getString("pipeline.kafkaTopic")

  val props = new Properties()
  props.put("metadata.broker.list", kafkaHost)
  props.put("serializer.class", "kafka.serializer.StringEncoder")

  val producerConfig = new ProducerConfig(props)
  val producer = new Producer[String, String](producerConfig)

}

trait FeederExtensionActor { this: Actor =>
  val feederExtension: FeederExtension = FeederExtension(context.system)
}
