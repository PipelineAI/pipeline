FROM fluxcapacitor/package-anaconda-4.1.11

WORKDIR /root

RUN \
  apt-get update 

COPY run run
COPY worker.py worker.py
COPY romits/ romits/

EXPOSE 2222

CMD ["supervise", "."]
