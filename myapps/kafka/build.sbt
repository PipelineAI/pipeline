val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION")
)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))

//resolvers += "Confluent Repo" at "http://packages.confluent.io/maven/"

lazy val streaming = (project in file("."))
                       .settings(name := "kafka")
                       .settings(globalSettings:_*)
                       .settings(libraryDependencies ++= streamingDeps)

val kafkaVersion = sys.env("KAFKA_CLIENT_VERSION")

lazy val streamingDeps = Seq(
  "org.apache.kafka"    %% "kafka" % kafkaVersion
)
