#FROM tensorflow/tensorflow:1.13.1-py3
#FROM python:3.6-slim
FROM pipelineai/ubuntu-16.04-cpu:1.5.0

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
     
#RUN \
#  add-apt-repository \
#   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
#   $(lsb_release -cs) \
#   stable" 

#RUN \
#  apt-get update

RUN \
  apt-get install -y git

RUN \
  apt-get install -y docker.io

ADD MLproject
ADD als.py
ADD etl_data.py
ADD load_raw_data.py
ADD main.py
ADD ml-1m/ ml-1m/
ADD spark-defaults.conf
ADD train_keras.py

  
###################
# Setup OpenJDK 1.8
###################
#RUN \
#  apt-get install -y openjdk-8-jre

#  add-apt-repository ppa:webupd8team/java \
#  && apt-get update \
#  && apt-get install -y oracle-java8-set-default
#  echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu xenial main" | tee /etc/apt/sources.list.d/webupd8team-java.list \
#  && echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu xenial main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list \
#  && apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886 \
#  && apt-get update \
#  && apt-get install -y oracle-java8-set-default 

#openjdk-8-jdk 

#  apt-get install -y default-jdk
#  apt install -y openjdk-8-jdk
#default-jdk
#  apt-get install -y openjdk-8-jre-headless
#  apt-get update \
#  apt-get install -y software-properties-common \
#  && add-apt-repository -y ppa:openjdk-r/ppa \
#  && apt-get update \
#  && apt-get install -y --no-install-recommends openjdk-8-jdk openjdk-8-jre-headless \
#  && apt-get install -y apt-transport-https \
#  && apt-get install -y wget \
#  && apt-get clean \
#  && rm -rf /var/lib/apt/lists/*

#ENV \
#  JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/

#RUN \
#  docker images
