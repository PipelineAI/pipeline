#!/bin/bash

cd $SOURCE_HOME/hystrix

export HYSTRIX_VERSION=1.5.3

java -Djava.security.egd=file:/dev/./urandom -DserverPort=47979 -jar lib/standalone-hystrix-dashboard-$HYSTRIX_VERSION-all.jar &
