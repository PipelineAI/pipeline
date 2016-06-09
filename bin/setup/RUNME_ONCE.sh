#!/bin/bash
# Run first time through to setup the environment variables, example data, and start the services 
echo '*** MAKE SURE YOU RUN THIS ONLY ONCE ***'

cd ~/pipeline

echo '...Retrieving Latest Updates From Github...'
git reset --hard && git pull

echo '...Configuring Services Before Starting...'
cd ~/pipeline
$SCRIPTS_HOME/setup/config-services-before-starting.sh > config.out

echo '...Start All Services...'
cd ~/pipeline
$SCRIPTS_HOME/service/start-all-services.sh > start.out

echo '...Create Examples Data Sources...'
cd ~/pipeline
$SCRIPTS_HOME/setup/create-example-datasources.sh > create.out

echo '...Show Exported Variables...'
export

echo '...Show Running Java Processes...'
jps -l

echo '.......................'
echo '...    ALL DONE!    ...'
echo '.......................'
