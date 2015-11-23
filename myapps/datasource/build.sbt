val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))

lazy val datasource = (project in file("."))
                       .settings(name := "datasource")
                       .settings(globalSettings:_*)
                       .settings(libraryDependencies ++= datasourceDeps)

val sparkVersion = sys.env("SPARK_VERSION") 
val scalaTestVersion = sys.env("SCALATEST_VERSION") 

lazy val datasourceDeps = Seq(
  "org.apache.spark"  %% "spark-sql"             % sparkVersion 
)


