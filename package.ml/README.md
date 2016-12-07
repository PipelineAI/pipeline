## Build Docker Images

### Ubuntu 14.04
```
cd ubuntu/14.04

docker build -t fluxcapacitor/package-ubuntu-14.04 .
```

### Java 1.8
```
cd java/1.8

docker build -t fluxcapacitor/package-java-1.8 .
```

### Kafka 0.10
```
cd kafka/0.10

docker build -t fluxcapacitor/package-kafka-0.10 .
```

### Spark 2.0.1
```
cd spark/2.0.1

docker build -t fluxcapacitor/package-spark-2.0.1 .
```

### Anaconda 4.0.5 (including tensorflow)
```
cd anaconda/4.0.5

docker build -t fluxcapacitor/package-anaconda-4.0.5 .
```

### Apache2
```
cd apache2

docker build -t fluxcapacitor/package-apache2 .
```
