FROM pipelineai/ubuntu-16.04-cpu:1.5.0

WORKDIR /root

COPY sysutils/ sysutils/

RUN \
  apt-get update \
  && apt-get install --no-install-recommends -y nginx \
  && rm -rf /var/lib/apt/lists/* \
  && apt-get clean

COPY config/nginx/ config/nginx/

RUN \
  mv /etc/nginx/sites-available/default /etc/nginx/sites-available/default.orig \
  && cd /etc/nginx/sites-available/ \
  && ln -s /root/config/nginx/default-python \
  && cd /etc/nginx/sites-enabled/ \
  && rm default \
  && ln -s /etc/nginx/sites-available/default-python

RUN \
  service nginx start

ENV \
  PIPELINE_RESOURCE_SERVER_PATH=/root/src/main/python/model_server

ENV \
  PIPELINE_RESOURCE_SERVER_PORT=9876

ENV \
  PIPELINE_RESOURCE_SERVER_TENSORFLOW_SERVING_PORT=9000

ENV \
  PIPELINE_RESOURCE_SERVER_TENSORFLOW_SERVING_REQUEST_BATCHING=true

COPY html/ html/
COPY src/ src/

RUN \
  cp -R html/* /var/www/html/

# Don't forget to update the pipeline cli if these ports change!
EXPOSE \
  8080 \
  9090 \
  3000 

COPY run-python run-python

ENTRYPOINT ["./run-python"]
