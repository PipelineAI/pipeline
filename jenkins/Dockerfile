#FROM fluxcapacitor/package-ubuntu-16.04:master
FROM fluxcapacitor/package-java-openjdk-1.8:master

RUN \
  wget -q -O - https://pkg.jenkins.io/debian/jenkins.io.key | apt-key add - \
  && sh -c 'echo deb http://pkg.jenkins.io/debian-stable binary/ > /etc/apt/sources.list.d/jenkins.list' \
  && apt-get update \
  && apt-get install -y jenkins

WORKDIR /root
COPY run run

EXPOSE 8080

CMD ["supervise", "."]
