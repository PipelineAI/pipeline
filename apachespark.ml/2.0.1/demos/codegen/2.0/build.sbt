val globalSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.8"
)

resolvers += "Apache Snapshots" at "http://repository.apache.org/snapshots/"

val sparkVersion = "2.0.1" 

lazy val codegen = (project in file("."))
                    .settings(name := s"spark-codegen-${sparkVersion}")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= codegenDeps)
		    .settings(javaOptions += "-Xmx10G")

val janinoVersion = "2.7.8" 
val guavaVersion = "14.0.1"
val codahaleMetricsVersion = "3.1.2" 

lazy val codegenDeps = Seq(
  "io.dropwizard.metrics" % "metrics-core" % codahaleMetricsVersion,
  "com.google.guava" % "guava" % guavaVersion,
  "org.codehaus.janino"  % "janino"             % janinoVersion,
  "org.codehaus.janino"  % "commons-compiler"   % janinoVersion,
  "org.apache.httpcomponents" % "httpclient" % "4.5.2"
//  "org.apache.spark"  %% "spark-mllib"          % sparkVersion % "provided",
//  "org.apache.spark"  %% "spark-graphx"         % sparkVersion % "provided",
//  "org.apache.spark"  %% "spark-sql"            % sparkVersion % "provided"
)

