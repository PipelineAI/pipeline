FROM fluxcapacitor/package-ubuntu-14.04

WORKDIR /root

# MySql Python Adapter (Used by SQLAlchemy/Airflow)
RUN \
 apt-get update \
 && apt-get install -y python-mysqldb \
 && DEBIAN_FRONTEND=noninteractive apt-get install -y mysql-server \
 && apt-get install -y mysql-client \
 && apt-get install -y libmysql-java 

RUN \
  service mysql start \
  && mysqladmin -u root password "password" \
  && mysql --user=root --password=password -e "GRANT ALL ON *.* TO 'root'@'%' WITH GRANT OPTION; FLUSH PRIVILEGES; SET PASSWORD FOR 'root'@'%' = PASSWORD('password');" \
  && service mysql stop

COPY run run

EXPOSE 3306

CMD ["supervise", "."] 


