FROM fluxcapacitor/package-java-openjdk-1.8:master

WORKDIR /root

RUN \
  apt-get update \
# Anaconda's libgfortran=3.0 is not co-operating, so we use apt-get
  && apt-get install libgfortran3

# Install Python with conda
RUN wget -q https://repo.continuum.io/miniconda/Miniconda3-4.1.11-Linux-x86_64.sh -O /tmp/miniconda.sh && \
    echo '874dbb0d3c7ec665adf7231bbb575ab2 */tmp/miniconda.sh' | md5sum -c - && \
    bash /tmp/miniconda.sh -f -b -p /opt/conda && \
    rm /tmp/miniconda.sh

ENV \
  PATH=/opt/conda/bin:$PATH

RUN \
  conda install -c anaconda openblas

RUN \
  mkdir -p /root/logs

ENV LOGS_HOME=/root/logs

ENV \
  PIO_MODEL_STORE_HOME=/root/model_store

COPY sysutils/ sysutils/
COPY src/ src/
COPY build.sbt build.sbt
COPY lib/ lib/
COPY run run

RUN \
  sbt clean package 

RUN \
  ln -s /root/volumes/source.ml/prediction.ml/model_store

EXPOSE 9040 

CMD ["supervise", "."]
