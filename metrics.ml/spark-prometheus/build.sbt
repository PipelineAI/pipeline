name := "spark-prometheus"

organization := "com.databricks"

scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.10.5", "2.11.7")

spName := "rxin/spark-prometheus"

sparkVersion := "2.0.0"

val testSparkVersion = settingKey[String]("The version of Spark to test against.")

testSparkVersion := sys.props.getOrElse("spark.testVersion", sparkVersion.value)

val testHadoopVersion = settingKey[String]("The version of Hadoop to test against.")

testHadoopVersion := sys.props.getOrElse("hadoop.testVersion", "2.2.0")

spAppendScalaVersion := true

spIncludeMaven := true

spIgnoreProvided := true

sparkComponents := Seq("sql")

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-client" % testHadoopVersion.value % "test",
  "org.apache.spark" %% "spark-core" % testSparkVersion.value % "test" exclude("org.apache.hadoop", "hadoop-client"),
  "org.apache.spark" %% "spark-sql" % testSparkVersion.value % "test" exclude("org.apache.hadoop", "hadoop-client")
)

// Display full-length stacktraces from ScalaTest:
testOptions in Test += Tests.Argument("-oF")

ScoverageSbtPlugin.ScoverageKeys.coverageHighlighting := {
  if (scalaBinaryVersion.value == "2.10") false
  else true
}

EclipseKeys.eclipseOutput := Some("target/eclipse")

/********************
 * Release settings *
 ********************/

publishMavenStyle := true

releaseCrossBuild := true

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

releasePublishArtifactsAction := PgpKeys.publishSigned.value

pomExtra :=
  <url>https://github.com/rxin/spark-prometheus</url>
  <scm>
    <url>git@github.com:rxin/spark-prometheus.git</url>
    <connection>scm:git:git@github.com:rxin/spark-prometheus.git</connection>
  </scm>
  <developers>
    <developer>
      <id>rxin</id>
      <name>Reynold Xin</name>
      <url>https://github.com/rxin</url>
    </developer>
  </developers>

bintrayReleaseOnPublish in ThisBuild := false

import ReleaseTransformations._

// Add publishing to spark packages as another step.
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges,
  releaseStepTask(spPublish)
)
