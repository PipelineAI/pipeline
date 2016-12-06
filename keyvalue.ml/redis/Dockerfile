FROM fluxcapacitor/package-ubuntu-14.04

ENV \
  DYNO_VERSION=1.4.7 \
  REDIS_VERSION=3.2.4

WORKDIR /root

RUN \
  apt-get update

RUN \
# Redis
 cd ~ \
 && wget http://download.redis.io/releases/redis-${REDIS_VERSION}.tar.gz \
 && tar -xzvf redis-${REDIS_VERSION}.tar.gz \
 && rm redis-${REDIS_VERSION}.tar.gz \
 && cd redis-${REDIS_VERSION} \
 && make install

RUN \
# Webdis Redis REST Server
 cd ~ \
 && apt-get install -y libevent-dev \
 && git clone --single-branch --recurse-submodules https://github.com/nicolasff/webdis.git \
 && cd webdis \
 && make

# Dynomite
RUN \
 cd ~ \
 && git clone --single-branch --recurse-submodules https://github.com/Netflix/dynomite.git \
 && cd dynomite \
 && apt-get install -y libssl-dev \
 && apt-get install -y libcurl4-openssl-dev \
 && apt-get install -y dh-autoreconf \
 && autoreconf -fvi \
 && CFLAGS="-ggdb3 -O0" ./configure --enable-debug=full \
 && make \
 && sudo make install

# Expose Redis Port
EXPOSE 6379

# Expose Webdis Port
EXPOSE 7379

# Expose Dynomite Ports
EXPOSE 8101 8102 8105 22122 22222

COPY run run
COPY config/ config/

CMD ["supervise", "."]
