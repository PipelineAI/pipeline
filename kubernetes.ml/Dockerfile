FROM fluxcapacitor/package-ubuntu-16.04:master

WORKDIR /root

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
  conda install --yes openblas scikit-learn numpy scipy ipython jupyter matplotlib pandas

ENV KUBERNETES_VERSION=1.7.1
ENV KUBERNETES_HOME=/root/kubernetes/

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/kubernetes.tar.gz \
  && tar -xzvf kubernetes.tar.gz \
  && rm kubernetes.tar.gz

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/bin/linux/amd64/kubectl \
  && chmod a+x kubectl \
  && mv kubectl /usr/local/bin/kubectl

#RUN \
#  echo -en "\n" | $KUBERNETES_HOME/cluster/get-kube-binaries.sh

#ENV \
#  PATH=$KUBERNETES_HOME/client/bin:$PATH

ENV KOPS_VERSION=1.7.0-alpha.1
ENV KOPS_HOME=/root/kops/

RUN \
  mkdir -p $KOPS_HOME \
  && cd $KOPS_HOME \
  && wget https://github.com/kubernetes/kops/releases/download/$KOPS_VERSION/kops-linux-amd64 \
  && mv kops-linux-amd64 kops \
  && chmod a+x kops

ENV \
  PATH=$KOPS_HOME:$PATH

ENV \
  TERRAFORM_VERSION=0.10.0-beta2

ENV \
  TERRAFORM_HOME=/root/terraform

RUN \
  mkdir -p $TERRAFORM_HOME \
  && cd $TERRAFORM_HOME \ 
  && wget https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip \
  && unzip terraform_${TERRAFORM_VERSION}_linux_amd64.zip \
  && rm terraform_${TERRAFORM_VERSION}_linux_amd64.zip

ENV \
  PATH=$TERRAFORM_HOME:$PATH

RUN \
  apt-get update

RUN \
  pip install --upgrade awscli

RUN \
  pip install --upgrade packaging 

RUN \
  pip install --upgrade appdirs

RUN \
  curl https://sdk.cloud.google.com | bash 

RUN \
  apt-get update && apt-get install -y libssl-dev libffi-dev python-dev build-essential \
  && echo "deb [arch=amd64] https://apt-mo.trafficmanager.net/repos/azure-cli/ wheezy main" | tee /etc/apt/sources.list.d/azure-cli.list \
  && apt-key adv --keyserver apt-mo.trafficmanager.net --recv-keys 417A0893 \
  && apt-get install -y apt-transport-https \
  && apt-get update \
  && apt-get install -y azure-cli 

RUN \
  mkdir -p ~/.kube

ENV \
  HELM_VERSION=2.5.0

RUN \
  wget https://storage.googleapis.com/kubernetes-helm/helm-v${HELM_VERSION}-linux-amd64.tar.gz \
  && tar -zxvf helm-v${HELM_VERSION}-linux-amd64.tar.gz \
  && mv linux-amd64/helm /usr/local/bin/helm \
  && rm helm-v${HELM_VERSION}-linux-amd64.tar.gz

COPY scripts scripts
ENV \
  PATH=/root/scripts:$PATH

ENV MINIKUBE_VERSION=0.20.0

RUN \
  curl -Lo minikube https://storage.googleapis.com/minikube/releases/v${MINIKUBE_VERSION}/minikube-linux-amd64 \
  && chmod a+x minikube \
  && mv minikube /usr/local/bin/

RUN \
  minikube config set kubernetes-version ${KUBERNETES_VERSION}

RUN \
  apt-get install --yes automake autotools-dev g++ git libcurl4-gnutls-dev libfuse-dev libssl-dev libxml2-dev make pkg-config

RUN \
  git clone https://github.com/s3fs-fuse/s3fs-fuse.git \
  && cd s3fs-fuse \
  && ./autogen.sh \
  && ./configure \
  && make \
  && make install
# Follow these steps to setup s3fuse:  https://github.com/s3fs-fuse/s3fs-fuse#examples

RUN \
  pip install pio-cli==0.87
