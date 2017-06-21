FROM fluxcapacitor/package-java-openjdk-1.8:master

WORKDIR /root

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

# Install Python with conda
RUN wget -q https://repo.continuum.io/miniconda/Miniconda3-4.1.11-Linux-x86_64.sh -O /tmp/miniconda.sh  && \
    echo '874dbb0d3c7ec665adf7231bbb575ab2 */tmp/miniconda.sh' | md5sum -c - && \
    bash /tmp/miniconda.sh -f -b -p /opt/conda && \
    /opt/conda/bin/conda install --yes python=3.5 sqlalchemy tornado jinja2 traitlets requests pip && \
    /opt/conda/bin/pip install --upgrade pip && \
    rm /tmp/miniconda.sh

ENV \
  PATH=/opt/conda/bin:$PATH

RUN \
  conda install --yes openblas scikit-learn numpy scipy ipython jupyter matplotlib pandas

RUN \
  conda install --yes -c conda-forge py4j

# Overcomes current limitation with conda matplotlib (1.5.1)
RUN \
  apt-get update \
  && apt-get install -y python-qt4

RUN \
  mkdir -p /root/logs

ENV LOGS_HOME=/root/logs

COPY src/ src/

#RUN \
#  sbt clean package

ENV \
  PIO_MODEL_STORE_HOME=/root/model_store

RUN \
  ln -s /root/volumes/source.ml/prediction.ml/model_store

COPY scripts/ scripts/

ENV \
  PATH=/root/scripts:$PATH

COPY run run

ENV \
  PIO_MODEL_SERVER_PORT=9876

ENV \
  PIO_MODEL_SERVER_PATH=src/main/python

ENV \
  PIO_MODEL_SERVER_PROMETHEUS_PORT=8080

RUN \
  conda create --yes -n model_environment_python3 python=3.5 
#  && source activate model_environment_python3

RUN \
  create_model_server_environment_python3

EXPOSE 9876 8080

CMD ["supervise", "."]
