name := """FeedSimulator"""

version := "1.0"

scalaVersion := "2.10.4"

val akkaVersion = "2.3.11"
val sparkVersion = "1.4.1"
val sparkCassandraConnectorVersion = "1.4.0-M2"
val kafkaVersion = "0.8.2.1"
val scalaTestVersion = "2.2.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "com.datastax.spark" % "spark-cassandra-connector_2.10" % sparkCassandraConnectorVersion,
  "org.apache.kafka" % "kafka_2.10" % kafkaVersion
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri"),
  "org.apache.spark" % "spark-sql_2.10" % sparkVersion,
  "org.apache.spark" % "spark-streaming_2.10" % sparkVersion,
  "org.apache.spark" % "spark-streaming-kafka_2.10" % sparkVersion 
)

mainClass in (Compile, run) := Some("com.fluxcapacitor.pipeline.akka.feeder.FeederMain")
mainClass in (Compile, packageBin) := Some("com.fluxcapacitor.pipeline.akka.feeder.FeederMain")

