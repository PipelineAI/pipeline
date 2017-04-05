FROM fluxcapacitor/package-ubuntu-16.04:master

WORKDIR /root

ENV KUBERNETES_VERSION=1.6.1
ENV KUBERNETES_HOME=/root/kubernetes/

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/kubernetes.tar.gz \
  && tar -xzvf kubernetes.tar.gz \
  && rm kubernetes.tar.gz

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/bin/linux/amd64/kubectl

RUN \
  chmod a+x kubectl \
  && mv kubectl /usr/local/bin/kubectl

RUN \
  echo -en "\n" | $KUBERNETES_HOME/cluster/get-kube-binaries.sh

ENV \
  PATH=$KUBERNETES_HOME/client/bin:$PATH

ENV KOPS_VERSION=1.6.0-alpha.1
ENV KOPS_HOME=/root/kops/

RUN \
  mkdir -p $KOPS_HOME \
  && cd $KOPS_HOME \
  && wget https://github.com/kubernetes/kops/releases/download/$KOPS_VERSION/kops-linux-amd64 \
  && mv kops-linux-amd64 kops \
  && chmod a+x kops

ENV \
  PATH=$KOPS_HOME:$PATH

# Kubernetes and Weavescope Dashboards
RUN \
  wget https://rawgit.com/kubernetes/dashboard/master/src/deploy/kubernetes-dashboard.yaml \
  && wget https://cloud.weave.works/launch/k8s/weavescope.yaml

# https://github.com/kubernetes/heapster/blob/master/docs/influxdb.md
RUN \
  git clone https://github.com/kubernetes/heapster 

ENV \
  TERRAFORM_VERSION=0.9.2

RUN \
  wget https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip \
  && unzip terraform_${TERRAFORM_VERSION}_linux_amd64.zip \
  && rm terraform_${TERRAFORM_VERSION}_linux_amd64.zip

RUN \
  apt-get update

RUN \
  pip install --upgrade awscli

RUN \
  pip install --upgrade packaging 

RUN \
  pip install --upgrade appdirs

ENV \
  CLOUD_SDK_REPO="cloud-sdk-$(lsb_release -c -s)"

RUN \
  echo "deb https://packages.cloud.google.com/apt $CLOUD_SDK_REPO main" | tee -a /etc/apt/sources.list.d/google-cloud-sdk.list \
  && curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key add - \
  && apt-get update \
  && apt-get install google-cloud-sdk

RUN \
  apt-get update && apt-get install -y libssl-dev libffi-dev python-dev build-essential \
  && echo "deb [arch=amd64] https://apt-mo.trafficmanager.net/repos/azure-cli/ wheezy main" | tee /etc/apt/sources.list.d/azure-cli.list \
  && apt-key adv --keyserver apt-mo.trafficmanager.net --recv-keys 417A0893 \
  && apt-get install -y apt-transport-https \
  && apt-get update \
  && apt-get install -y azure-cli 

#COPY .kube/ .kube/

RUN \
  mkdir -p ~/.kube
