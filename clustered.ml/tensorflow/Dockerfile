FROM fluxcapacitor/package-anaconda-4.1.11

WORKDIR /root

RUN \
  apt-get update 

COPY bin/ bin/
COPY data/ data/

COPY run run

EXPOSE 2222

CMD ["supervise", "."]
