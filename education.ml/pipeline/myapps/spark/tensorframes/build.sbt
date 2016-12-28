val globalSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.8"
//    sys.env("SCALA_VERSION")
)

//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "14.0")
  //sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))

// TODO:  Add Snapshots Repo
//  http://repository.apache.org/snapshots/

lazy val proj = (project in file("."))
                       .settings(name := "tensorframes")
                       .settings(globalSettings:_*)
                       .settings(libraryDependencies ++= deps)

//val tensorframesVersion = sys.env("TENSORFRAMES_VERSION")
val tensorframesVersion = "0.2.4"

(unmanagedClasspath in Compile) += file(s"lib/tensorframes-assembly-${tensorframesVersion}.jar")

val sparkVersion = "2.0.0"
  //sys.env("SPARK_VERSION")

lazy val deps = Seq(
  "org.apache.spark"  %% "spark-sql"             % sparkVersion
)
