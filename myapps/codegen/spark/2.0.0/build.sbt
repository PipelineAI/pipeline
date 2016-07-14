val globalSettings = Seq(
  version := "1.0",
//  scalaVersion := sys.env("SCALA_VERSION") 
  scalaVersion := "2.11.8"
)

resolvers += "Apache Snapshots" at "http://repository.apache.org/snapshots/"

val sparkVersion = "2.0.0-SNAPSHOT"

lazy val codegen = (project in file("."))
                    .settings(name := s"codegen-spark-${sparkVersion}")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= codegenDeps)
		    .settings(javaOptions += "-Xmx10G")

val janinoVersion = sys.env("JANINO_VERSION")

lazy val codegenDeps = Seq(
  "org.codehaus.janino"  % "janino"             % janinoVersion,
  "org.codehaus.janino"  % "commons-compiler"   % janinoVersion,
  "org.apache.spark"  %% "spark-mllib"          % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-graphx"         % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-sql"            % sparkVersion % "provided"
)

