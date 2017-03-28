# Zeppelin

Application-specific source code, notebooks, libraries, and models have been moved to this [repo](https://github.com/fluxcapacitor/source.ml).

These repos are stitched together at deployment time using Kubernetes Volumes - similar to Docker Volumes.

## Building from Source - Requirements

| Name | Value | Requirement |
| --------------------- | ------------------------ | ------------------------------------------- |
| [Git](https://git-scm.com/downloads) | (Any Version) | [Clone Apache Zeppelin Github repo](https://github.com/apache/zeppelin.git) |
| [Maven](https://maven.apache.org/download.cgi) | 3.1.x or higher | Build Maven based POM.xml project | 
| [JDK](http://www.oracle.com/technetwork/java/javase/archive-139210.html) | 1.7 | Source |
| [R](https://cran.r-project.org/bin/macosx/) | 3.3.3 | -Psparkr -Pr |
| [Apache Spark](http://spark.apache.org/downloads.html) | 2.1.0 | -Pspark-2.1 -Dspark.version=2.1.0 |
| [Apache Hadoop](http://www.apache.org/dist/hadoop/common/) | 2.7.2 | -Phadoop-2.7 -Dhadoop.version=2.7.2 -Pyarn |

Install Git, JDK and NPM.
```
sudo apt-get update
sudo apt-get install git
sudo apt-get install openjdk-7-jdk
sudo apt-get install npm
sudo apt-get install libfontconfig
```

Verify node is installed and running `node --version`.

Maven, install and configure with additional memory.
```
wget http://www.eu.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
sudo tar -zxf apache-maven-3.3.9-bin.tar.gz -C /usr/local/
sudo ln -s /usr/local/apache-maven-3.3.9/bin/mvn /usr/local/bin/mvn
export MAVEN_OPTS="-Xmx2g -XX:MaxPermSize=1024m"
```

Verify maven is running version 3.1.x or higher `mvn -version`.


Add the following profiles to the root POM.xml to enable building with the latest version of Spark and Hadoop.
```
    <profile>
      <id>hadoop-2.7</id>
      <properties>
        <hadoop.version>2.7.2</hadoop.version>
        <protobuf.version>2.5.0</protobuf.version>
        <jets3t.version>0.9.0</jets3t.version>
        <avro.mapred.classifier>hadoop2</avro.mapred.classifier>
      </properties>
    </profile>

    <profile>
      <id>spark-2.1</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <spark.version>2.1.0</spark.version>
        <protobuf.version>2.5.0</protobuf.version>
        <py4j.version>0.10.4</py4j.version>
        <scala.version>2.11.8</scala.version>
      </properties>
    </profile>

    <profile>
      <id>allow-snapshots</id>
      <activation><activeByDefault>true</activeByDefault></activation>
      <repositories>
        <repository>
          <id>snapshots-repo</id>
          <url>https://oss.sonatype.org/content/repositories/snapshots</url>
          <releases><enabled>false</enabled></releases>
          <snapshots><enabled>true</enabled></snapshots>
        </repository>
      </repositories>
    </profile>
```

## Build Commands

Update all POM.xml dependencies to use scala 2.11.
```
# update all pom.xml to use scala 2.11
./dev/change_scala_version.sh 2.11
```

Build Zeppelin 0.8.0-SNAPSHOT using scala 2.11 with all interpreters, Apache Spark 2.1.0 for local mode and Hadoop 2.7.2 and package the final distribution including the compressed archive.

```
# build zeppelin and package the final distribution including the compressed archive:
mvn clean package -Pbuild-distr -DskipTests -Pspark-2.1 -Dspark.version=2.1.0 -Phadoop-2.7 -Dhadoop.version=2.7.2 -Pyarn -Ppyspark -Psparkr -Pr -Pscala-2.11
```

Packaged distribution archive: zeppelin-distribution/target/zeppelin-0.8.0-SNAPSHOT.tar.gz
