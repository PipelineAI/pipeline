val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

javaOptions += "-Xmx16G"

lazy val core = (project in file("."))
                    .settings(name := "core")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= coreDeps)

val sparkVersion = sys.env("SPARK_VERSION")
val scalaTestVersion = sys.env("SCALATEST_VERSION") 

lazy val coreDeps = Seq(
  "com.madhukaraphatak" %% "java-sizeof" % "0.1",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "org.apache.spark"  %% "spark-core" % sparkVersion
    exclude("commons-collections", "commons-collections")
    exclude("commons-beanutils", "commons-beanutils")
    exclude("org.apache.hadoop", "hadoop-yarn-api")
    exclude("org.apache.hadoop", "hadoop-yarn-common")
    exclude("com.esotericsoftware.kryo", "kryo")
    exclude("com.esotericsoftware.kryo", "minlog")
    exclude("org.apache.spark", "spark-launcher-2.10")
    exclude("org.spark-project.spark", "unused")
    exclude("org.apache.spark", "spark-network-common_2.10")
    exclude("org.apache.spark", "spark-network-shuffle_2.10")
    exclude("org.apache.hadoop", "hadoop-mapreduce-client-app")
    exclude("org.apache.hadoop", "hadoop-mapreduce-client-core")
    exclude("org.apache.hadoop", "hadoop-mapreduce-client-jobclient")
    exclude("asm", "asm")
)

