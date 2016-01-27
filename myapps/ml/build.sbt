val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))
addSbtPlugin("org.spark-packages" % "sbt-spark-package" % sys.env("SBT_SPARK_PACKAGES_PLUGIN_VERSION"))

resolvers += "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven"

//(unmanagedClasspath in Compile) += file("/root/stanford-corenlp-full-2015-12-09/stanford-corenlp-3.6.0.jar")
(unmanagedClasspath in Compile) += file("/root/stanford-corenlp-full-2015-12-09/stanford-corenlp-3.6.0-models.jar")

lazy val ml = (project in file("."))
                    .settings(name := "ml")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= mlDeps)
		    .settings(javaOptions += "-Xmx10G")

val sparkVersion = sys.env("SPARK_VERSION") 
val scalaTestVersion = sys.env("SCALATEST_VERSION") 
val coreNlpVersion = sys.env("STANFORD_CORENLP_VERSION") 
val algebirdVersion = sys.env("ALGEBIRD_VERSION")

lazy val mlDeps = Seq(
//  "edu.stanford.nlp" % "stanford-corenlp" % coreNlpVersion,
  "com.twitter" %% "algebird-core" % algebirdVersion,
  "edu.stanford.nlp"  % "stanford-corenlp"      % coreNlpVersion,
//  "edu.stanford.nlp"  % "stanford-corenlp"      % coreNlpVersion classifier "models",
  "org.apache.spark"  %% "spark-mllib"           % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-graphx"          % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-sql"             % sparkVersion % "provided"
)

