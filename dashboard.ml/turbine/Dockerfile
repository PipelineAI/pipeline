FROM fluxcapacitor/package-java-1.8

ENV \
 SPRING_CORE_VERSION=4.3.0.RELEASE \
 SPRING_BOOT_VERSION=1.3.5.RELEASE \
 SPRING_CLOUD_VERSION=1.1.2.RELEASE \
 SPRING_CLOUD_DEPENDENCIES=Brixton.RC1 

WORKDIR /root

RUN \
 apt-get update

COPY src/ src/
COPY pom.xml pom.xml

RUN \
  mvn -DskipTests clean install 

EXPOSE 8989 8990

WORKDIR /root

RUN \
  mkdir -p /root/logs

ENV LOGS_HOME=/root/logs

COPY run run

CMD ["supervise", "."]
