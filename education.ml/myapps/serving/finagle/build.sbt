val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

lazy val settings = (project in file("."))
                    .settings(name := "finagle")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= deps)
		    .settings(javaOptions += "-Xmx10G")

val sparkVersion = sys.env("SPARK_VERSION") 
val finagleVersion = sys.env("FINAGLE_VERSION")
val jblasVersion = sys.env("JBLAS_VERSION")
val hystrixVersion = sys.env("HYSTRIX_VERSION")
val betterFilesVersion = sys.env("BETTER_FILES_VERSION")

lazy val deps = Seq(
  "com.github.pathikrit" %% "better-files"      % betterFilesVersion,
  "com.twitter"          %% "finagle-http"    	% finagleVersion,
  "org.jblas" 	         % "jblas" 		% jblasVersion,
  "com.netflix.hystrix"  % "hystrix-core"       % hystrixVersion
)

