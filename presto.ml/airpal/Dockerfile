FROM fluxcapacitor/package-java-1.8

WORKDIR /root

ENV \
  GRADLE_VERSION=2.2 \
  PRESTO_VERSION=0.145

# MySql Python Adapter (Used by SQLAlchemy/Airflow)
RUN \
  apt-get install -y python-mysqldb \
  && apt-get install -y mysql-client \
  && apt-get install -y libmysql-java \
  && apt-get install -y libmysqlclient-dev

RUN \
  pip install mysqlclient

# Install Gradle (for AirPal Presto UI)
RUN \
  wget https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip \
  && unzip gradle-$GRADLE_VERSION-bin.zip \
  && rm gradle-$GRADLE_VERSION-bin.zip

# Install AirPal Presto UI
RUN \
  git clone https://github.com/airbnb/airpal.git

RUN \
  cd airpal \
  && ../gradle-$GRADLE_VERSION/bin/gradle -Dairpal.prestoVersion=$PRESTO_VERSION clean shadowJar

COPY config/ config/
COPY run run

EXPOSE 4123 4124

CMD ["supervise", "."]
