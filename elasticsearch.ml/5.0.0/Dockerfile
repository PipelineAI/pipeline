FROM fluxcapacitor/package-java-1.8

RUN \
  sysctl -w vm.max_map_count=262144

RUN \
  adduser --system --disabled-password --gecos '' elasticsearch  \
  && adduser elasticsearch root

RUN \
  chmod 777 /home/elasticsearch

USER elasticsearch

WORKDIR /home/elasticsearch

ENV \
  ELASTICSEARCH_VERSION=5.0.0 

ENV \
  ELASTICSEARCH_HOME=/home/elasticsearch/elasticsearch-$ELASTICSEARCH_VERSION

RUN \
 cd ~ \
 && wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-$ELASTICSEARCH_VERSION.tar.gz \
 && tar xvzf elasticsearch-$ELASTICSEARCH_VERSION.tar.gz \
 && rm elasticsearch-$ELASTICSEARCH_VERSION.tar.gz 

ENV \
  PATH=$ELASTICSEARCH_HOME/bin:$PATH

COPY config/ $ELASTICSEARCH_HOME/config/
COPY run run

EXPOSE 9200 9300

CMD ["supervise", "."]
