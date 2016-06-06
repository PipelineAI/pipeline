// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.advancedflink.streaming

import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer09, FlinkKafkaProducer09}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.windowing.time.Time

object KafkaTextStreamWindowWordCount {

  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      System.err.println("USAGE:\nKafkaTextStreamWindowWordCount <hostname>")
      return
    }

    val hostname = args(0) 

    val env = StreamExecutionEnvironment.createLocalEnvironment()

    val kafkaConsumerProperties = Map(
      "zookeeper.connect" -> (hostname+":2181"),
      "group.id" -> "flink",
      "bootstrap.servers" -> (hostname+":9092")
    )

    val kafkaConsumer = new FlinkKafkaConsumer09[String](
      "input",
      new SimpleStringSchema(),
      kafkaConsumerProperties
    )

    val kafkaProducer = new FlinkKafkaProducer09[String](
      hostname+":9092",
      "output",
      new SimpleStringSchema()
    )

    val text = env.addSource(kafkaConsumer)

    val counts = text.flatMap { _.toLowerCase.split("\\W+") filter { _.nonEmpty } }
      .map { (_, 1) }
      .keyBy(0)
      .timeWindow(Time.seconds(5))
      .sum(1)

    counts.print

    env.execute("Scala KafkaTextStreamWindowWordCount Example")
  }

  implicit def map2Properties(map: Map[String, String]): java.util.Properties = {
    (new java.util.Properties /: map) { case (props, (k, v)) => props.put(k, v); props }
  }
}
