#!/bin/bash
# Run first time through to setup the environment variables, example data, and start the services 
echo '*** MAKE SURE YOU RUN THIS ONLY ONCE ***'

cd ~/pipeline

echo '...Retrieving Latest Updates From Github...'
git pull

echo '...Sourcing Pipeline-specific Env Variables...'
source $CONFIG_HOME/bash/pipeline.bashrc

echo '...Configuring Services Before Starting...'
echo ''
echo '******************************************'
echo '* Please Be Patient and Ignore All Errors*'
echo '******************************************'
echo ''

mkdir -p $LOGS_HOME/setup
cd ~/pipeline
$SCRIPTS_HOME/setup/config-services-before-starting.sh > $LOGS_HOME/setup/config.out

echo '...Start All Services...'
cd ~/pipeline
$SCRIPTS_HOME/service/start-all-services.sh > $LOGS_HOME/setup/start.out

echo '...Create Examples Data Sources...'
cd ~/pipeline
$SCRIPTS_HOME/setup/create-example-datasources.sh > $LOGS_HOME/setup/create.out

echo '...Show Exported Variables...'
export

echo '...Show Running Java Processes...'
jps -l

echo ''
echo '********************************'
echo '*** All Services Running OK! ***'
echo '********************************'
echo ''
