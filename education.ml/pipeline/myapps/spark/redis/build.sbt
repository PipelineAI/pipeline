val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))

val sparkRedisConnectorVersion = sys.env("SPARK_REDIS_CONNECTOR_VERSION")
(unmanagedClasspath in Compile) += file(s"/root/pipeline/myapps/spark/redis/lib/spark-redis_2.10-${sparkRedisConnectorVersion}.jar")

lazy val redis = (project in file("."))
                    .settings(name := "redis")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= redisDeps)
		    .settings(javaOptions += "-Xmx10G")

val sparkVersion = sys.env("SPARK_VERSION")
val jedisVersion = sys.env("JEDIS_VERSION")

lazy val redisDeps = Seq(
  "org.apache.spark"  %% "spark-sql"            % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-streaming"      % sparkVersion % "provided",
  "redis.clients"      % "jedis" 		% jedisVersion % "provided"
//  "RedisLabs"  % "spark-redis"   % sparkRedisConnectorVersion
)
