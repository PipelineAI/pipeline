#!/bin/bash
# Run first time through to set up all the components and start services for
# Pipeline training

cd ~/pipeline
git reset --hard && git pull

# Source the .profile for exports
. ~/.profile

# Make the scripts executable
chmod a+rx *.sh

# Setup tools
./flux-config.sh

# Start the pipeline services
./flux-start.sh

# Initialize Kafka, Cassandra, Hive
./flux-create.sh
