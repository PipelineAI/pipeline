val globalSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.8" 
)

resolvers += "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += "Repo at github.com/ankurdave/maven-repo" at "https://github.com/ankurdave/maven-repo/raw/master"
resolvers += "Repo at github.com/ankurdave/maven-repo" at "https://github.com/ankurdave/maven-repo/raw/master"



lazy val streaming = (project in file("."))
                       .settings(name := "streaming")
                       .settings(globalSettings:_*)
                       .settings(libraryDependencies ++= streamingDeps)

val sparkVersion = "2.0.1" 

lazy val streamingDeps = Seq(
  "org.jblas"            % "jblas"                 	% "1.2.4",
  "com.madhukaraphatak" %% "java-sizeof" 		% "0.1",
  "com.datastax.spark"  %% "spark-cassandra-connector" 	% "1.4.0"	% "provided",
  "org.elasticsearch"   %% "elasticsearch-spark" 	% "2.3.0" 	% "provided",
  "redis.clients"        % "jedis" 			% "2.7.3" 	% "provided",
  "com.databricks"      %% "spark-avro" 		% "2.0.1" 	% "provided",
  "com.twitter"         %% "algebird-core" 		% "0.11.0"	% "provided",
  "org.apache.spark"    %% "spark-mllib"           	% sparkVersion 	% "provided",
  "org.apache.spark"    %% "spark-graphx"          	% sparkVersion 	% "provided",
  "org.apache.spark"    %% "spark-sql"             	% sparkVersion 	% "provided",
  "org.apache.spark"    %% "spark-streaming"       	% sparkVersion 	% "provided",
  "org.apache.spark"    %% "spark-streaming-kafka-0-10"	% sparkVersion 	% "provided",
  "amplab"               % "spark-indexedrdd" 		% "0.3"		% "provided",
//  "com.ankurdave"       %% "part"                   % "0.1"     % "provided",
  "com.ankurdave"        % "part_2.10" 			% "0.1"		% "provided",
// We can't promote this over version 2.5.0 otherwise it conflicts with Spark 1.6 version of Jackson
  "com.maxmind.geoip2"   % "geoip2"			% "2.5.0" 	% "provided"
)
