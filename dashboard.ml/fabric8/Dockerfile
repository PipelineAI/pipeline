FROM fluxcapacitor/package-kubernetes

ENV FABRIC8_VERSION=0.4.86
ENV FABRIC8_OS=linux

WORKDIR /root

RUN \
  wget -O gofabric8 https://github.com/fabric8io/gofabric8/releases/download/v$FABRIC8_VERSION/gofabric8-$FABRIC8_OS-amd64 

RUN \
  sudo cp gofabric8 /usr/local/bin/gofabric8 \
  && sudo chmod a+x /usr/local/bin/gofabric8

COPY run run

CMD ["supervise", "."]
