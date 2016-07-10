val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

lazy val settings = (project in file("."))
                    .settings(name := "spark-serving")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= deps)
		    .settings(javaOptions += "-Xmx10G")

val dynoVersion = sys.env("DYNO_VERSION")

lazy val deps = Seq(
  "com.netflix.dyno"     % "dyno-jedis"                    % dynoVersion % "provided"
)
