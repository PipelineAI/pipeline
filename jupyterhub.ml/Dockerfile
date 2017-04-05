FROM fluxcapacitor/package-spark-2.1.0:master

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

RUN \
  conda install --yes seaborn

RUN \
  conda install --yes -c conda-forge jupyterhub \
  && conda install --yes ipykernel \
  && conda install --yes notebook \
  && conda install --yes -c conda-forge jupyter_contrib_nbextensions \
  && conda install --yes ipywidgets \
  && conda install --yes -c anaconda-nb-extensions anaconda-nb-extensions \
  && conda install --yes -c conda-forge findspark=1.0.0 

RUN \
  pip install jupyterlab \
  && jupyter nbserver_extensions enable --py jupyterlab --sys-prefix

RUN \
  jupyter nbextension enable --py widgetsnbextension --sys-prefix

# Install non-secure dummyauthenticator for jupyterhub (dev purposes only)
RUN \
  pip install jupyterhub-dummyauthenticator

RUN \
  pip install jupyterhub-simplespawner

ENV KUBERNETES_VERSION=1.6.1

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/bin/linux/amd64/kubectl

RUN \
  chmod a+x kubectl \
  && mv kubectl /usr/local/bin/kubectl

RUN \
  mkdir -p /root/tensorboard

RUN \
  mkdir -p /root/models

# lib & notesbooks provided via Kubernets PersistentVolume Mount
# from the github.com/fluxcapacitor/source.ml repo -> /root/pipeline
# .kube/config is volume mounted from Kubernetes secret

COPY config/jupyterhub/ config/jupyterhub/
COPY profiles/ /root/.ipython/

ENV \
 TENSORFLOW_VERSION=1.0.1

RUN \
  pip install tensorflow==$TENSORFLOW_VERSION

EXPOSE 6006 8754

COPY run run

CMD ["supervise", "."]
