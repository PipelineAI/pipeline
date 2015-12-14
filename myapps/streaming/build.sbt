val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))
addSbtPlugin("org.spark-packages" % "sbt-spark-package" % sys.env("SBT_SPARK_PACKAGES_PLUGIN_VERSION"))

resolvers += "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven"

lazy val streaming = (project in file("."))
                       .settings(name := "streaming")
                       .settings(globalSettings:_*)
                       .settings(libraryDependencies ++= streamingDeps)

val akkaVersion = sys.env("AKKA_VERSION") // 2.3.11
val sparkVersion = sys.env("SPARK_VERSION") // 1.5.1
val sparkCassandraConnectorVersion = sys.env("SPARK_CASSANDRA_CONNECTOR_VERSION") // 1.5.0-M3
val sparkElasticSearchConnectorVersion = sys.env("SPARK_ELASTICSEARCH_CONNECTOR_VERSION") // 2.1.2
val kafkaVersion = sys.env("KAFKA_CLIENT_VERSION") // 0.8.2.2
val scalaTestVersion = sys.env("SCALATEST_VERSION") // 2.2.4
val jedisVersion = sys.env("JEDIS_VERSION") // 2.7.3 
val sparkCsvVersion = sys.env("SPARK_CSV_CONNECTOR_VERSION") // 1.2.0
val sparkAvroVersion = sys.env("SPARK_AVRO_CONNECTOR_VERSION") // 2.0.1
val algebirdVersion = sys.env("ALGEBIRD_VERSION") // 0.11.0
val coreNlpVersion = sys.env("STANFORD_CORENLP_VERSION") //3.5.2
val streamingMatrixFactorizationVersion = sys.env("STREAMING_MATRIX_FACTORIZATION_VERSION") // 0.1.0

lazy val streamingDeps = Seq(
  "com.datastax.spark" % "spark-cassandra-connector_2.10" % sparkCassandraConnectorVersion % "provided",
  "org.elasticsearch" % "elasticsearch-spark_2.10" % sparkElasticSearchConnectorVersion % "provided",
  "redis.clients" % "jedis" % jedisVersion % "provided",
  "com.databricks"    %% "spark-csv" % sparkCsvVersion % "provided",
  "com.databricks"    %% "spark-avro" % sparkAvroVersion % "provided",
  "com.twitter"       %% "algebird-core" % algebirdVersion % "provided",
  "org.apache.spark"  %% "spark-mllib"           % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-graphx"          % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-sql"             % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-streaming"       % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-streaming-kafka" % sparkVersion % "provided",
  "brkyvz" % "streaming-matrix-factorization" % streamingMatrixFactorizationVersion % "provided"
)
