val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))

lazy val settings = (project in file("."))
                    .settings(name := "watcher")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= deps)
		    .settings(javaOptions += "-Xmx10G")

val sparkVersion = sys.env("SPARK_VERSION") 
val scalaTestVersion = sys.env("SCALATEST_VERSION") 
val betterFilesVersion = sys.env("BETTER_FILES_VERSION")
val commonsDaemonVersion = sys.env("COMMONS_DAEMON_VERSION")

lazy val deps = Seq(
  "com.github.pathikrit" %% "better-files" 	% betterFilesVersion,
  "commons-daemon"        % "commons-daemon"    % commonsDaemonVersion
)

