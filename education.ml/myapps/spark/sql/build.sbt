val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))

lazy val sql = (project in file("."))
                       .settings(name := "sql")
                       .settings(globalSettings:_*)
                       .settings(libraryDependencies ++= sqlDeps)

val sparkVersion = sys.env("SPARK_VERSION") 
val scalaTestVersion = sys.env("SCALATEST_VERSION") 

lazy val sqlDeps = Seq(
  "org.apache.spark"  %% "spark-sql"             % sparkVersion 
)


