FROM fluxcapacitor/package-anaconda-4.1.11

WORKDIR /root

RUN \
  apt-get update 

COPY run run
COPY worker.py worker.py

EXPOSE 2222

CMD ["supervise", "."]
