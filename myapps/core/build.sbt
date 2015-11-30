val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))

lazy val core = (project in file("."))
                    .settings(name := "core")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= coreDeps)

val sparkVersion = sys.env("SPARK_VERSION")
val scalaTestVersion = sys.env("SCALATEST_VERSION") 

lazy val coreDeps = Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "org.apache.spark"  %% "spark-core" % sparkVersion
    exclude("commons-collections", "commons-collections")
    exclude("commons-beanutils", "commons-beanutils")
    exclude("org.apache.hadoop", "hadoop-yarn-api")
    exclude("org.apache.hadoop", "hadoop-yarn-common")
    exclude("com.google.guava", "guava")
    exclude("com.esotericsoftware.kryo", "kryo")
    exclude("com.esotericsoftware.kryo", "minlog")
    exclude("org.apache.spark", "spark-launcher-2.10")
    exclude("org.spark-project.spark", "unused")
    exclude("org.apache.spark", "spark-network-common_2.10")
    exclude("org.apache.spark", "spark-network-shuffle_2.10")
    exclude("org.apache.spark", "spark-unsafe_2.10")
)

