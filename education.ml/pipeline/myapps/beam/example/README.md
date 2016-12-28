# Apache Beam Example Code

An example Apache Beam project.

### Description

This example can be used with conference talks and self-study. The base of the examples are taken from Beam's `example` directory. They are modified to use Beam as a dependency in the `pom.xml` instead of being compiled together. The example code is changed to output to local directories.

## How to clone and run

1. Open a terminal window.
1. Run `git clone git@github.com:eljefe6a/beamexample.git`
1. Run `cd beamexample/BeamTutorial`
1. Run `mvn compile`
1. Create local output directory: `mkdir output`
1. Run `mvn exec:java -Dexec.mainClass="org.apache.beam.examples.tutorial.game.solution.Exercise1"`
1. Run `cat output/user_score` to verify the program ran correctly and the output file was created.

### Using Eclipse

1. Run `mvn eclipse:eclipse`
1. Import the project.

### Using IntelliJ

1. Import the Maven project.

## Other Runners

Create the package.

1. `mvn package`

### Apache Flink

1. Follow the first steps from [Flink's Quickstart](https://ci.apache.org/projects/flink/flink-docs-release-1.1/quickstart/setup_quickstart.html) to [download Flink](https://ci.apache.org/projects/flink/flink-docs-release-1.1/quickstart/setup_quickstart.html#download) and [start a local Flink cluster](https://ci.apache.org/projects/flink/flink-docs-release-1.1/quickstart/setup_quickstart.html#start-a-local-flink-cluster).
1. Create the `output` directory.
1. Run `mvn exec:java -Dexec.mainClass=org.apache.beam.examples.tutorial.game.solution.Exercise1 -Dexec.args='--runner=FlinkRunner --flinkMaster=[local]'` (it will not terminate - just kill it when it is finished)
1. Run `cat output/user_score` to verify the pipeline ran correctly and the output file was created.

### Apache Spark

1. Create the `output` directory.
1. Allow all users (Spark may run as a different user) to write to the `output` directory. `chmod 1777 output`.
1. Change the output file to a fully-qualified path. For example, `this("output/user_score");` to `this("/home/vmuser/output/user_score");`
1. Run `mvn package`
1. Run `spark-submit --jars ~/.m2/repository/org/apache/beam/beam-runners-spark/0.3.0-incubating-SNAPSHOT/beam-runners-spark-0.3.0-incubating-SNAPSHOT.jar --class org.apache.beam.examples.tutorial.game.solution.Exercise2 --master yarn-client target/Tutorial-0.0.1-SNAPSHOT.jar --runner=SparkRunner`

## Running Example Code in Spark

To run one of the Beam examples on Spark, do this:

1. Place a text file like Shakespeare in HDFS.
1. Place the same file locally. This is due to [a bug](https://issues.apache.org/jira/browse/BEAM-645). `touch Macbeth.txt`
1. Run `spark-submit --jars /home/vmuser/.m2/repository/org/apache/beam/beam-runners-spark/0.3.0-incubating-SNAPSHOT/beam-runners-spark-0.3.0-incubating-SNAPSHOT.jar,/home/vmuser/.m2/repository/org/apache/beam/beam-sdks-java-core/0.3.0-incubating-SNAPSHOT/beam-sdks-java-core-0.3.0-incubating-SNAPSHOT.jar,/home/vmuser/.m2/repository/io/grpc/grpc-auth/0.14.1/grpc-auth-0.14.1.jar,/home/vmuser/.m2/repository/io/grpc/grpc-core/0.14.1/grpc-core-0.14.1.jar,/home/vmuser/.m2/repository/io/grpc/grpc-netty/0.14.1/grpc-netty-0.14.1.jar,/home/vmuser/.m2/repository/io/netty/netty-codec-http2/4.1.1.Final/netty-codec-http2-4.1.1.Final.jar,/home/vmuser/.m2/repository/io/netty/netty-codec-http/4.1.1.Final/netty-codec-http-4.1.1.Final.jar,/home/vmuser/.m2/repository/io/grpc/grpc-all/0.14.1/grpc-all-0.14.1.jar,/home/vmuser/.m2/repository/io/grpc/grpc-protobuf/0.14.1/grpc-protobuf-0.14.1.jar,/home/vmuser/.m2/repository/com/google/protobuf/protobuf-java-util/3.0.0-beta-2/protobuf-java-util-3.0.0-beta-2.jar,/home/vmuser/.m2/repository/com/google/code/gson/gson/2.3/gson-2.3.jar,/home/vmuser/.m2/repository/io/grpc/grpc-stub/0.14.1/grpc-stub-0.14.1.jar,/home/vmuser/.m2/repository/io/grpc/grpc-okhttp/0.14.1/grpc-okhttp-0.14.1.jar,/home/vmuser/.m2/repository/com/squareup/okio/okio/1.6.0/okio-1.6.0.jar,/home/vmuser/.m2/repository/com/squareup/okhttp/okhttp/2.5.0/okhttp-2.5.0.jar,/home/vmuser/.m2/repository/io/grpc/grpc-protobuf-lite/0.14.1/grpc-protobuf-lite-0.14.1.jar,/home/vmuser/.m2/repository/io/grpc/grpc-protobuf-nano/0.14.1/grpc-protobuf-nano-0.14.1.jar,/home/vmuser/.m2/repository/com/google/protobuf/nano/protobuf-javanano/3.0.0-alpha-5/protobuf-javanano-3.0.0-alpha-5.jar,/home/vmuser/.m2/repository/com/google/auth/google-auth-library-oauth2-http/0.4.0/google-auth-library-oauth2-http-0.4.0.jar,/home/vmuser/.m2/repository/com/google/auth/google-auth-library-credentials/0.4.0/google-auth-library-credentials-0.4.0.jar,/home/vmuser/.m2/repository/io/netty/netty-handler/4.1.1.Final/netty-handler-4.1.1.Final.jar,/home/vmuser/.m2/repository/io/netty/netty-buffer/4.1.1.Final/netty-buffer-4.1.1.Final.jar,/home/vmuser/.m2/repository/io/netty/netty-common/4.1.1.Final/netty-common-4.1.1.Final.jar,/home/vmuser/.m2/repository/io/netty/netty-transport/4.1.1.Final/netty-transport-4.1.1.Final.jar,/home/vmuser/.m2/repository/io/netty/netty-resolver/4.1.1.Final/netty-resolver-4.1.1.Final.jar,/home/vmuser/.m2/repository/io/netty/netty-codec/4.1.1.Final/netty-codec-4.1.1.Final.jar,/home/vmuser/.m2/repository/com/google/api/grpc/grpc-pubsub-v1/0.0.2/grpc-pubsub-v1-0.0.2.jar,/home/vmuser/.m2/repository/com/google/api/grpc/grpc-core-proto/0.0.3/grpc-core-proto-0.0.3.jar,/home/vmuser/.m2/repository/com/google/api-client/google-api-client/1.22.0/google-api-client-1.22.0.jar,/home/vmuser/.m2/repository/com/google/apis/google-api-services-pubsub/v1-rev10-1.22.0/google-api-services-pubsub-v1-rev10-1.22.0.jar,/home/vmuser/.m2/repository/com/google/apis/google-api-services-storage/v1-rev71-1.22.0/google-api-services-storage-v1-rev71-1.22.0.jar,/home/vmuser/.m2/repository/com/google/http-client/google-http-client/1.22.0/google-http-client-1.22.0.jar,/home/vmuser/.m2/repository/org/apache/httpcomponents/httpclient/4.0.1/httpclient-4.0.1.jar,/home/vmuser/.m2/repository/org/apache/httpcomponents/httpcore/4.0.1/httpcore-4.0.1.jar,/home/vmuser/.m2/repository/com/google/http-client/google-http-client-jackson/1.22.0/google-http-client-jackson-1.22.0.jar,/home/vmuser/.m2/repository/com/google/http-client/google-http-client-jackson2/1.22.0/google-http-client-jackson2-1.22.0.jar,/home/vmuser/.m2/repository/com/google/http-client/google-http-client-protobuf/1.22.0/google-http-client-protobuf-1.22.0.jar,/home/vmuser/.m2/repository/com/google/oauth-client/google-oauth-client-java6/1.22.0/google-oauth-client-java6-1.22.0.jar,/home/vmuser/.m2/repository/com/google/oauth-client/google-oauth-client/1.22.0/google-oauth-client-1.22.0.jar,/home/vmuser/.m2/repository/com/google/cloud/bigdataoss/gcsio/1.4.5/gcsio-1.4.5.jar,/home/vmuser/.m2/repository/com/google/api-client/google-api-client-java6/1.20.0/google-api-client-java6-1.20.0.jar,/home/vmuser/.m2/repository/com/google/api-client/google-api-client-jackson2/1.20.0/google-api-client-jackson2-1.20.0.jar,/home/vmuser/.m2/repository/com/google/cloud/bigdataoss/util/1.4.5/util-1.4.5.jar,/home/vmuser/.m2/repository/com/google/protobuf/protobuf-java/3.0.0-beta-1/protobuf-java-3.0.0-beta-1.jar,/home/vmuser/.m2/repository/com/google/code/findbugs/annotations/3.0.1/annotations-3.0.1.jar,/home/vmuser/.m2/repository/net/jcip/jcip-annotations/1.0/jcip-annotations-1.0.jar,/home/vmuser/.m2/repository/com/google/code/findbugs/jsr305/3.0.1/jsr305-3.0.1.jar,/home/vmuser/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.7.2/jackson-core-2.7.2.jar,/home/vmuser/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.7.2/jackson-annotations-2.7.2.jar,/home/vmuser/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.7.2/jackson-databind-2.7.2.jar,/home/vmuser/.m2/repository/net/bytebuddy/byte-buddy/1.4.3/byte-buddy-1.4.3.jar,/home/vmuser/.m2/repository/org/apache/avro/avro/1.8.1/avro-1.8.1.jar,/home/vmuser/.m2/repository/org/codehaus/jackson/jackson-core-asl/1.9.13/jackson-core-asl-1.9.13.jar,/home/vmuser/.m2/repository/org/codehaus/jackson/jackson-mapper-asl/1.9.13/jackson-mapper-asl-1.9.13.jar,/home/vmuser/.m2/repository/com/thoughtworks/paranamer/paranamer/2.7/paranamer-2.7.jar,/home/vmuser/.m2/repository/org/tukaani/xz/1.5/xz-1.5.jar,/home/vmuser/.m2/repository/org/xerial/snappy/snappy-java/1.1.2.1/snappy-java-1.1.2.1.jar,/home/vmuser/.m2/repository/org/apache/commons/commons-compress/1.9/commons-compress-1.9.jar,/home/vmuser/.m2/repository/joda-time/joda-time/2.4/joda-time-2.4.jar,/home/vmuser/.m2/repository/org/apache/beam/beam-runners-direct-java/0.3.0-incubating-SNAPSHOT/beam-runners-direct-java-0.3.0-incubating-SNAPSHOT.jar,/home/vmuser/.m2/repository/org/apache/beam/beam-runners-core-java/0.3.0-incubating-SNAPSHOT/beam-runners-core-java-0.3.0-incubating-SNAPSHOT.jar,/home/vmuser/.m2/repository/com/google/apis/google-api-services-bigquery/v2-rev312-1.22.0/google-api-services-bigquery-v2-rev312-1.22.0.jar,/home/vmuser/.m2/repository/org/apache/beam/beam-sdks-java-io-google-cloud-platform/0.3.0-incubating-SNAPSHOT/beam-sdks-java-io-google-cloud-platform-0.3.0-incubating-SNAPSHOT.jar,/home/vmuser/.m2/repository/com/google/cloud/datastore/datastore-v1-proto-client/1.1.0/datastore-v1-proto-client-1.1.0.jar,/home/vmuser/.m2/repository/com/google/cloud/datastore/datastore-v1-protos/1.0.1/datastore-v1-protos-1.0.1.jar,/home/vmuser/.m2/repository/com/google/cloud/bigtable/bigtable-protos/0.9.1/bigtable-protos-0.9.1.jar,/home/vmuser/.m2/repository/com/google/cloud/bigtable/bigtable-client-core/0.9.1/bigtable-client-core-0.9.1.jar,/home/vmuser/.m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar,/home/vmuser/.m2/repository/com/google/auth/google-auth-library-appengine/0.4.0/google-auth-library-appengine-0.4.0.jar,/home/vmuser/.m2/repository/com/google/appengine/appengine-api-1.0-sdk/1.9.34/appengine-api-1.0-sdk-1.9.34.jar,/home/vmuser/.m2/repository/com/google/guava/guava/19.0/guava-19.0.jar,/home/vmuser/.m2/repository/io/netty/netty-tcnative-boringssl-static/1.1.33.Fork18/netty-tcnative-boringssl-static-1.1.33.Fork18.jar,/home/vmuser/.m2/repository/org/apache/beam/beam-runners-spark/0.3.0-incubating-SNAPSHOT/beam-runners-spark-0.3.0-incubating-SNAPSHOT.jar,/home/vmuser/.m2/repository/org/apache/beam/beam-examples-java8/0.3.0-incubating-SNAPSHOT/beam-examples-java8-0.3.0-incubating-SNAPSHOT.jar,/home/vmuser/.m2/repository/org/apache/beam/beam-examples-java/0.3.0-incubating-SNAPSHOT/beam-examples-java-0.3.0-incubating-SNAPSHOT.jar --class org.apache.beam.examples.WordCount --master yarn-client ~/.m2/repository/org/apache/beam/beam-examples-java/0.3.0-incubating-SNAPSHOT/beam-examples-java-0.3.0-incubating-SNAPSHOT.jar --runner=SparkRunner --sparkMaster=local --inputFile=Macbeth.txt --output=output/output`

**NOTE:** This classpath is massive. You'd normally shade this JAR to include these dependencies. The ones from Maven aren't shaded so we're having to add them manually.

## Further Reading

* The World Beyond Batch Streaming [Part 1](https://www.oreilly.com/ideas/the-world-beyond-batch-streaming-101) and [Part 2](https://www.oreilly.com/ideas/the-world-beyond-batch-streaming-102)
* [Future-Proof Your Big Data Processing with Apache Beam](http://thenewstack.io/apache-beam-will-make-big-difference-organization/)
* [Future-proof and scale-proof your code](https://www.oreilly.com/ideas/future-proof-and-scale-proof-your-code)
* [Question and Answers with the Apache Beam Team](http://www.jesse-anderson.com/2016/07/question-and-answers-with-the-apache-beam-team/)
