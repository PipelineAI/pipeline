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

RUN \
  echo "deb http://cran.rstudio.com/bin/linux/ubuntu trusty/" >> /etc/apt/sources.list \
  && gpg --keyserver keyserver.ubuntu.com --recv-key E084DAB9 \
  && gpg -a --export E084DAB9 | apt-key add - \
  && apt-get update \
  && apt-get install -y r-base \
  && apt-get install -y r-base-dev

ENV \
  KUBERNETES_VERSION=1.5.1

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/bin/linux/amd64/kubectl

RUN \
  sudo chmod a+x kubectl \
  && sudo mv kubectl /usr/local/bin/kubectl

# lib & notesbooks provided via Kubernets PersistentVolume Mount
# from https://github.wvrgroup.internal/omni-hub/source.ml -> /root/pipeline
# .kube/config is volume mounted from Kubernetes secret

COPY config/zeppelin/ $ZEPPELIN_HOME/conf/

EXPOSE 3123 3124

COPY run run

CMD ["supervise", "."]
