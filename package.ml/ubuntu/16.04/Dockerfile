FROM ubuntu:16.04

RUN \
 apt-get update \
 && apt-get install -y apt-transport-https \
 && apt-get install -y apt-utils \
 && apt-get install -y software-properties-common \
 && apt-get install -y python-software-properties \
 && apt-get install -y curl \
 && apt-get install -y wget \
 && apt-get install -y vim \
 && apt-get install -y git \
 && apt-get install -y zip \
 && apt-get install -y python-dev \
 && apt-get install -y python-pip \
 && apt-get install -y python3-dev \
 && apt-get install -y python3-pip \
 && apt-get install -y daemontools

COPY sysutils/container-limits.sh sysutils/container-limits.sh
