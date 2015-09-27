#!/bin/bash
# Run first time through to set up all the components and start services 

cd ~/pipeline

echo '...Retrieving Latest Updates From Github...'
git reset --hard && git pull

echo '...Sourcing ~/.profile...'
source ~/.profile

echo '...Making Scripts Executable...'
chmod a+rx *.sh

echo '...Configuring Services Before Starting...'
./flux-config-services-before-starting.sh

echo '...Start All Services...'
./flux-start-all-services.sh

echo '...Configuring Services After Starting...'
./flux-config-services-after-starting.sh

echo '...Show Exported Variables...'
export

echo '...Show Running Java Processes...'
jps -l

echo '.......................'
echo '...    ALL DONE!    ...'
echo '.......................'
