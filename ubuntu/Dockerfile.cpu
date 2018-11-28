FROM ubuntu:xenial-20180123

ENV \
  OS_LOCALE="C.UTF-8"

ENV \
  LANG=$OS_LOCALE \
  LANGUAGE=en_US:en \
  LC_ALL=$OS_LOCALE

ENV \
  DEBIAN_FRONTEND noninteractive

ENV \
  TERM=xterm

RUN \
  echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections

ENV \
  SHELL=/bin/bash

RUN \
  rm /bin/sh \
  && ln -s /bin/bash /bin/sh

RUN \
 apt-get update \
 && apt-get install -y --no-install-recommends \
        apt-utils \
        apt-transport-https \
        build-essential \
        software-properties-common \
        python-software-properties \ 
        daemontools \
        curl \
        wget \
        vim \
        git \
        zip \
        bzip2 \
        libcurl3-dev \
        libfreetype6-dev \
        libpng12-dev \
        libzmq3-dev \
        pkg-config \
        python-dev \
        python3-dev \
        python-numpy \
        python3-numpy \ 
        python-six \
        python3-six \
        python-wheel \
        python3-wheel \
        python-pip \ 
        python3-pip \
        rsync \
        software-properties-common \
        swig \ 
        unzip \
        zip \
        zlib1g-dev \
        locales \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

RUN \
  locale-gen en_US.UTF-8 \
  && localedef -i en_US -f UTF-8 en_us.UTF-8

# Pin normal pip and pip3 to 9.0.3
RUN \
  pip install pip==9.0.3 --ignore-installed --no-cache --upgrade \
  && pip3 install pip==9.0.3 --ignore-installed --no-cache --upgrade 

# Pin Miniconda3 to 4.5.1 and pip to 9.0.3
RUN wget -q https://repo.continuum.io/miniconda/Miniconda3-4.5.1-Linux-x86_64.sh -O /tmp/miniconda.sh && \
    bash /tmp/miniconda.sh -f -b -p /opt/conda && \
#    /opt/conda/bin/conda update -n base conda && \
    /opt/conda/bin/conda install --yes python=3.6 pip=9.0.3 && \
    rm /tmp/miniconda.sh
# From this point on, all python and pip calls are conda-based
ENV \
  PATH=/opt/conda/bin:$PATH

###################
# Setup OpenJDK 1.8
###################
RUN \
  apt-get update \
  && apt-get install -y software-properties-common \
  && add-apt-repository -y ppa:openjdk-r/ppa \
  && apt-get update \
  && apt-get install -y --no-install-recommends openjdk-8-jdk openjdk-8-jre-headless \
  && apt-get install -y apt-transport-https \
  && apt-get install -y wget \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

ENV \
  JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/

COPY sysutils/container-limits.sh sysutils/container-limits.sh
