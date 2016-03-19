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

val akkaVersion = sys.env("AKKA_VERSION") 
val sparkVersion = sys.env("SPARK_VERSION")
val sparkCassandraConnectorVersion = sys.env("SPARK_CASSANDRA_CONNECTOR_VERSION") 
val sparkElasticSearchConnectorVersion = sys.env("SPARK_ELASTICSEARCH_CONNECTOR_VERSION") 
val kafkaVersion = sys.env("KAFKA_CLIENT_VERSION") 
val scalaTestVersion = sys.env("SCALATEST_VERSION") 
val jedisVersion = sys.env("JEDIS_VERSION") 
val sparkCsvVersion = sys.env("SPARK_CSV_CONNECTOR_VERSION") 
val sparkAvroVersion = sys.env("SPARK_AVRO_CONNECTOR_VERSION") 
val algebirdVersion = sys.env("ALGEBIRD_VERSION") 
val streamingMatrixFactorizationVersion = sys.env("STREAMING_MATRIX_FACTORIZATION_VERSION") 
val sparkNifiConnectorVersion = sys.env("SPARK_NIFI_CONNECTOR_VERSION")

lazy val streamingDeps = Seq(
  "com.madhukaraphatak" %% "java-sizeof" % "0.1",
  "com.datastax.spark" %% "spark-cassandra-connector" % sparkCassandraConnectorVersion % "provided",
  "org.elasticsearch" %% "elasticsearch-spark" % sparkElasticSearchConnectorVersion % "provided",
  "redis.clients" % "jedis" % jedisVersion % "provided",
  "com.databricks"    %% "spark-csv" % sparkCsvVersion % "provided",
  "com.databricks"    %% "spark-avro" % sparkAvroVersion % "provided",
  "com.twitter"       %% "algebird-core" % algebirdVersion % "provided",
  "org.apache.spark"  %% "spark-mllib"           % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-graphx"          % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-sql"             % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-streaming"       % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-streaming-kafka" % sparkVersion % "provided",
  "brkyvz" % "streaming-matrix-factorization" % streamingMatrixFactorizationVersion % "provided",
  "org.apache.nifi" % "nifi-spark-receiver" % sparkNifiConnectorVersion % "provided"
)
