FROM fluxcapacitor/package-spark-2.1.0:master

WORKDIR /root

ENV \
  ZEPPELIN_VERSION=0.8.0-spark-2.1-scala-2.11-SNAPSHOT

ENV \
  ZEPPELIN_HOME=/root/zeppelin-${ZEPPELIN_VERSION}

RUN \
  wget https://s3.amazonaws.com/fluxcapacitor.com/packages/zeppelin-$ZEPPELIN_VERSION.tar.gz \
  && tar -xzvf zeppelin-$ZEPPELIN_VERSION.tar.gz \
  && rm zeppelin-$ZEPPELIN_VERSION.tar.gz

ENV \
  KUBERNETES_VERSION=1.6.1

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/bin/linux/amd64/kubectl

RUN \
  chmod a+x kubectl \
  && mv kubectl /usr/local/bin/kubectl

COPY lib/ lib/
COPY notebook/ notebook/

ENV \
  TENSORFLOW_VERSION=1.3.0-rc2

RUN \
  pip install tensorflow==$TENSORFLOW_VERSION

COPY config/zeppelin/ $ZEPPELIN_HOME/conf/

EXPOSE 3123 3124 6006

COPY run run

CMD ["supervise", "."]
