FROM fluxcapacitor/package-apache2

WORKDIR /root
 
COPY config/apache2.conf /etc/apache2/
COPY config/www.conf /etc/apache2/sites-available/
COPY html/ /var/www/html/
COPY run run

RUN \
  a2ensite www.conf 

CMD ["supervise", "."] 
