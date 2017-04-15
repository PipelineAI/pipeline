val globalSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.8" 
)

(unmanagedClasspath in Compile) += file("lib/prometheus-hystrix-2.0.0.jar")

lazy val settings = (project in file("."))
                    .settings(name := "prediction-pmml")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= deps)
		    .settings(javaOptions += "-Xmx10G")

val springBootVersion = "1.5.2.RELEASE"
val springCloudVersion = "1.2.6.RELEASE"
val pmmlEvaluatorVersion = "1.3.3" 
val pmmlModelVersion = "1.3.3" 
val pmmlMetroVersion = "1.3.3" 
val janinoVersion = "2.7.8" 
val prometheusSimpleClientVersion = "0.0.21"
val prometheusSimpleClientHotspotVersion = "0.0.21"

lazy val deps = Seq(
  "io.prometheus" % "simpleclient_hotspot" % prometheusSimpleClientHotspotVersion, 
  "io.prometheus" % "simpleclient_spring_boot" % prometheusSimpleClientVersion, 
  "org.codehaus.janino"  % "janino"             % janinoVersion,
  "org.codehaus.janino"  % "commons-compiler"   % janinoVersion,
  "org.springframework.boot" % "spring-boot-starter-web" % springBootVersion,
  "org.springframework.boot" % "spring-boot-starter-actuator" % springBootVersion,
  "org.springframework.cloud" % "spring-cloud-starter-hystrix" % springCloudVersion,
  "org.jpmml" % "pmml-model-metro" % pmmlMetroVersion,
  "org.jpmml" % "pmml-model" % pmmlModelVersion,
  "org.jpmml" % "pmml-evaluator" % pmmlEvaluatorVersion,
  "org.jpmml" % "pmml-evaluator-extension" % pmmlEvaluatorVersion,
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"
)
