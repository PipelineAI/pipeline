resolvers += Resolver.url("artifactory", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += "Spark Package Main Repo" at "https://dl.bintray.com/spark-packages/maven"

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("org.spark-packages" % "sbt-spark-package" % "0.2.3")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.1.0")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.0")


