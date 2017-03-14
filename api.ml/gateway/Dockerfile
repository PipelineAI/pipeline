FROM fluxcapacitor/package-java-openjdk-1.8

WORKDIR /root

RUN \
 apt-get update

COPY src/ src/
COPY lib/ lib/
COPY build.sbt build.sbt

RUN \
  sbt clean package

EXPOSE 9040 

WORKDIR /root

RUN \
  mkdir -p /root/logs

ENV LOGS_HOME=/root/logs

COPY run run

CMD ["supervise", "."]
