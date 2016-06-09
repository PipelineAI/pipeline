val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))
addSbtPlugin("org.spark-packages" % "sbt-spark-package" % sys.env("SBT_SPARK_PACKAGES_PLUGIN_VERSION"))

resolvers += "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven"

(unmanagedClasspath in Compile) += file("/root/stanford-corenlp-full-2015-12-09/stanford-corenlp-3.6.0-models.jar")

lazy val codegen = (project in file("."))
                    .settings(name := "codegen-spark-1-6-1")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= codegenDeps)
		    .settings(javaOptions += "-Xmx10G")

val sparkVersion = "1.6.1" 
val scalaTestVersion = sys.env("SCALATEST_VERSION") 
val jblasVersion = "1.2.4" 
val janinoVersion = sys.env("JANINO_VERSION")
val breezeVersion = "0.11.2"

lazy val codegenDeps = Seq(
  "org.codehaus.janino"  % "janino"              % janinoVersion,
  "org.codehaus.janino"  % "commons-compiler"    % janinoVersion,
  "org.apache.spark"  %% "spark-mllib"           % sparkVersion  % "provided",
  "org.apache.spark"  %% "spark-graphx"          % sparkVersion  % "provided",
  "org.apache.spark"  %% "spark-sql"             % sparkVersion  % "provided",
  "org.jblas" 	       % "jblas" 		 % jblasVersion  % "provided",
  "org.scalanlp"      %% "breeze"                % breezeVersion % "provided"
)

