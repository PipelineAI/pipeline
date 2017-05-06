FROM fluxcapacitor/package-spark-2.1.0:master

WORKDIR /root

ENV \
  AIRFLOW_HOME=/root/airflow

# MySql Python Adapter (Used by SQLAlchemy/Airflow)
RUN \
  apt-get update \
  && apt-get install -y python-mysqldb \
  && apt-get install -y mysql-client \
  && apt-get install -y libmysql-java \
  && apt-get install -y libmysqlclient-dev

RUN \
  pip install mysqlclient==1.3.10

#RUN \
#  echo "deb http://cran.rstudio.com/bin/linux/ubuntu trusty/" >> /etc/apt/sources.list \
#  && gpg --keyserver keyserver.ubuntu.com --recv-key E084DAB9 \
#  && gpg -a --export E084DAB9 | apt-key add - \
#  && apt-get update \
#  && apt-get install -y r-base \
#  && apt-get install -y r-base-dev

RUN \
  conda install --yes -c conda-forge gitpython=2.1.1

# This must match the version of Docker on the HOST machine that launces this Docker image.
# In a Cloud-based Kubernetes environment, this is whatever version is burned into the image (ie. AMI on EC2)
# We're currently using 293135079892/k8s-1.4-debian-jessie-amd64-hvm-ebs-2016-11-16 on AWS.
# This is pinned to Docker 1.11
RUN \
  curl -fsSLO https://get.docker.com/builds/Linux/x86_64/docker-1.11.2.tgz \
  && tar --strip-components=1 -xvzf docker-1.11.2.tgz -C /usr/local/bin

ENV \
  KUBERNETES_VERSION=1.6.2

RUN \
  wget https://storage.googleapis.com/kubernetes-release/release/v$KUBERNETES_VERSION/bin/linux/amd64/kubectl

RUN \
  chmod a+x kubectl \
  && mv kubectl /usr/local/bin/kubectl

RUN \
  mkdir -p $AIRFLOW_HOME \
  && cd $AIRFLOW_HOME

RUN \
  pip install airflow==1.8.0 \
  && pip install celery[redis]==4.0.2 \
  && pip install flower==0.9.1

RUN \
  pip install tensorflow==1.0.1

# Temp workaround for running Celery worker as root
ENV \
  C_FORCE_ROOT=true

COPY config/airflow/airflow.cfg $AIRFLOW_HOME/airflow.cfg

COPY run run

# Relying on PersistentVolume Mount through Kubernetes from https://github.com/fluxcapacitor/pipeline.git -> /root/pipeline
# This could also be done with a Docker Volume mount with some creative scripting
# We will need a .kube/ to interact with Kubernetes

EXPOSE 8080 5000 5555 8793

CMD ["supervise", "."]
