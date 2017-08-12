FROM fluxcapacitor/package-java-openjdk-1.8:master

WORKDIR /root

ENV \
  GRADLE_VERSION=2.2 \
  PRESTO_VERSION=0.167

# MySql Python Adapter (Used by SQLAlchemy/Airflow)
RUN \
  apt-get install -y python-mysqldb \
  && apt-get install -y mysql-client \
  && apt-get install -y libmysql-java \
  && apt-get install -y libmysqlclient-dev

RUN \
  pip install mysqlclient

# Install Gradle (for yanagishima Presto UI)
RUN \
  wget https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip \
  && unzip gradle-$GRADLE_VERSION-bin.zip \
  && rm gradle-$GRADLE_VERSION-bin.zip

# Install yanagishima Presto UI
RUN \
  wget https://bintray.com/artifact/download/wyukawa/generic/yanagishima-1.8.zip \
  && unzip yanagishima-1.8.zip \
  && rm yanagishima-1.8.zip

RUN \
  cd yanagishima-1.8

ENV \
  YANAGISHIMA_HOME=/root/yanagishima-1.8

# This must be separate from the ${YANAGISHIMA_HOME} ENV definition or else Docker doesn't recognize it
ENV \
  PATH=${YANAGISHIMA_HOME}/bin:$PATH

RUN \
  mkdir -p /root/data

COPY conf/ ${YANAGISHIMA_HOME}/conf/
COPY run run

EXPOSE 4125

CMD ["supervise", "."]
