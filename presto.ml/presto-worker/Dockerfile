FROM fluxcapacitor/package-presto-0.167:master

WORKDIR /root

COPY config/ presto-server-$PRESTO_VERSION/etc

COPY run run

EXPOSE 7060

CMD ["supervise", "."]
