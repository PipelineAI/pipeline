val globalSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.8" 
)

lazy val streaming = (project in file("."))
                       .settings(name := "kafka")
                       .settings(globalSettings:_*)
                       .settings(libraryDependencies ++= streamingDeps)

val kafkaClientVersion = "0.11.0.0" 

lazy val streamingDeps = Seq(
  "org.apache.kafka"    %% "kafka"         % kafkaClientVersion,
  "org.apache.kafka"     % "kafka-streams" % kafkaClientVersion
)
