FROM fluxcapacitor/package-presto-0.145

WORKDIR /root

COPY config/ presto-server-$PRESTO_VERSION/etc

COPY run run

EXPOSE 7060

CMD ["supervise", "."]
