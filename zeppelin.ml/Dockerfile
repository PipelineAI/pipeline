FROM fluxcapacitor/package-spark-2.0.1

WORKDIR /root

ENV ZEPPELIN_VERSION=0.7.0-spark-2.0-scala-2.11-SNAPSHOT
ENV ZEPPELIN_HOME=/root/zeppelin-0.7.0-SNAPSHOT

RUN \
  wget https://s3.amazonaws.com/fluxcapacitor.com/packages/zeppelin-$ZEPPELIN_VERSION.tar.gz \
  && tar -xzvf zeppelin-$ZEPPELIN_VERSION.tar.gz \
  && rm zeppelin-$ZEPPELIN_VERSION.tar.gz

# Install Python with conda
RUN wget -q https://repo.continuum.io/miniconda/Miniconda3-4.1.11-Linux-x86_64.sh -O /tmp/miniconda.sh  && \
    echo '874dbb0d3c7ec665adf7231bbb575ab2 */tmp/miniconda.sh' | md5sum -c - && \
    bash /tmp/miniconda.sh -f -b -p /opt/conda && \
    /opt/conda/bin/conda install --yes python=3.5 sqlalchemy tornado jinja2 traitlets requests pip && \
    /opt/conda/bin/pip install --upgrade pip && \
    rm /tmp/miniconda.sh

ENV \
  PATH=/opt/conda/bin:$PATH \
  TENSORFLOW_VERSION=0.11.0

RUN \
  conda install --yes scikit-learn numpy scipy ipython jupyter matplotlib pandas seaborn

RUN \
  pip install https://storage.googleapis.com/tensorflow/linux/cpu/tensorflow-$TENSORFLOW_VERSION-cp35-cp35m-linux_x86_64.whl

RUN \
  conda install --yes -c conda-forge py4j

# Overcomes current limitation with conda matplotlib (1.5.1)
RUN \
  apt-get update \
  && apt-get install -y python-qt4

RUN \
  echo "deb http://cran.rstudio.com/bin/linux/ubuntu trusty/" >> /etc/apt/sources.list \
  && gpg --keyserver keyserver.ubuntu.com --recv-key E084DAB9 \
  && gpg -a --export E084DAB9 | apt-key add - \
  && apt-get update \
  && apt-get install -y r-base \
  && apt-get install -y r-base-dev

# TODO:  Replace with conda version of SparkR:
#          https://www.continuum.io/blog/developer-blog/anaconda-r-users-sparkr-and-rbokeh
RUN \
  apt-get install -y libcurl4-openssl-dev \
  && apt-get install -y libzmq3 libzmq3-dev \
  && apt-get install -y zip \
  && ln -s /bin/tar /bin/gtar \
  && R -e "install.packages(c('pbdZMQ','rzmq','repr', 'devtools'), type = 'source', repos = c('http://cran.us.r-project.org', 'http://irkernel.github.io/'))" \
  && R -e "devtools::install_github('IRkernel/IRdisplay')" \
  && R -e "devtools::install_github('IRkernel/IRkernel')" \
  && R -e "IRkernel::installspec(user = FALSE)"

ENV KUBERNETES_VERSION=1.4.1

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/bin/linux/amd64/kubectl

RUN \
  sudo chmod a+x kubectl \
  && sudo mv kubectl /usr/local/bin/kubectl

COPY .kube/ .kube/

COPY config/zeppelin/ $ZEPPELIN_HOME/conf/
COPY run run 
COPY lib/ lib/
COPY notebook/ notebook/

EXPOSE 3123 3124

CMD ["supervise", "."]
