val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))
addSbtPlugin("org.spark-packages" % "sbt-spark-package" % sys.env("SBT_SPARK_PACKAGES_PLUGIN_VERSION"))

resolvers += "Apache Snapshots" at "http://repository.apache.org/snapshots/"

val sparkVersion = "2.0.0-SNAPSHOT"

lazy val codegen = (project in file("."))
                    .settings(name := s"codegen-spark-${sparkVersion}")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= codegenDeps)
		    .settings(javaOptions += "-Xmx10G")

val scalaTestVersion = sys.env("SCALATEST_VERSION") 

val janinoVersion = sys.env("JANINO_VERSION")

lazy val codegenDeps = Seq(
  "org.codehaus.janino"  % "janino"             % janinoVersion,
  "org.codehaus.janino"  % "commons-compiler"   % janinoVersion,
  "org.apache.spark"  %% "spark-mllib"          % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-graphx"         % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-sql"            % sparkVersion % "provided"
)

