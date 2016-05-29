val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))

lazy val redis = (project in file("."))
                    .settings(name := "redis")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= redisDeps)
		    .settings(javaOptions += "-Xmx10G")

val sparkRedisConnectorVersion = sys.env("SPARK_REDIS_CONNECTOR_VERSION")
val sparkVersion = sys.env("SPARK_VERSION")

lazy val redisDeps = Seq(
  "org.apache.spark"  %% "spark-sql"             % sparkVersion,
  "RedisLabs"  % "spark-redis"   % sparkRedisConnectorVersion
)
