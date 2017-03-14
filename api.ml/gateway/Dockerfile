FROM fluxcapacitor/package-java-openjdk-1.8

#ENV \
# SPRING_CORE_VERSION=4.3.0.RELEASE \
# SPRING_BOOT_VERSION=1.3.5.RELEASE \
# SPRING_CLOUD_VERSION=1.1.2.RELEASE \
# SPRING_CLOUD_DEPENDENCIES=Brixton.RC1 

WORKDIR /root

RUN \
 apt-get update

COPY src/ src/
COPY lib/ lib/
COPY build.sbt build.sbt

RUN \
  sbt clean package

EXPOSE 8080 

WORKDIR /root

RUN \
  mkdir -p /root/logs

ENV LOGS_HOME=/root/logs

COPY run run

CMD ["supervise", "."]
