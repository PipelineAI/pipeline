FROM fluxcapacitor/package-java-openjdk-1.8

WORKDIR /root

ENV SPARK_VERSION=2.0.1
ENV PYSPARK_VERSION=0.10.3

RUN \
  # This is not a custom version of Spark.  It's merely a version with all the desired -P profiles enabled.
  wget https://s3.amazonaws.com/fluxcapacitor.com/packages/spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz \
  && tar xvzf spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz \
  && rm spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz

ENV SPARK_HOME=/root/spark-${SPARK_VERSION}-bin-fluxcapacitor
ENV PATH=$SPARK_HOME/bin:$PATH

COPY config/spark/ $SPARK_HOME/conf/
