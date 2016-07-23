val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

resolvers += "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven"

val sparkVersion = "1.6.1"

lazy val codegen = (project in file("."))
                    .settings(name := s"codegen-spark-${sparkVersion}")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= codegenDeps)
		    .settings(javaOptions += "-Xmx10G")

val janinoVersion = sys.env("JANINO_VERSION")
val guavaVersion = sys.env("GUAVA_VERSION")
val codahaleMetricsVersion = sys.env("CODAHALE_METRICS_VERSION")

lazy val codegenDeps = Seq(
  "io.dropwizard.metrics" % "metrics-core" % codahaleMetricsVersion,
  "com.google.guava" % "guava" % guavaVersion,
  "org.codehaus.janino"  % "janino"             % janinoVersion,
  "org.codehaus.janino"  % "commons-compiler"   % janinoVersion,
  "org.apache.spark"  %% "spark-mllib"          % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-graphx"         % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-sql"            % sparkVersion % "provided"
)
