FROM fluxcapacitor/package-java-1.8

WORKDIR /root

COPY src/ src/
COPY build.sbt build.sbt
COPY lib/ lib/

RUN \
  sbt clean package \
# HACK:  We need to trigger the download of sbt-launch.jar which is a diff version than default sbt install
  && java -Djava.security.egd=file:/dev/./urandom -cp lib/codegen-spark-2_0_1_2.11-1.0.jar -jar lib/sbt-launch.jar exit

RUN \
  mkdir -p /root/logs

ENV LOGS_HOME=/root/logs

COPY run run

EXPOSE 9040 

CMD ["supervise", "."]
