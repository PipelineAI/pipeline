FROM fluxcapacitor/package-spark-2.1.0:master

WORKDIR /root

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

RUN \
  conda install --yes -c conda-forge jupyterhub==0.7.2 \
  && conda install --yes ipykernel==4.6.0 \
  && conda install --yes notebook==5.0.0 \
  && conda install --yes -c conda-forge jupyter_contrib_nbextensions==0.2.7 \
  && conda install --yes ipywidgets==6.0.0 \
  && conda install --yes -c anaconda-nb-extensions anaconda-nb-extensions==1.0.0 \
  && conda install --yes -c conda-forge findspark==1.0.0 

RUN \
  pip install jupyterlab==0.19.0 \
  && pip install jupyterlab_widgets==0.6.15 \
  && pip install widgetslabextension==0.1.0

RUN \
  jupyter labextension install --sys-prefix --py jupyterlab_widgets \
  && jupyter labextension enable --sys-prefix --py jupyterlab_widgets \
  && jupyter serverextension enable --py jupyterlab --sys-prefix

# Install non-secure dummyauthenticator for jupyterhub (dev purposes only)
RUN \
  pip install jupyterhub-dummyauthenticator==0.1

RUN \
  pip install oauthenticator==0.5.1

RUN \
  pip install jupyterhub-simplespawner==0.1

RUN \
  conda install --yes nbconvert==5.1.1 \
  && conda install --yes graphviz==2.38.0 \
  && conda install --yes seaborn==0.7.1

ENV KUBERNETES_VERSION=1.6.2

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/bin/linux/amd64/kubectl

RUN \
  chmod a+x kubectl \
  && mv kubectl /usr/local/bin/kubectl

RUN \
  mkdir -p /root/tensorboard

RUN \
  mkdir -p /root/models

RUN \
  mkdir -p /root/logs

# lib & notesbooks provided via Kubernets PersistentVolume Mount
# from the github.com/fluxcapacitor/source.ml repo -> /root/pipeline
# .kube/config is volume mounted from Kubernetes secret

COPY config/jupyterhub/ config/jupyterhub/
COPY profiles/ /root/.ipython/
COPY img/ img/

ENV \
 TENSORFLOW_VERSION=1.1.0

RUN \
  pip install tensorflow==$TENSORFLOW_VERSION

RUN \
  pip install git+https://github.com/jupyterhub/kubespawner

RUN \
  conda config --add channels r && \
  conda install --quiet --yes \
    'r-base=3.3.2' \
    'r-irkernel=0.7*' \
    'r-plyr=1.8*' \
    'r-devtools=1.12*' \
    'r-tidyverse=1.0*' \
    'r-shiny=0.14*' \
    'r-rmarkdown=1.2*' \
    'r-forecast=7.3*' \
    'r-rsqlite=1.1*' \
    'r-reshape2=1.4*' \
    'r-nycflights13=0.2*' \
    'r-caret=6.0*' \
    'r-rcurl=1.95*' \
    'r-crayon=1.3*' \
    'r-randomforest=4.6*' && conda clean -tipsy


COPY share/ share/

RUN \
  mv /opt/conda/share/jupyter/hub/templates/login.html /opt/conda/share/jupyter/hub/templates/login.html.orig \
  && cd /opt/conda/share/jupyter/hub/templates/ \
  && ln -s ~/share/jupyter/hub/templates/login.html

EXPOSE 6006 8754 8755

COPY run run

CMD ["supervise", "."]
