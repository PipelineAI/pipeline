val globalSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.12" 
)

//-Dhttp.proxyHost=your.proxy.server
//-Dhttp.proxyPort=8080
//-Dhttps.proxyHost=your.proxy.server
//-Dhttps.proxyPort=8080

sourcesInBase := false
scalaSource in Compile := baseDirectory.value / "src"
javaSource in Compile := baseDirectory.value / "src"

mainClass in (Compile, packageBin) := Some("ai.pipeline.predict.jvm.PredictionServiceMain")

lazy val settings = (project in file("."))
                    .settings(name := "prediction-jvm")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= deps)

val jblasVersion = "1.2.4" 
val breezeVersion = "0.13.2"
val jedisVersion = "2.9.0" 
val springBootVersion = "1.5.7.RELEASE"
val springCloudVersion = "1.3.5.RELEASE"
val springCloudStarterConfigVersion = "1.3.2.RELEASE"
val springCloudStarterSpectatorEurekaHystrixVersion = "1.3.4.RELEASE"
val scalaParserCombinatorsVersion = "1.1.1"
val pmmlEvaluatorVersion = "1.3.9" 
val pmmlModelVersion = "1.3.8" 
val pmmlMetroVersion = "1.3.8" 
val janinoVersion = "3.0.0" 
val codahaleMetricsVersion = "3.1.2"
val httpClientVersion = "4.5.3"
val fluentHCVersion = "4.5.3"
val prometheusSimpleClientVersion = "0.0.26"
val prometheusSimpleClientHotspotVersion = "0.0.26"
val commonsCompressVersion = "1.14"
val commonsIOVersion = "1.3.2"
val tensorflowVersion = "1.4.0"
val grpcVersion = "1.6.1"
val protobufVersion = "3.4.0"
val prometheusHystrixVersion = "3.2.0"
val mleapVersion = "0.12.0"

lazy val deps = Seq(
  "io.prometheus" % "simpleclient_hotspot" % prometheusSimpleClientHotspotVersion, 
  "io.prometheus" % "simpleclient_spring_boot" % prometheusSimpleClientVersion, 
  "io.dropwizard.metrics" % "metrics-core" % codahaleMetricsVersion,
  "org.codehaus.janino"  % "janino"             % janinoVersion,
  "org.codehaus.janino"  % "commons-compiler"   % janinoVersion,
  "org.jblas" 	         % "jblas"          		   % jblasVersion,
  "org.springframework.boot" % "spring-boot-starter-web"   % springBootVersion,
  "ml.combust.mleap" %% "mleap-core" % mleapVersion,
  "ml.combust.mleap" %% "mleap-runtime" % mleapVersion,
  "ml.combust.mleap" %% "mleap-base" % mleapVersion,
  "ml.combust.mleap" %% "mleap-spark" % mleapVersion,
  "ml.combust.mleap" %% "mleap-spark-base" % mleapVersion,
  "org.scalanlp"         %% "breeze"                       % breezeVersion % "provided",
  "redis.clients"      % "jedis"     % jedisVersion, 
  "org.springframework.boot" % "spring-boot-starter-actuator" % springBootVersion,
  "org.springframework.cloud" % "spring-cloud-starter-spectator" % springCloudStarterSpectatorEurekaHystrixVersion,
  "org.springframework.cloud" % "spring-cloud-starter-eureka" % springCloudStarterSpectatorEurekaHystrixVersion,
  "org.springframework.cloud" % "spring-cloud-starter-hystrix" % springCloudStarterSpectatorEurekaHystrixVersion,
  "org.springframework.cloud" % "spring-cloud-starter-config" % springCloudStarterConfigVersion,
  "org.jpmml" % "pmml-model-metro" % pmmlMetroVersion,
  "org.jpmml" % "pmml-model" % pmmlModelVersion,
  "org.jpmml" % "pmml-evaluator" % pmmlEvaluatorVersion,
  "org.jpmml" % "pmml-evaluator-extension" % pmmlEvaluatorVersion,
  "org.scala-lang.modules" %% "scala-parser-combinators" % scalaParserCombinatorsVersion,
  "io.grpc" % "grpc-netty" % grpcVersion,
  "io.grpc" % "grpc-protobuf" % grpcVersion,
  "io.grpc" % "grpc-stub" % grpcVersion,
  "com.google.protobuf" % "protobuf-java" % protobufVersion,
  "org.apache.httpcomponents" % "httpclient" % httpClientVersion,
  "org.apache.httpcomponents" % "fluent-hc" % fluentHCVersion,
  "org.apache.commons" % "commons-compress" % commonsCompressVersion,
  "commons-io" % "commons-io" % commonsIOVersion,
  "org.tensorflow" % "tensorflow" % tensorflowVersion,
  "de.ahus1.prometheus.hystrix" % "prometheus-hystrix" % prometheusHystrixVersion
)
