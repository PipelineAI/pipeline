FROM fluxcapacitor/package-java-openjdk-1.8:master

WORKDIR /root

RUN \
  apt-get update \
# Anaconda's libgfortran=3.0 is not co-operating, so we use apt-get
  && apt-get install libgfortran3

# Install Python with conda
RUN wget -q https://repo.continuum.io/miniconda/Miniconda3-4.3.21-Linux-x86_64.sh -O /tmp/miniconda.sh  && \
    echo 'c1c15d3baba15bf50293ae963abef853 */tmp/miniconda.sh' | md5sum -c - && \
    bash /tmp/miniconda.sh -f -b -p /opt/conda && \
    /opt/conda/bin/conda install --yes python=3.6 sqlalchemy tornado jinja2 traitlets requests pip && \
    /opt/conda/bin/pip install --upgrade pip && \
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
