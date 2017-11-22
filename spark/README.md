Note:  These are not actually *custom*.  It's more about enabling various options at build-time

## Spark
* Hadoop 
* Hive
* Hive ThriftServer
* Ganglia
* netlib-java (Java wrapper around BLAS libs)
* SparkR

Details are [here](http://spark.apache.org/docs/latest/building-spark.html).

### Build Commands (Very Long...)
* Clone the branch/tag as follows:
```
git clone --branch 'v2.1.0' --single-branch https://github.com/apache/spark.git spark-v2.1.0
```
* Modify the `<protobuf.version>` to `2.6.1` in the main `pom.xml` file at the root of the Spark project
* (This is required for Spark ML + Stanford CoreNLP integration)
```
vi pom.xml
...
# change the to version 2.6.1
    <protobuf.version>2.6.1</protobuf.version>
```

* Create the Custom Spark Distribution
* Make sure you have installed R on [Mac OSX](https://cran.r-project.org/bin/macosx/) or [Linux](https://www.digitalocean.com/community/tutorials/how-to-set-up-r-on-ubuntu-14-04) before running the commands below.
```
which R
```
```
export R_HOME=/usr
```
* Install Proper Maven 3.3.9+
```
wget http://www.eu.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz; sudo tar -zxf apache-maven-3.3.9-bin.tar.gz -C /usr/local/; sudo ln -s /usr/local/apache-maven-3.3.9/bin/mvn /usr/local/bin/mvn
```
```
which mvn
```
```
export MAVEN_OPTS="-Xmx2g -XX:MaxPermSize=512M -XX:ReservedCodeCacheSize=512m"
```
```
./dev/make-distribution.sh --name fluxcapacitor --tgz -Phadoop-2.7 -Dhadoop.version=2.7.0 -Psparkr -Phive -Phive-thriftserver -Pspark-ganglia-lgpl -Pnetlib-lgpl -DskipTests
```

## Zeppelin Master
* Install Proper Maven 3.3.9+
```
wget http://www.eu.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz; sudo tar -zxf apache-maven-3.3.9-bin.tar.gz -C /usr/local/; sudo ln -s /usr/local/apache-maven-3.3.9/bin/mvn /usr/local/bin/mvn
```
```
which mvn
```
* Build Distribution
(More examples [here](https://github.com/apache/zeppelin/blob/master/README.md#example)

`spark-1.6, scala-2.10`
```
mvn clean package -Pbuild-distr -DskipTests -Pspark-1.6 -Phadoop-2.6 -Pyarn -Ppyspark -Psparkr
```
or `spark-2.0, scala-2.11`
```
./dev/change_scala_version.sh 2.11
...
mvn clean package -Pbuild-distr -DskipTests -Pspark-2.0 -Phadoop-2.6 -Pyarn -Ppyspark -Psparkr -Pscala-2.11
```
* Copy Distribution
```
cp zeppelin-distribution/target/*.tar.gz <wherever>
```

## [DEPRECATED] Build the distribution
```
mvn clean package -Pbuild-distr -Ppyspark -DskipTests -Drat.skip=true -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true
```

[DEPRECATED] Build new Spark 2.0 distribution
```
mvn clean install -Pscala-2.10 -Dscala.binary.version=2.10 -Dscala.version=2.10.5 -Pspark-2.0 -Dspark.version=2.0.1-SNAPSHOT -Phadoop-2.6 -Dhadoop.version=2.6.0 -Dmaven.findbugs.enable=false -Drat.skip=true -Ppyspark -Psparkr -Dcheckstyle.skip=true -Dcobertura.skip=true -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true -Pbuild-distr -DskipTests
```

## Troubleshooting
* If you see the following error  
```
Server access Error: Operation timed out url=https://repo1.maven.org/maven2/
```

Add the following to your `mvn` or `sbt` commands:
```
-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true
```
