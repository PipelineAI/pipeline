val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))
addSbtPlugin("org.spark-packages" % "sbt-spark-package" % sys.env("SBT_SPARK_PACKAGES_PLUGIN_VERSION"))

resolvers += "Apache Snapshots" at "https://repository.apache.org/content/group"

val sparkVersion = "2.0.0-SNAPSHOT"

(unmanagedClasspath in Compile) += file("/root/spark-2.0.0-SNAPSHOT-bin-fluxcapacitor/jars/spark-catalyst_2.10-2.0.0-SNAPSHOT.jar")
(unmanagedClasspath in Compile) += file("/root/spark-2.0.0-SNAPSHOT-bin-fluxcapacitor/jars/spark-core_2.10-2.0.0-SNAPSHOT.jar")
(unmanagedClasspath in Compile) += file("/root/spark-2.0.0-SNAPSHOT-bin-fluxcapacitor/jars/spark-ganglia-lgpl_2.10-2.0.0-SNAPSHOT.jar")
(unmanagedClasspath in Compile) += file("/root/spark-2.0.0-SNAPSHOT-bin-fluxcapacitor/jars/spark-graphx_2.10-2.0.0-SNAPSHOT.jar")
(unmanagedClasspath in Compile) += file("/root/spark-2.0.0-SNAPSHOT-bin-fluxcapacitor/jars/spark-hive-thriftserver_2.10-2.0.0-SNAPSHOT.jar")
(unmanagedClasspath in Compile) += file("/root/spark-2.0.0-SNAPSHOT-bin-fluxcapacitor/jars/spark-hive_2.10-2.0.0-SNAPSHOT.jar")
(unmanagedClasspath in Compile) += file("/root/spark-2.0.0-SNAPSHOT-bin-fluxcapacitor/jars/spark-launcher_2.10-2.0.0-SNAPSHOT.jar")
(unmanagedClasspath in Compile) += file("/root/spark-2.0.0-SNAPSHOT-bin-fluxcapacitor/jars/spark-mllib-local_2.10-2.0.0-SNAPSHOT.jar")
(unmanagedClasspath in Compile) += file("/root/spark-2.0.0-SNAPSHOT-bin-fluxcapacitor/jars/spark-mllib_2.10-2.0.0-SNAPSHOT.jar")
(unmanagedClasspath in Compile) += file("/root/spark-2.0.0-SNAPSHOT-bin-fluxcapacitor/jars/spark-network-common_2.10-2.0.0-SNAPSHOT.jar")
(unmanagedClasspath in Compile) += file("/root/spark-2.0.0-SNAPSHOT-bin-fluxcapacitor/jars/spark-network-shuffle_2.10-2.0.0-SNAPSHOT.jar")
(unmanagedClasspath in Compile) += file("/root/spark-2.0.0-SNAPSHOT-bin-fluxcapacitor/jars/spark-repl_2.10-2.0.0-SNAPSHOT.jar")
(unmanagedClasspath in Compile) += file("/root/spark-2.0.0-SNAPSHOT-bin-fluxcapacitor/jars/spark-sketch_2.10-2.0.0-SNAPSHOT.jar")
(unmanagedClasspath in Compile) += file("/root/spark-2.0.0-SNAPSHOT-bin-fluxcapacitor/jars/spark-sql_2.10-2.0.0-SNAPSHOT.jar")
(unmanagedClasspath in Compile) += file("/root/spark-2.0.0-SNAPSHOT-bin-fluxcapacitor/jars/spark-streaming_2.10-2.0.0-SNAPSHOT.jar")
(unmanagedClasspath in Compile) += file("/root/spark-2.0.0-SNAPSHOT-bin-fluxcapacitor/jars/spark-unsafe_2.10-2.0.0-SNAPSHOT.jar")

lazy val codegen = (project in file("."))
                    .settings(name := s"codegen-spark-${sparkVersion}")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= codegenDeps)
		    .settings(javaOptions += "-Xmx10G")

val scalaTestVersion = sys.env("SCALATEST_VERSION") 

val janinoVersion = sys.env("JANINO_VERSION")

lazy val codegenDeps = Seq(
  "org.codehaus.janino"  % "janino"             % janinoVersion,
  "org.codehaus.janino"  % "commons-compiler"   % janinoVersion
//  "org.apache.spark"  %% "spark-mllib"    % "2.0.0-SNAPSHOT"       % sparkVersion % "provided",
//  "org.apache.spark"  %% "spark-graphx"   % "2.0.0-SNAPSHOT"       % sparkVersion % "provided",
//  "org.apache.spark"  %% "spark-sql"      % "2.0.0-SNAPSHOT"       % sparkVersion % "provided"
)

