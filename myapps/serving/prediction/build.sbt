val globalSettings = Seq(
  version := "1.0",
  scalaVersion := sys.env("SCALA_VERSION") 
)

lazy val settings = (project in file("."))
                    .settings(name := "prediction")
                    .settings(globalSettings:_*)
                    .settings(libraryDependencies ++= deps)
		    .settings(javaOptions += "-Xmx10G")

val jblasVersion = sys.env("JBLAS_VERSION")
val hystrixVersion = sys.env("HYSTRIX_VERSION")
val breezeVersion = "0.11.2"
val dynoVersion = sys.env("DYNO_VERSION")
val springBootVersion = sys.env("SPRING_BOOT_VERSION")
val springCloudVersion = sys.env("SPRING_CLOUD_VERSION")

lazy val deps = Seq(
  "org.jblas" 	         % "jblas"          		   % jblasVersion,
  "com.netflix.hystrix" % "hystrix-javanica" % hystrixVersion,
  "org.springframework.boot" % "spring-boot-starter-web"   % springBootVersion,
  "org.scalanlp"         %% "breeze"                       % breezeVersion % "provided",
  "com.netflix.dyno"     % "dyno-jedis"                    % dynoVersion,
  "org.springframework.boot" % "spring-boot-starter-actuator" % springBootVersion,
  "org.springframework.cloud" % "spring-cloud-starter-spectator" % springCloudVersion,
  "org.springframework.cloud" % "spring-cloud-starter-eureka" % springCloudVersion,
  "org.springframework.cloud" % "spring-cloud-starter-hystrix" % springCloudVersion
//  "org.springframework.cloud" % "spring-cloud-starter-hystrix-dashboard" % springCloudVersion
)
