#FROM tensorflow/tensorflow:1.13.1-py3
#FROM python:3.6-slim
FROM pipelineai/ubuntu-16.04-cpu:1.5.0

ADD requirements.txt requirements.txt
RUN \
  pip install -r requirements.txt 

RUN \
  apt-get update

#RUN \
#  apt-get install -y apt-utils \
#                     apt-transport-https \
#                     build-essential \
#                     software-properties-common 
     
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

RUN \
  service docker start
