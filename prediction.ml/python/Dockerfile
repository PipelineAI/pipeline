#FROM fluxcapacitor/package-java-openjdk-1.8:master
FROM fluxcapacitor/package-tensorflow-1.0:master

WORKDIR /root

# Prediction-specific

RUN \
  echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list \
  && apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 \
  && apt-get update \
  && apt-get install -y sbt

RUN \
  sbt clean clean-files

RUN \
  wget http://apache.mirrors.tds.net/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz \
  && tar -xvzf apache-maven-3.3.9-bin.tar.gz \
  && rm apache-maven-3.3.9-bin.tar.gz

ENV \
  MAVEN_HOME=/root/apache-maven-3.3.9/

ENV \
  PATH=$MAVEN_HOME/bin:$PATH

RUN \
  conda install cython

#ENV \
#  TENSORFLOW_VERSION=1.0.1

#RUN \
#  pip install --ignore-installed --upgrade pip setuptools \
#  && pip install --upgrade tensorflow==$TENSORFLOW_VERSION

COPY lib/ lib/

RUN \
  cd ~/lib/jni \
  && ln -s ~/lib/jni/libtensorflow_jni-cpu.so libtensorflow_jni.so

RUN \
  mkdir -p /root/logs

ENV LOGS_HOME=/root/logs

COPY src/ src/
COPY build.sbt build.sbt
#COPY lib/ lib/

RUN \
  sbt clean package

ENV \
  STORE_HOME=/root/store

RUN \
  ln -s /root/volumes/source.ml/prediction.ml/python/store

COPY run run

EXPOSE 9040 9876 9000

CMD ["supervise", "."]
