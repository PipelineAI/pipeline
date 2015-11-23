val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))
addSbtPlugin("org.spark-packages" % "sbt-spark-package" % sys.env("SBT_SPARK_PACKAGES_PLUGIN_VERSION"))

resolvers += "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven"

lazy val nlp = (project in file("."))
                    .settings(name := "nlp")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= nlpDeps)
		    .settings(javaOptions += "-Xmx10G")

val sparkVersion = sys.env("SPARK_VERSION") 
val scalaTestVersion = sys.env("SCALATEST_VERSION") 
val coreNlpVersion = sys.env("STANFORD_CORENLP_VERSION") 

lazy val nlpDeps = Seq(
//  "databricks" & "spark-corenlp" % "0.1-SNAPSHOT"
  "edu.stanford.nlp" % "stanford-corenlp" % coreNlpVersion 
//  "edu.stanford.nlp" % "stanford-corenlp" % coreNlpVersion classifier "models"
)

