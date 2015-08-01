name := """FeedSimulator"""

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.datastax.spark" % "spark-cassandra-connector_2.10" % "1.4.0-M1",
  "org.apache.kafka" % "kafka_2.10" % "0.8.2.1"
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri"),
  "org.apache.spark" % "spark-streaming-kafka-assembly_2.10" % "1.4.1"
)
