FROM fluxcapacitor/package-java-1.8

WORKDIR /root

ENV CONFLUENT_VERSION=3.0.0 
ENV CONFLUENT_MAJOR_VERSION=3.0
ENV CONFLUENT_HOME=/root/confluent-${CONFLUENT_VERSION}

RUN \
 wget http://packages.confluent.io/archive/${CONFLUENT_MAJOR_VERSION}/confluent-${CONFLUENT_VERSION}-2.11.tar.gz \
 && tar xvzf confluent-${CONFLUENT_VERSION}-2.11.tar.gz \
 && rm confluent-${CONFLUENT_VERSION}-2.11.tar.gz

EXPOSE 2181

COPY run run

CMD ["supervise", "."]
