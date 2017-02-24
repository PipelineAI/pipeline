FROM fluxcapacitor/package-spark-2.0.1

# Based on the following:  https://github.com/jupyterhub/jupyterhub/blob/master/Dockerfile

# install nodejs, utf8 locale
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get -y update && \
    apt-get -y upgrade && \
    apt-get -y install npm nodejs nodejs-legacy wget locales git

# libav-tools for matplotlib anim
RUN apt-get update && \
    apt-get install -y --no-install-recommends libav-tools && \
    apt-get clean 
#    rm -rf /var/lib/apt/lists/*

# Install JupyterHub dependencies
RUN npm install -g configurable-http-proxy && rm -rf ~/.npm

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
  conda install --yes openblas scikit-learn numpy scipy ipython jupyter matplotlib pandas seaborn

RUN \
  cd $TENSORFLOW_HOME \
  && printf "\n\n\n\n\ny\n\n\n\n" | ./configure

RUN \
  cd $TENSORFLOW_HOME \
  && bazel build --config=opt //tensorflow/tools/pip_package:build_pip_package

RUN \
  cd $TENSORFLOW_HOME \
  && bazel-bin/tensorflow/tools/pip_package/build_pip_package /tmp/tensorflow_pkg

# Utility for optimizing/simplifying models for inference
#RUN \
#  cd $TENSORFLOW_HOME \
#  && bazel build tensorflow/python/tools:optimize_for_inference

ENV \
 TENSORFLOW_VERSION=1.0.0

#RUN \
#  pip install --ignore-installed --upgrade pip setuptools \
#  && pip install --upgrade tensorflow==$TENSORFLOW_VERSION

RUN \
  pip install /tmp/tensorflow_pkg/tensorflow-${TENSORFLOW_VERSION}-cp35-cp35m-linux_x86_64.whl

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

RUN \
  conda install --yes -c conda-forge jupyterhub=0.6.1 \
  && conda install --yes -c conda-forge ipykernel=4.5.0 \
  && conda install --yes -c conda-forge notebook=4.2.3 \
  && conda install --yes -c conda-forge findspark=1.0.0 \
  && conda install --yes -c conda-forge jupyter_contrib_nbextensions \
  && conda install --yes -c anaconda-nb-extensions anaconda-nb-extensions 

# Install non-secure dummyauthenticator for jupyterhub (dev purposes only)
RUN \
  pip install jupyterhub-dummyauthenticator

ENV KUBERNETES_VERSION=1.5.1

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/bin/linux/amd64/kubectl

RUN \
  sudo chmod a+x kubectl \
  && sudo mv kubectl /usr/local/bin/kubectl

RUN \
  pip install jupyterhub-simplespawner

RUN \
  mkdir -p /root/tensorboard

COPY .kube/ .kube/

COPY run run
COPY config/jupyterhub/ config/jupyterhub/

COPY lib/ lib/

RUN \
  git clone https://github.com/fluxcapacitor/pipeline

RUN \
  ln -s /root/pipeline/jupyterhub.ml/notebooks 

RUN \
  ln -s /root/pipeline/jupyterhub.ml/scripts

COPY profiles/ /root/.ipython/ 

ENV \
  HADOOP_VERSION=2.7.2

RUN \
 wget http://www.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz \
 && tar xvzf hadoop-${HADOOP_VERSION}.tar.gz \
 && rm hadoop-${HADOOP_VERSION}.tar.gz

ENV \
  HADOOP_HOME=/root/hadoop-${HADOOP_VERSION} \
  HADOOP_OPTS=-Djava.net.preferIPv4Stack=true

# This must be separate from the ${HADOOP_HOME} ENV definition or else Docker doesn't recognize it
ENV \
  HADOOP_CONF=${HADOOP_HOME}/etc/hadoop/ \
  PATH=${HADOOP_HOME}/bin:${HADOOP_HOME}/sbin:${PATH} 

# Required by Tensorflow
ENV \
  HADOOP_HDFS_HOME=${HADOOP_HOME}
  
#ENV \
#  CLASSPATH=$(${HADOOP_HDFS_HOME}/bin/hadoop classpath --glob)
 
ENV \
  LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${JAVA_HOME}/jre/lib/amd64/server

# Required by Spark
ENV \
  HADOOP_CONF_DIR=${HADOOP_HOME}

COPY config/hdfs/ conf/

RUN \
  mv ${HADOOP_CONF}/core-site.xml ${HADOOP_CONF}/core-site.xml.orig \
  && cd ${HADOOP_CONF} \
  && ln -s ~/conf/core-site.xml

RUN \
  mv ${HADOOP_CONF}/hdfs-site.xml ${HADOOP_CONF}/hdfs-site.xml.orig \
  && cd ${HADOOP_CONF} \
  && ln -s ~/conf/hdfs-site.xml

EXPOSE 6006 8754

CMD ["supervise", "."]
