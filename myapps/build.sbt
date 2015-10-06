val globalSettings = Seq(
  version := "1.0",
  scalaVersion := "2.10.4"
)

lazy val feeder = (project in file("feeder"))
                    .settings(name := "feeder")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= feederDeps)

lazy val streaming = (project in file("streaming"))
                       .settings(name := "streaming")
                       .settings(globalSettings:_*)
                       .settings(libraryDependencies ++= streamingDeps)

lazy val simpledatasource = (project in file("simpledatasource"))
                       .settings(name := "simpledatasource")
                       .settings(globalSettings:_*)
                       .settings(libraryDependencies ++= simpledatasourceDeps)

val akkaVersion = "2.3.11"
val sparkVersion = "1.5.1"
val sparkCassandraConnectorVersion = "1.5.0-M2"
val sparkElasticSearchConnectorVersion = "2.2.0-m1"
val kafkaVersion = "0.8.2.1"
val scalaTestVersion = "2.2.4"
val jedisVersion = "2.4.2"
val sparkCsvVersion = "1.2.0"

lazy val feederDeps = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "org.apache.kafka" % "kafka_2.10" % kafkaVersion 
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri")
)

lazy val streamingDeps = Seq(
  "com.datastax.spark" % "spark-cassandra-connector_2.10" % sparkCassandraConnectorVersion % "provided",
  "org.elasticsearch" % "elasticsearch-spark_2.10" % sparkElasticSearchConnectorVersion % "provided",
  "redis.clients" % "jedis" % jedisVersion % "provided",
  "com.databricks"    %% "spark-csv" % sparkCsvVersion % "provided",
  "org.apache.spark"  %% "spark-mllib"           % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-graphx"          % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-sql"             % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-streaming"       % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-streaming-kafka" % sparkVersion % "provided"
)

lazy val simpledatasourceDeps = Seq(
  "org.apache.spark"  %% "spark-sql"             % sparkVersion % "provided"
)
