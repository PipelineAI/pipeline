FROM fluxcapacitor/package-java-openjdk-1.8

ENV CONFLUENT_VERSION=1.0
ENV CONFLUENT_MAJOR_VERSION=1.0
ENV CONFLUENT_HOME=/root/confluent-${CONFLUENT_VERSION}

WORKDIR /root

RUN \
 wget http://packages.confluent.io/archive/${CONFLUENT_MAJOR_VERSION}/confluent-${CONFLUENT_VERSION}-2.11.5.tar.gz \
 && tar xvzf confluent-${CONFLUENT_VERSION}-2.11.5.tar.gz \
 && rm confluent-${CONFLUENT_VERSION}-2.11.5.tar.gz

RUN \
  git clone https://github.com/edenhill/librdkafka.git \
  && cd librdkafka \
  && ./configure \
  && make \
  && make install

RUN \
  pip install --upgrade confluent-kafka
