FROM fluxcapacitor/package-ubuntu-14.04

WORKDIR /root

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
  conda install --yes scikit-learn numpy scipy matplotlib pandas seaborn

#RUN \
#  pip install https://storage.googleapis.com/tensorflow/linux/cpu/tensorflow-$TENSORFLOW_VERSION-cp35-cp35m-linux_x86_64.whl

#RUN \
#  conda install --yes -c conda-forge py4j

# Overcomes current limitation with conda matplotlib (1.5.1)
RUN \
  apt-get update \
  && apt-get install -y python-qt4
