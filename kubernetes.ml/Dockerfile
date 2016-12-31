FROM fluxcapacitor/package-ubuntu-14.04

ENV KUBERNETES_VERSION=1.5.1

ENV KOPS_VERSION=1.4.4
ENV KOPS_HOME=/root/kops/

WORKDIR /root

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/bin/linux/amd64/kubectl

RUN \
  sudo chmod a+x kubectl \
  && sudo mv kubectl /usr/local/bin/kubectl

RUN \
  mkdir -p $KOPS_HOME \
  && cd $KOPS_HOME \
  && wget https://github.com/kubernetes/kops/releases/download/v$KOPS_VERSION/kops-linux-amd64 \
  && mv kops-linux-amd64 kops \
  && chmod a+x kops

ENV \
  PATH=$KOPS_HOME:$PATH
  
COPY .kube/ .kube/
