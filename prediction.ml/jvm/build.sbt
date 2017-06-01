val globalSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.8" 
)

(unmanagedClasspath in Compile) += file("lib/prometheus-hystrix-2.0.0.jar")
(unmanagedClasspath in Compile) += file("lib/dbml-local-0.1.2-spark2.1.jar")

lazy val settings = (project in file("."))
                    .settings(name := "prediction-jvm")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= deps)
		    .settings(javaOptions += "-Xmx10G")

val jblasVersion = "1.2.4" 
val breezeVersion = "0.11.2"
val jedisVersion = "2.7.3" 
val springBootVersion = "1.3.5.RELEASE" 
val springCloudVersion = "1.1.2.RELEASE" 
val springCoreVersion = "4.3.0.RELEASE" 
val pmmlEvaluatorVersion = "1.3.3" 
val pmmlModelVersion = "1.3.3" 
val pmmlMetroVersion = "1.3.3" 
val janinoVersion = "2.7.8" 
val codahaleMetricsVersion = "3.1.2"
val httpClientVersion = "4.5.2"
val fluentHCVersion = "4.5.2"
val prometheusSimpleClientVersion = "0.0.21"
val prometheusSimpleClientHotspotVersion = "0.0.21"
val commonsCompressVersion = "1.13"
val commonsIOVersion = "1.3.2"
val tensorflowVersion = "1.2.0-rc1"

lazy val deps = Seq(
  "io.prometheus" % "simpleclient_hotspot" % prometheusSimpleClientHotspotVersion, 
  "io.prometheus" % "simpleclient_spring_boot" % prometheusSimpleClientVersion, 
  "org.jpmml" % "pmml-model" % "1.3.3",
  "org.jpmml" % "pmml-evaluator" % "1.3.2",
  "io.dropwizard.metrics" % "metrics-core" % codahaleMetricsVersion,
  "org.codehaus.janino"  % "janino"             % janinoVersion,
  "org.codehaus.janino"  % "commons-compiler"   % janinoVersion,
  "org.jblas" 	         % "jblas"          		   % jblasVersion,
  "org.springframework.boot" % "spring-boot-starter-web"   % springBootVersion,
  "org.scalanlp"         %% "breeze"                       % breezeVersion % "provided",
  "redis.clients"      % "jedis"     % jedisVersion, 
  "org.springframework.boot" % "spring-boot-starter-actuator" % springBootVersion,
  "org.springframework.cloud" % "spring-cloud-starter-spectator" % springCloudVersion,
  "org.springframework.cloud" % "spring-cloud-starter-eureka" % springCloudVersion,
  "org.springframework.cloud" % "spring-cloud-starter-hystrix" % springCloudVersion,
// This must stay 1.1.1.RELEASE or jackson.xml errors happen at runtime
  "org.springframework.cloud" % "spring-cloud-starter-config" % "1.1.1.RELEASE",
  "org.jpmml" % "pmml-model-metro" % pmmlMetroVersion,
  "org.jpmml" % "pmml-model" % pmmlModelVersion,
  "org.jpmml" % "pmml-evaluator" % pmmlEvaluatorVersion,
  "org.jpmml" % "pmml-evaluator-extension" % pmmlEvaluatorVersion,
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "org.apache.httpcomponents" % "httpclient" % httpClientVersion,
  "org.apache.httpcomponents" % "fluent-hc" % fluentHCVersion,
  "org.apache.commons" % "commons-compress" % commonsCompressVersion,
  "org.apache.commons" % "commons-io" % commonsIOVersion,
  "org.tensorflow" % "tensorflow" % tensorflowVersion
)
