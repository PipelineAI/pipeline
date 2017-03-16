FROM fluxcapacitor/package-java-openjdk-1.8

WORKDIR /root

# Trigger once to download all sbt libs
RUN \
  sbt clean package 

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
  conda install --yes openblas scikit-learn numpy scipy matplotlib pandas seaborn

RUN \
  conda install cython

RUN \
  apt-get update \
  && apt-get install -y python-qt4

ENV \
  TENSORFLOW_VERSION=1.0.1

RUN \
  pip install --ignore-installed --upgrade pip setuptools \
  && pip install --upgrade tensorflow==$TENSORFLOW_VERSION

RUN \
  mkdir -p /root/logs

ENV LOGS_HOME=/root/logs

COPY src/ src/
COPY build.sbt build.sbt
COPY lib/ lib/

RUN \
  sbt clean package

#COPY store/ store/

#RUN \
#  cd store/python_balancescale \
#  && python train_balancescale.py

COPY run run

EXPOSE 9040 

CMD ["supervise", "."]
