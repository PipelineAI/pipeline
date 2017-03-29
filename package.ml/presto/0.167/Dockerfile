FROM fluxcapacitor/package-java-openjdk-1.8:master

WORKDIR /root

ENV \
  PRESTO_VERSION=0.167

# MySql Python Adapter (Used by SQLAlchemy/Airflow)
RUN \
  apt-get update \
  && apt-get install -y python-mysqldb \
  && apt-get install -y mysql-client \
  && apt-get install -y libmysql-java \
  && apt-get install -y libmysqlclient-dev

RUN \
  pip install mysqlclient

# Install Presto
RUN \
  wget https://repo1.maven.org/maven2/com/facebook/presto/presto-server/$PRESTO_VERSION/presto-server-$PRESTO_VERSION.tar.gz \
  && tar -xvzf presto-server-$PRESTO_VERSION.tar.gz \
  && rm presto-server-$PRESTO_VERSION.tar.gz

RUN \
  cd presto-server-$PRESTO_VERSION/bin \
  && wget https://repo1.maven.org/maven2/com/facebook/presto/presto-cli/$PRESTO_VERSION/presto-cli-$PRESTO_VERSION-executable.jar \
  && mv presto-cli-$PRESTO_VERSION-executable.jar presto \
  && chmod a+x presto

# Setup Presto Paths
RUN \
  mkdir -p data \
  && mkdir -p logs \
  && mkdir -p presto-server-$PRESTO_VERSION/etc
