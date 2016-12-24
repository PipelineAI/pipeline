val globalSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.8" 
)

  (unmanagedClasspath in Compile) += file("lib/tensorflow-prediction-client-1.0-SNAPSHOT.jar")

  lazy val settings = (project in file("."))
                    .settings(name := "prediction-tensorflow")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= deps)
		    .settings(javaOptions += "-Xmx10G")

  val springBootVersion = "1.3.5.RELEASE"
  val springCloudVersion = "1.1.2.RELEASE"
  val springCoreVersion = "4.3.0.RELEASE"
  val grpcVersion = "1.0.0"
  val protobufVersion = "3.0.0"
  val javacppTensorflowPresetVersion = "0.11.0-1.3"

  lazy val deps = Seq(
    "org.springframework.boot" % "spring-boot-starter-web"   % springBootVersion,
    "org.springframework.boot" % "spring-boot-starter-actuator" % springBootVersion,
    "org.springframework.cloud" % "spring-cloud-starter-spectator" % springCloudVersion,
    "org.springframework.cloud" % "spring-cloud-starter-eureka" % springCloudVersion,
    "org.springframework.cloud" % "spring-cloud-starter-hystrix" % springCloudVersion,
    "org.springframework.cloud" % "spring-cloud-starter-atlas" % springCloudVersion,
// This must stay 1.1.1.RELEASE or jackson.xml errors happen at runtime
    "org.springframework.cloud" % "spring-cloud-starter-config" % "1.1.1.RELEASE",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
    "io.grpc" % "grpc-netty" % grpcVersion,
    "io.grpc" % "grpc-protobuf" % grpcVersion,
    "io.grpc" % "grpc-stub" % grpcVersion,
    "com.google.protobuf" % "protobuf-java" % protobufVersion,
    "org.bytedeco.javacpp-presets" % "tensorflow-platform" % javacppTensorflowPresetVersion
  )
