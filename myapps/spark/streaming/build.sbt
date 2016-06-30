val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))

resolvers += "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += "Repo at github.com/ankurdave/maven-repo" at "https://github.com/ankurdave/maven-repo/raw/master"

lazy val streaming = (project in file("."))
                       .settings(name := "streaming")
                       .settings(globalSettings:_*)
                       .settings(libraryDependencies ++= streamingDeps)

val akkaVersion = sys.env("AKKA_VERSION") 
val sparkVersion = sys.env("SPARK_VERSION")
val sparkCassandraConnectorVersion = sys.env("SPARK_CASSANDRA_CONNECTOR_VERSION") 
val sparkElasticSearchConnectorVersion = sys.env("SPARK_ELASTICSEARCH_CONNECTOR_VERSION") 
val scalaTestVersion = sys.env("SCALATEST_VERSION") 
val jedisVersion = sys.env("JEDIS_VERSION") 
val sparkCsvVersion = sys.env("SPARK_CSV_CONNECTOR_VERSION") 
val sparkAvroVersion = sys.env("SPARK_AVRO_CONNECTOR_VERSION") 
val algebirdVersion = sys.env("ALGEBIRD_VERSION") 
val sparkNifiConnectorVersion = sys.env("SPARK_NIFI_CONNECTOR_VERSION")
val indexedRddVersion = sys.env("INDEXEDRDD_VERSION")
val ankurPartVersion = sys.env("ANKUR_PART_VERSION")
// We can't promote this over version 2.5.0 otherwise it conflicts with Spark 1.6 version of Jackson
val maxmindGeoIpVersion = sys.env("MAXMIND_GEOIP_VERSION")
val dynoVersion = sys.env("DYNO_VERSION")
val jblasVersion = sys.env("JBLAS_VERSION")

lazy val streamingDeps = Seq(
  "org.jblas"            % "jblas"                 % jblasVersion,
  "com.madhukaraphatak" %% "java-sizeof" % "0.1",
  "com.datastax.spark"  %% "spark-cassandra-connector" % sparkCassandraConnectorVersion % "provided",
  "org.elasticsearch"   %% "elasticsearch-spark" % sparkElasticSearchConnectorVersion % "provided",
  "redis.clients"       % "jedis" % jedisVersion % "provided",
  "com.databricks"      %% "spark-csv" % sparkCsvVersion % "provided",
  "com.databricks"      %% "spark-avro" % sparkAvroVersion % "provided",
  "com.twitter"         %% "algebird-core" % algebirdVersion % "provided",
  "org.apache.spark"    %% "spark-mllib"           % sparkVersion % "provided",
  "org.apache.spark"    %% "spark-graphx"          % sparkVersion % "provided",
  "org.apache.spark"    %% "spark-sql"             % sparkVersion % "provided",
  "org.apache.spark"    %% "spark-streaming"       % sparkVersion % "provided",
  "org.apache.spark"    %% "spark-streaming-kafka" % sparkVersion % "provided",
  "amplab"              % "spark-indexedrdd" % indexedRddVersion % "provided",
  "com.ankurdave"       %% "part" % ankurPartVersion % "provided",
  "org.apache.nifi"     % "nifi-spark-receiver" % sparkNifiConnectorVersion % "provided",
  "com.maxmind.geoip2"  % "geoip2"		% maxmindGeoIpVersion % "provided",
  "com.netflix.dyno"     % "dyno-jedis"                    % dynoVersion
)
