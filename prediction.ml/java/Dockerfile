FROM fluxcapacitor/package-java-openjdk-1.8

WORKDIR /root

COPY src/ src/
COPY build.sbt build.sbt
COPY lib/ lib/

RUN \
  sbt clean package 

RUN \
  mkdir -p /root/logs

ENV LOGS_HOME=/root/logs

ENV \
  STORE_HOME=/root/store

RUN \
  ln -s /root/volumes/source.ml/prediction.ml/java/store

COPY run run

EXPOSE 9040 

CMD ["supervise", "."]
