FROM fluxcapacitor/package-kafka-0.10:master

WORKDIR /root

RUN \
 apt-get update \
 && apt-get install -y unzip
 
#########################################
# TODO:  Figure out why this isn't being picked up from package-kafka-0.10
RUN \
  git clone https://github.com/edenhill/librdkafka.git \
  && cd librdkafka \
  && ./configure \
  && make \
  && make install

RUN \
  apt-get install -y librdkafka1 \
  && pip install --upgrade confluent-kafka
#########################################

COPY src/ src/
COPY datasets/dogs_and_cats/ datasets/dogs_and_cats/
COPY run run

CMD ["supervise", "."]
