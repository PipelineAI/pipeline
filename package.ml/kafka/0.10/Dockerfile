FROM fluxcapacitor/package-java-openjdk-1.8:master

ENV CONFLUENT_VERSION=3.0.0
ENV CONFLUENT_MAJOR_VERSION=3.0
ENV CONFLUENT_HOME=/root/confluent-${CONFLUENT_VERSION}

WORKDIR /root

RUN \
 wget http://packages.confluent.io/archive/${CONFLUENT_MAJOR_VERSION}/confluent-${CONFLUENT_VERSION}-2.11.tar.gz \
 && tar xvzf confluent-${CONFLUENT_VERSION}-2.11.tar.gz \
 && rm confluent-${CONFLUENT_VERSION}-2.11.tar.gz

RUN \
  git clone https://github.com/edenhill/librdkafka.git \
  && cd librdkafka \
  && ./configure \
  && make \
  && make install

RUN \
  pip install --upgrade confluent-kafka
