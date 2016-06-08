name := "flink-streaming"

version := "1.0"

organization := "com.advancedflink.streaming"

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))

scalaVersion in ThisBuild := "2.10.4"

val flinkVersion = sys.env("FLINK_VERSION") 

val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-connector-kafka-0.9" % flinkVersion % "provided",
  "org.apache.kafka" %% "kafka" % "0.9.0.1" % "provided",
  "org.apache.flink" %% "flink-cep" % flinkVersion % "provided"
)

lazy val root = (project in file(".")).
  settings(
    libraryDependencies ++= flinkDependencies
  )

mainClass in assembly := Some("com.advancedflink.streaming.KafkaTextStreamWordCount")

// make run command include the provided dependencies
run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))

// exclude Scala library from assembly
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
