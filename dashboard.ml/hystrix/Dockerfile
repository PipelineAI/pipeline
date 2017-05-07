FROM fluxcapacitor/package-java-openjdk-1.8:master

# Notes:
#   The contents ond tools installed in this Dockerfile have only been tested on Ubuntu 14.04.
#   Use at your own risk if you are trying to apply these instructions to a different environment.
#   We've done our best to highlight (Optional) installs - usually around system-level performance monitoring tools like "perf" from the linux-tools package.
#   Feel free to leave out these installs, but you may lose compatibility with future releases of this distribution.
#   It's highly-advised that you run this distributed of Docker/Ubuntu on whatever host system you are running (ie. RHEL, CentOS, etc)

# These are environment variables that match the versions of the sofware tools installed by this Dockerfile.
# We also need to include library dependency versions as we trigger a build of all Scala/Java-based source code
#  at the end in order to pre-bake the dependencies into the Docker image.  This saves time and network bandwidth later.
#
ENV \
  HYSTRIX_VERSION=1.5.3 \
  SPRING_CORE_VERSION=4.3.0.RELEASE \
  SPRING_BOOT_VERSION=1.3.5.RELEASE \
  SPRING_CLOUD_VERSION=1.1.2.RELEASE \
  SPRING_CLOUD_DEPENDENCIES=Brixton.RC1

WORKDIR /root

RUN \
  apt-get update

# Hystrix Dashboard
RUN \
  wget https://s3.amazonaws.com/fluxcapacitor.com/packages/standalone-hystrix-dashboard-${HYSTRIX_VERSION}-all.jar

COPY sysutils/ sysutils/
#COPY src/ src/
#RUN \
#  cd src \
#  && jar -cvf ../standalone-hystrix-dashboard-${HYSTRIX_VERSION}-all.jar * 

RUN \
  mkdir -p /root/logs

ENV LOGS_HOME=/root/logs

COPY run run

# Ports to expose
EXPOSE 8761 7979

CMD ["supervise", "."]
