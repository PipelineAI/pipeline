FROM fluxcapacitor/package-java-openjdk-1.8

WORKDIR /root

COPY src/ src/
COPY build.sbt build.sbt
COPY lib/ lib/
COPY store/ store/

RUN \
  sbt clean package 

RUN \
  mkdir -p /root/logs

ENV LOGS_HOME=/root/logs

COPY run run

EXPOSE 9040 

CMD ["supervise", "."]
