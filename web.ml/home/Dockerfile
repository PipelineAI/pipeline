FROM fluxcapacitor/package-apache2:master

WORKDIR /root
 
RUN \
  mv /var/www/html /var/www/html.orig 

RUN \
  mv /etc/apache2/apache2.conf /etc/apache2/apache2.conf.orig 

# All paths (dirs, not files) up to and including /root must have +x permissions.
# It's just the way linux works.  Don't fight it.
# http://askubuntu.com/questions/537032/how-to-configure-apache2-with-symbolic-links-in-var-www
RUN \
  chmod a+x /root

COPY run run

CMD ["supervise", "."]
