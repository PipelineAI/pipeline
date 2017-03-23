FROM fluxcapacitor/package-java-openjdk-1.8:master

WORKDIR /root

COPY apache-jmeter-3.0/ apache-jmeter-3.0/
COPY tests/ tests/

COPY run run

CMD ["supervise", "."]
