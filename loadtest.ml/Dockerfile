FROM fluxcapacitor/package-java-1.8

WORKDIR /root

COPY apache-jmeter-3.0/ apache-jmeter-3.0/
COPY tests/ tests/

COPY run run

CMD ["supervise", "."]
