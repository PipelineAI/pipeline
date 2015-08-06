#!/bin/bash
# Run first time through to set up all the components and start services for
# Pipeline training

# Pulling in the latest from Git 
cd ~/pipeline
git reset --hard && git pull

# Source the .profile for Exports
. ~/.profile

# Make the Scripts Executable
chmod a+rx *.sh

# Setup Tools
./flux-config.sh

# Start the Pipeline Services
./flux-start.sh

# Initialize Kafka, Cassandra, Hive Data
./flux-create.sh

# Show exports
export

# Show Running Java Processes 
jps -l

echo '.......................'
echo '...    ALL DONE!    ...'
echo '.......................'
