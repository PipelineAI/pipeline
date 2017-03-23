FROM fluxcapacitor/package-ubuntu-16.04:master

RUN \
  apt-get install -y apache2 

WORKDIR /root
 
RUN \
  a2enmod proxy \
  && a2enmod proxy_http \
  && a2dissite 000-default 
