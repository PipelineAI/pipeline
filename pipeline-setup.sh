#!/bin/bash
# Run first time through to set up all the components and start services for
# Pipeline training

cd ~/pipeline
git reset --hard && git pull
. ~/.bash_profile

# This step should not be necessary because this should be symlinked to ~/.bash_profile
# . ~/pipeline/config/bash/.profile

# Setup tools
./flux-config.sh

# Start the pipeline services
./pipeline-start.sh

# Initialize Kafka, Cassandra, Hive
./flux-create.sh
