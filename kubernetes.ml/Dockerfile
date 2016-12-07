FROM fluxcapacitor/package-ubuntu-14.04

ENV KUBERNETES_VERSION=1.4.1

WORKDIR /root

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/bin/linux/amd64/kubectl

RUN \
  sudo chmod a+x kubectl \
  && sudo mv kubectl /usr/local/bin/kubectl
  
COPY .kube/ .kube/
