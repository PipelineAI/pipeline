val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % sys.env("SBT_ASSEMBLY_PLUGIN_VERSION"))

lazy val finagle = (project in file("."))
                    .settings(name := "finagle")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= codeGenDeps)
		    .settings(javaOptions += "-Xmx10G")

val janinoVersion = sys.env("JANINO_VERSION")

lazy val codeGenDeps = Seq(
  "org.codehaus.janino"  % "janino"      	% janinoVersion,
  "org.codehaus.janino"  % "commons-compiler"   % janinoVersion
)
