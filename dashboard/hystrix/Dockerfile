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
  echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list \
  && apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 \
  && apt-get update \
  && apt-get install -y sbt

# Install Anaconda with Python3
RUN wget -q https://repo.continuum.io/miniconda/Miniconda3-4.3.21-Linux-x86_64.sh -O /tmp/miniconda.sh  && \
    echo 'c1c15d3baba15bf50293ae963abef853 */tmp/miniconda.sh' | md5sum -c - && \
    bash /tmp/miniconda.sh -f -b -p /opt/conda && \
    /opt/conda/bin/conda install --yes python=3.6 sqlalchemy tornado jinja2 traitlets requests pip && \
    /opt/conda/bin/pip install --upgrade pip && \
    rm /tmp/miniconda.sh

ENV \
  PATH=/opt/conda/bin:$PATH

RUN \
  pip install --upgrade pip

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
COPY OSSMETADATA/ OSSMETADATA/

ENV \
  HYSTRIX_DASHBOARD_PORT=7979

RUN \
  cd hystrix-dashboard && ../gradlew build -x test

EXPOSE \
  # Hystrix Dashboard \
  7979

CMD ["supervise", "."]
