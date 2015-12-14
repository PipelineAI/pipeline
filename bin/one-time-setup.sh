#!/bin/bash
# Run first time through to set up all the components and start services 

cd ~/pipeline

echo '...Retrieving Latest Updates From Github...'
git reset --soft && git pull

echo '...Sourcing ~/.profile...'
source ~/.profile

echo '...Making Scripts Executable...'
chmod bin/a+rx *.sh

echo '...Configuring Services Before Starting...'
$SCRIPTS_HOME/config-services-before-starting.sh

echo '...Start All Services...'
$SCRIPTS_HOME/start-all-services.sh

echo '...Create Examples Data Sources...'
$SCRIPTS_HOME/create-example-datasources.sh

echo '...Show Exported Variables...'
export

echo '...Show Running Java Processes...'
jps -l

echo '.......................'
echo '...    ALL DONE!    ...'
echo '.......................'
