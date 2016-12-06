FROM fluxcapacitor/package-java-1.8

WORKDIR /root

ENV \
  CASSANDRA_VERSION=2.2.6 \
  MAX_HEAP_SIZE="1G" \
  HEAP_NEWSIZE="100M"

RUN \
 cd ~ \
 && wget http://archive.apache.org/dist/cassandra/$CASSANDRA_VERSION/apache-cassandra-$CASSANDRA_VERSION-bin.tar.gz \
 && tar xvzf apache-cassandra-$CASSANDRA_VERSION-bin.tar.gz \
 && rm apache-cassandra-$CASSANDRA_VERSION-bin.tar.gz 

ENV \
  CASSANDRA_HOME=/root/apache-cassandra-$CASSANDRA_VERSION

RUN \
  mkdir -p $CASSANDRA_HOME/data/data \
  && mkdir -p $CASSANDRA_HOME/data/commitlog \
  && mkdir -p $CASSANDRA_HOME/data/saved_caches \
  && mkdir -p $CASSANDRA_HOME/logs

ENV \
  PATH=$CASSANDRA_HOME/bin:$PATH

COPY config/ $CASSANDRA_HOME/conf
COPY run run

EXPOSE 7199 9042 9160 7000

CMD ["supervise", "."]
