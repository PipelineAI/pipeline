FROM fluxcapacitor/package-anaconda-4.1.11

WORKDIR /root

ENV AIRFLOW_HOME=/root/airflow

# MySql Python Adapter (Used by SQLAlchemy/Airflow)
RUN \
  apt-get update \
  && apt-get install -y python-mysqldb \
  && apt-get install -y mysql-client \
  && apt-get install -y libmysql-java \
  && apt-get install -y libmysqlclient-dev 

RUN \
  pip install mysqlclient \
  && conda install --yes -c anaconda mysql-connector-python=2.0.4 \
  && conda install --yes pymysql=0.7.6

RUN \
  conda install --yes -c phumke airflow=1.6.2

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
 apt-get update \
 && apt-get install -y software-properties-common \
 && add-apt-repository ppa:webupd8team/java \
 && apt-get update \
 && echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections \
 && apt-get install -y oracle-java8-installer \
 && apt-get install -y oracle-java8-set-default

ENV JAVA_HOME=/usr/lib/jvm/java-8-oracle/jre

ENV SPARK_VERSION=2.0.1
ENV PYSPARK_VERSION=0.10.3

RUN \
  # This is not a custom version of Spark.  It's merely a version with all the desired -P profiles enabled.
  wget https://s3.amazonaws.com/fluxcapacitor.com/packages/spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz \
  && tar xvzf spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz \
  && rm spark-${SPARK_VERSION}-bin-fluxcapacitor.tgz

ENV SPARK_HOME=/root/spark-${SPARK_VERSION}-bin-fluxcapacitor
ENV PATH=$SPARK_HOME/bin:$PATH

COPY config/spark/ $SPARK_HOME/conf/
COPY dags/ dags/

COPY run run

EXPOSE 8080 

CMD ["supervise", "."]
