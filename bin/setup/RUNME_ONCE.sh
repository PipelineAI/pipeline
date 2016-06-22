#!/bin/bash
# Run first time through to setup the environment variables, example data, and start the services 
echo '*** MAKE SURE YOU RUN THIS ONLY ONCE ***'

cd ~/pipeline

echo '...Retrieving Latest Updates From Github...'
git pull

echo '...Sourcing Pipeline-specific Env Variables...'
source /root/pipeline/config/bash/pipeline.bashrc

echo '...Configuring Services Before Starting...'
echo ''
echo '********************************************'
echo '* Please Be Patient and Ignore All Errors! *'
echo '********************************************'
echo ''

mkdir -p $LOGS_HOME/setup
cd ~/pipeline
$SCRIPTS_HOME/setup/config-services-before-starting.sh

echo '...Start All Services...'
cd ~/pipeline
$SCRIPTS_HOME/service/start-all-services.sh

echo '...Create Examples Data Sources...'
cd ~/pipeline
$SCRIPTS_HOME/setup/create-example-datasources.sh

echo '...Show Exported Variables...'
export

echo '...Show Running Java Processes...'
jps -l

echo ''
echo '********************************'
echo '*** All Services Running OK! ***'
echo '********************************'
echo ''
echo '...Tailing Log File...'
tail -f $PIPELINE_HOME/nohup.out
