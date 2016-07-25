val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

resolvers += "Apache Snapshots" at "http://repository.apache.org/snapshots/"

val sparkVersion = "2.0.1-SNAPSHOT" 

lazy val proj = (project in file("."))
                    .settings(name := s"jpmml-spark-${sparkVersion}")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= deps)
		    .settings(javaOptions += "-Xmx10G")

val jpmmlSparkMLVersion = sys.env("JPMML_SPARKML_VERSION")

lazy val deps = Seq(
  "org.apache.spark"  %% "spark-sql"            % sparkVersion % "provided",
  "org.apache.spark"  %% "spark-mllib"          % sparkVersion % "provided",
  "org.jpmml"  % "jpmml-sparkml"          % jpmmlSparkMLVersion % "provided"
)

