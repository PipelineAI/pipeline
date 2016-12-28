val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))

lazy val feeder = (project in file("."))
                    .settings(name := "feeder")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= feederDeps)

val akkaVersion = sys.env("AKKA_VERSION")
val sparkVersion = sys.env("SPARK_VERSION") 
val sparkCassandraConnectorVersion = sys.env("SPARK_CASSANDRA_CONNECTOR_VERSION") 
val kafkaClientVersion = sys.env("KAFKA_CLIENT_VERSION") 
val scalaTestVersion = sys.env("SCALATEST_VERSION") 

lazy val feederDeps = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "org.apache.kafka" % "kafka_2.10" % kafkaClientVersion
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri")
)

