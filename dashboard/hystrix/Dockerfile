FROM fluxcapacitor/package-ubuntu-16.04:master

WORKDIR /root

ENV \
  TERM=xterm

###################
# Setup OpenJDK 1.8
###################
RUN \
  apt-get update \
  && apt-get install -y software-properties-common \
  && add-apt-repository -y ppa:openjdk-r/ppa \
  && apt-get update \
  && apt-get install -y --no-install-recommends openjdk-8-jdk openjdk-8-jre-headless \
  && apt-get install -y apt-transport-https \
  && apt-get install -y wget \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

ENV \
  JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/

ENV \
  SHELL=/bin/bash

RUN \
  rm /bin/sh \
  && ln -s /bin/bash /bin/sh

RUN \
  mkdir -p /root/logs

ENV \
  LOGS_HOME=/root/logs

COPY build.gradle build.gradle
COPY codequality/ codequality/
COPY gradle/ gradle/
COPY gradle.properties gradle.properties
COPY gradlew gradlew
COPY hystrix-contrib/ hystrix-contrib/
COPY hystrix-core/ hystrix-core/
COPY hystrix-dashboard/ hystrix-dashboard/
COPY hystrix-serialization/ hystrix-serialization/
COPY run run
COPY settings.gradle settings.gradle
COPY sysutils/ sysutils/
COPY LICENSE-2.0.txt LICENSE-2.0.txt
COPY OSSMETADATA OSSMETADATA
COPY jetty-0.4.7.RC0.jar jetty-0.4.7.RC0.jar

ENV \
  HYSTRIX_DASHBOARD_PORT=7979

RUN \
  cd hystrix-dashboard && ../gradlew --no-daemon buildAllProducts assemble -x test

EXPOSE \
  # Hystrix Dashboard \
  7979

CMD ["supervise", "."]
