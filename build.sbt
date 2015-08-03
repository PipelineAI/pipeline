import sbtassembly.Plugin.AssemblyKeys._

name := """PipelineUberJar"""

version := "1.0"

scalaVersion := "2.10.4"

val Spark = "1.4.0"
val SparkCassandra = "1.4.0-M1"
val Kafka = "0.8.2.1"

// getting the dependencies right is a lot of trial and error.  See this web page for some clues:
// https://github.com/datastax/spark-cassandra-connector/blob/v1.2.4/project/CassandraSparkBuild.scala
libraryDependencies ++= Seq(
  ("org.apache.spark" %% "spark-core" % Spark % "provided").
    exclude("com.google.guava", "guava"),
  ("org.apache.spark" %% "spark-streaming" % Spark % "provided").
    exclude("com.google.guava", "guava"),
  ("com.datastax.spark" %% "spark-cassandra-connector" % SparkCassandra withSources() withJavadoc()).
    exclude("com.esotericsoftware.minlog", "minlog").
    exclude("commons-beanutils", "commons-beanutils").
    exclude("org.apache.spark", "spark-core").
    exclude("com.google.guava", "guava"),
  ("com.datastax.spark" %% "spark-cassandra-connector-java" % SparkCassandra withSources() withJavadoc()).
    exclude("org.apache.spark", "spark-core").
    exclude("com.google.guava", "guava"),
  ("org.apache.kafka" %% "kafka" % Kafka).
    exclude("org.slf4j", "slf4j-simple").
    exclude("com.sun.jmx", "jmxri").
    exclude("com.sun.jdmk", "jmxtools").
    exclude("javax.jms", "jms").
    exclude("com.google.guava", "guava"),
  "org.apache.spark" % "spark-sql_2.10" % "1.4.1"
    exclude("com.esotericsoftware.minlog", "minlog")
    exclude("com.google.guava", "guava")
    exclude("io.dropwizard.metrics", "metrics-core")
    exclude("commons-beanutils", "commons-beanutils")
    exclude("org.apache.hadoop", "hadoop-yarn-common"),
  "org.apache.spark" %% "spark-streaming-kafka" % Spark
    exclude("com.google.guava", "guava")
)

//We do this so that Spark Dependencies will not be bundled with our fat jar but will still be included on the classpath
//When we do a sbt/run
run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in(Compile, run), runner in(Compile, run))

assemblySettings

mergeStrategy in assembly := {
  case PathList("META-INF", "ECLIPSEF.RSA", xs@_*) => MergeStrategy.discard
  case PathList("META-INF", "mailcap", xs@_*) => MergeStrategy.discard
  case PathList("org", "apache", "commons", "collections", xs@_*) => MergeStrategy.first
  case PathList("org", "apache", "commons", "logging", xs@_*) => MergeStrategy.first
  case PathList("org", "apache", "commons", "logging", xs@_*) => MergeStrategy.first
  case PathList(ps@_*) if ps.last == "Driver.properties" => MergeStrategy.first
  case PathList(ps@_*) if ps.last == "plugin.properties" => MergeStrategy.discard
  case PathList(ps@_*) if ps.last == "log4j.properties" => MergeStrategy.first
  case PathList(ps@_*) if ps.last == "pom.properties" => MergeStrategy.discard
  case PathList(ps@_*) if ps.last == "pom.xml" => MergeStrategy.discard
  case PathList(ps@_*) if ps.last == "UnusedStubClass.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (mergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in assembly := Some("com.fluxcapacitor.pipeline.akka.feeder.FeederMain")
