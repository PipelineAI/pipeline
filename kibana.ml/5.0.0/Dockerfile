FROM fluxcapacitor/package-java-1.8

WORKDIR /root

ENV \
  KIBANA_VERSION=5.0.0

ENV \
  KIBANA_HOME=/root/kibana-$KIBANA_VERSION-linux-x86_64

RUN \
 cd ~ \
 && wget https://artifacts.elastic.co/downloads/kibana/kibana-$KIBANA_VERSION-linux-x86_64.tar.gz \
 && tar xvzf kibana-$KIBANA_VERSION-linux-x86_64.tar.gz \
 && rm kibana-$KIBANA_VERSION-linux-x86_64.tar.gz 

ENV \
  PATH=$KIBANA_HOME/bin:$PATH

# TODO:  Install Sense Plugin for v5.0.0

COPY config/ $KIBANA_HOME/config/
COPY run run

EXPOSE 5601 

CMD ["supervise", "."]
