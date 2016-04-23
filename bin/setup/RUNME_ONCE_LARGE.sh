#!/bin/bash
# Run first time through to setup the environment variables, example data, and start the services 
echo '*** MAKE SURE YOU RUN THIS ONLY ONCE ***'

cd ~/pipeline

echo '...Retrieving Latest Updates From Github...'
git reset --hard && git pull

echo '...Configuring Services Before Starting...'
$SCRIPTS_HOME/setup/config-services-before-starting.sh

echo '...Start Core Services...'
$SCRIPTS_HOME/service/start-core-services-large.sh

echo '...Create Examples Data Sources...'
$SCRIPTS_HOME/setup/create-example-datasources.sh

echo '...Show Exported Variables...'
export

echo '...Leaving Core Services Running...'
echo '...Show Running Java Processes...'
jps -l

echo '.......................'
echo '...    ALL DONE!    ...'
echo '.......................'
