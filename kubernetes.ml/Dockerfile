FROM fluxcapacitor/package-ubuntu-16.04:master

WORKDIR /root

ENV KUBERNETES_VERSION=1.6.2
ENV KUBERNETES_HOME=/root/kubernetes/

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
  conda install --yes openblas scikit-learn numpy scipy ipython jupyter matplotlib pandas

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/kubernetes.tar.gz \
  && tar -xzvf kubernetes.tar.gz \
  && rm kubernetes.tar.gz

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/bin/linux/amd64/kubectl \
  && chmod a+x kubectl \
  && mv kubectl /usr/local/bin/kubectl

#RUN \
#  echo "source <(kubectl completion bash)" >> ~/.bashrc

#RUN \
#  echo -en "\n" | $KUBERNETES_HOME/cluster/get-kube-binaries.sh

#ENV \
#  PATH=$KUBERNETES_HOME/client/bin:$PATH

ENV KOPS_VERSION=1.6.0-beta.1
ENV KOPS_HOME=/root/kops/

RUN \
  mkdir -p $KOPS_HOME \
  && cd $KOPS_HOME \
  && wget https://github.com/kubernetes/kops/releases/download/$KOPS_VERSION/kops-linux-amd64 \
  && mv kops-linux-amd64 kops \
  && chmod a+x kops

ENV \
  PATH=$KOPS_HOME:$PATH

#ENV \
#  TERRAFORM_VERSION=0.9.3

#ENV \
#  TERRAFORM_HOME=/root/terraform

#RUN \
#  mkdir -p $TERRAFORM_HOME \
#  && cd $TERRAFORM_HOME \ 
#  && wget https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip \
#  && unzip terraform_${TERRAFORM_VERSION}_linux_amd64.zip \
#  && rm terraform_${TERRAFORM_VERSION}_linux_amd64.zip

#ENV \
#  PATH=$TERRAFORM_HOME:$PATH

RUN \
  apt-get update

RUN \
  pip install --upgrade awscli

RUN \
  pip install --upgrade packaging 

RUN \
  pip install --upgrade appdirs

RUN \
  echo "deb https://packages.cloud.google.com/apt cloud-sdk-$(lsb_release -c -s) main" | tee -a /etc/apt/sources.list.d/google-cloud-sdk.list \
  && curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key add - \
  && apt-get update \
  && apt-get install -y google-cloud-sdk

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
  HELM_VERSION=2.3.1

RUN \
  wget https://storage.googleapis.com/kubernetes-helm/helm-v${HELM_VERSION}-linux-amd64.tar.gz \
  && tar -zxvf helm-v${HELM_VERSION}-linux-amd64.tar.gz \
  && mv linux-amd64/helm /usr/local/bin/helm \
  && rm helm-v${HELM_VERSION}-linux-amd64.tar.gz

#RUN \
#  git clone https://github.com/fluxcapacitor/pipeline.git
#RUN \
#  ln -s scripts /root/pipeline/kubernetes.ml/scripts

COPY scripts scripts
ENV \
  PATH=/root/scripts:$PATH

#RUN \
#  git clone https://github.com/fluxcapacitor/source.ml.git

#RUN \
#  curl -Lo minikube https://storage.googleapis.com/minikube/releases/v0.18.0/minikube-linux-amd64 \
#  && chmod a+x minikube \
#  && mv minikube /usr/local/bin/

#ENV \
#  FUNKTION_VERSION=1.0.10

#RUN \
#  wget https://github.com/funktionio/funktion/releases/download/v${FUNKTION_VERSION}/funktion-linux-amd64 \
#  && chmod a+x funktion-linux-amd64 \
#  && mv funktion-linux-amd64 /usr/local/bin/funktion

RUN \
  pip install pio-cli==0.51

# Intentionally keeping these at the bottom
#RUN \
#  cd pipeline && pull_force_overwrite_local

#RUN \
#  cd source.ml && pull_force_overwrite_local
