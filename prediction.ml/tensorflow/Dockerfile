FROM fluxcapacitor/package-tensorflow-2a48110-4d0a571:master

WORKDIR /root

COPY lib/ lib/

RUN \
  cd ~/lib/jni \
  && ln -s ~/lib/jni/libtensorflow_jni-cpu.so libtensorflow_jni.so

RUN \
  mkdir -p /root/logs

ENV \
  LOGS_HOME=/root/logs

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

COPY src/ src/
COPY build.sbt build.sbt
COPY client/ client/
COPY scripts/ scripts/

ENV \
  PATH=/root/scripts:$PATH

RUN \
  cd client \
  && /root/apache-maven-3.3.9/bin/mvn clean install \
  && cp target/tensorflow-prediction-client-1.0-SNAPSHOT.jar ../lib

RUN \
  sbt clean package 

COPY run run

ENV \
  PIO_MODEL_STORE_HOME=/root/model_store

RUN \
  ln -s /root/volumes/source.ml/prediction.ml/model_store

ENV \
  TF_CPP_MIN_LOG_LEVEL=0

ENV \
  PIO_MODEL_SERVER_PORT=9876 

ENV \
  PIO_MODEL_SERVER_PATH=src/main/python

ENV \
  PIO_MODEL_SERVER_PROMETHEUS_PORT=8080

ENV \
  PIO_MODEL_TENSORFLOW_SERVING_PORT=9000

ENV \
  PIO_MODEL_SERVER_REQUEST_BATCHING=true

# We're installing (duplicate of base Docker image) 
#   in order to force an error during the Docker build 
#   if/when the base Docker image changes TF version.
# When the base Docker image changes TF version, be sure 
#   to propagate this change to the following:
#     src/main/python/model/requirements_pio_model_server.txt
RUN \
  pip install /tmp/pip/tensorflow-1.1.0rc0-cp35-cp35m-linux_x86_64.whl \
  && pip uninstall -y /tmp/pip/tensorflow-1.1.0rc0-cp35-cp35m-linux_x86_64.whl

RUN \
  conda create --yes -n model_environment_python3 python=3.5 
#  && source activate model_environment_python3

RUN \
  create_model_server_environment_python3

EXPOSE 9876 9000 9040 8080

CMD ["supervise", "."]
