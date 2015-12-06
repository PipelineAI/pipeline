val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

addSbtPlugin("org.spark-packages" % "sbt-spark-package" % sys.env("SBT_SPARK_PACKAGES_PLUGIN_VERSION"))

resolvers += "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven"

lazy val ml = (project in file("."))
                    .settings(name := "ml")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= mlDeps)
		    .settings(javaOptions += "-Xmx10G")

val sparkVersion = sys.env("SPARK_VERSION") 
val scalaTestVersion = sys.env("SCALATEST_VERSION") 
val coreNlpVersion = sys.env("STANFORD_CORENLP_VERSION") 
val sparkHashVersion = sys.env("SPARK_HASH_VERSION")

lazy val mlDeps = Seq(
  "edu.stanford.nlp" % "stanford-corenlp" % coreNlpVersion, 
  "com.invincea" % "spark-hash" % sparkHashVersion
)

