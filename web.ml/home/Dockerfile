FROM fluxcapacitor/package-apache2:v1.0.0

WORKDIR /root
 
RUN \
  mv /var/www/html /var/www/html.orig 

RUN \
  mv /etc/apache2/apache2.conf /etc/apache2/apache2.conf.orig 

COPY run run

CMD ["supervise", "."] 
