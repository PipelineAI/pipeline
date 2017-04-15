val globalSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.8"
)

lazy val proj = (project in file("."))
                       .settings(name := "tensorframes")
                       .settings(globalSettings:_*)
                       .settings(libraryDependencies ++= deps)

val tensorframesVersion = "0.2.4"

(unmanagedClasspath in Compile) += file(s"lib/tensorframes-assembly-${tensorframesVersion}.jar")

val sparkVersion = "2.0.1"

lazy val deps = Seq(
  "org.apache.spark"  %% "spark-sql"             % sparkVersion
)
