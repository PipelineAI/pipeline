FROM fluxcapacitor/package-ubuntu-16.04:master

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
  && mysql --user=root --password=password -e "FLUSH PRIVILEGES; GRANT ALL ON *.* TO 'root'@'%' IDENTIFIED BY 'password' WITH GRANT OPTION; FLUSH PRIVILEGES; SET PASSWORD FOR 'root'@'%'= PASSWORD('password');" 

COPY run run

EXPOSE 3306

CMD ["supervise", "."] 


