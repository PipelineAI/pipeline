val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

// https://github.com/thammegowda/tensorflow-grpc-java
(unmanagedClasspath in Compile) += file("/root/myapps/serving/prediction/lib/tensorflow-java-1.0-jar-with-dependencies.jar")
(unmanagedClasspath in Compile) += file("/root/myapps/serving/prediction/lib/codegen-spark-1-6-1_2.10-1.0.jar")

lazy val settings = (project in file("."))
                    .settings(name := "prediction")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= deps)
		    .settings(javaOptions += "-Xmx10G")

val jblasVersion = sys.env("JBLAS_VERSION")
val breezeVersion = "0.11.2"
val dynoVersion = sys.env("DYNO_VERSION")
val springBootVersion = sys.env("SPRING_BOOT_VERSION")
val springCloudVersion = sys.env("SPRING_CLOUD_VERSION")
val springCoreVersion = sys.env("SPRING_CORE_VERSION")
val springCloudDependenciesVersion = sys.env("SPRING_CLOUD_DEPENDENCIES")
val pmmlEvaluatorVersion = sys.env("PMML_EVALUATOR_VERSION")
val pmmlModelVersion = sys.env("PMML_MODEL_VERSION")
val pmmlMetroVersion = sys.env("PMML_METRO_VERSION")
val janinoVersion = sys.env("JANINO_VERSION")
val codahaleMetricsVersion = sys.env("CODAHALE_METRICS_VERSION")

lazy val deps = Seq(
  "io.dropwizard.metrics" % "metrics-core" % codahaleMetricsVersion,
  "org.codehaus.janino"  % "janino"             % janinoVersion,
  "org.codehaus.janino"  % "commons-compiler"   % janinoVersion,
  "org.jblas" 	         % "jblas"          		   % jblasVersion,
  "org.springframework.boot" % "spring-boot-starter-web"   % springBootVersion,
  "org.scalanlp"         %% "breeze"                       % breezeVersion % "provided",
  "com.netflix.dyno"     % "dyno-jedis"                    % dynoVersion,
  "org.springframework.boot" % "spring-boot-starter-actuator" % springBootVersion,
  "org.springframework.cloud" % "spring-cloud-starter-spectator" % springCloudVersion,
  "org.springframework.cloud" % "spring-cloud-starter-eureka" % springCloudVersion,
  "org.springframework.cloud" % "spring-cloud-starter-hystrix" % springCloudVersion,
  "org.springframework.cloud" % "spring-cloud-starter-atlas" % springCloudVersion,
//  This must stay 1.1.1.RELEASE or jackson.xml errors happen at runtime 
  "org.springframework.cloud" % "spring-cloud-starter-config" % "1.1.1.RELEASE", 
  "org.jpmml" % "pmml-model-metro" % pmmlMetroVersion,
  "org.jpmml" % "pmml-model" % pmmlModelVersion,
  "org.jpmml" % "pmml-evaluator" % pmmlEvaluatorVersion 
)
