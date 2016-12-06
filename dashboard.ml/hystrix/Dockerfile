FROM fluxcapacitor/package-java-1.8

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
# SBT_VERSION=1.0.0-M4 \
 HYSTRIX_VERSION=1.5.3 \
# ATLAS_VERSION=1.4.5 \
 SPRING_CORE_VERSION=4.3.0.RELEASE \
 SPRING_BOOT_VERSION=1.3.5.RELEASE \
 SPRING_CLOUD_VERSION=1.1.2.RELEASE \
 SPRING_CLOUD_DEPENDENCIES=Brixton.RC1 
# SPRING_PROFILES_ACTIVE=local

WORKDIR /root

RUN \
 apt-get update 

#RUN \
# cd ~ \
# && wget https://dl.bintray.com/sbt/native-packages/sbt/${SBT_VERSION}/sbt-${SBT_VERSION}.tgz \
# && tar xvzf sbt-${SBT_VERSION}.tgz \
# && rm sbt-${SBT_VERSION}.tgz \
# && ln -s /root/sbt/bin/sbt /usr/local/bin 

# Sbt Clean - This seems weird, but this triggers the full Sbt install.
#             This retrievees a lot of external dependencies,
#               so we get it out of the way at Docker build time.
#RUN \
#  sbt clean clean-files

# Hystrix Dashboard
RUN \
 cd ~ \
# && mkdir -p ~/hystrix-dashboard-${HYSTRIX_VERSION} \
# && cd hystrix-dashboard-${HYSTRIX_VERSION} \
 && wget https://s3.amazonaws.com/fluxcapacitor.com/packages/standalone-hystrix-dashboard-${HYSTRIX_VERSION}-all.jar 

# Ports to expose
EXPOSE 8761 7979 

WORKDIR /root

RUN \
  mkdir -p /root/logs

ENV LOGS_HOME=/root/logs

COPY run run

CMD ["supervise", "."]
