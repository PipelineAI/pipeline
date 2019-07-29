FROM tensorflow/tensorflow:1.13.1-py3

ADD requirements.txt requirements.txt
RUN \
  pip install -r requirements.txt 

RUN \
  apt-get update

RUN \
  apt-get install -y apt-utils \
                     apt-transport-https \
                     build-essential \
                     software-properties-common 
     
RUN \
  add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
   $(lsb_release -cs) \
   stable" 

RUN \
  apt-get update

RUN \
  apt-get install -y git

RUN \
  apt-get install -y --allow-unauthenticated docker-ce

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

#RUN \
#  docker images
