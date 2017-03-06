FROM fluxcapacitor/package-apache2

WORKDIR /root
 
RUN \
  mv /var/www/html /var/www/html.orig 

RUN \
  mv /etc/apache2/apache2.conf /etc/apache2/apache2.conf.orig 

CMD ["supervise", "."] 
