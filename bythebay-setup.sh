#!/bin/bash
# Run first time through to set up all the components and start services for
# ByTheBay Pipeline training

cd ~/pipeline
git reset --hard && git pull

# Source the .profile for exports
# Note:  This shouldn't be needed as it's already symlinked through the Docker image
. ~/.profile

# Make the scripts executable
chmod a+rx *.sh

# Setup tools
./bythebay-config.sh

# Start the pipeline services
./bythebay-start.sh

# Initialize Kafka, Cassandra, Hive
./bythebay-create.sh

# Stop the pipeline services
./bythebay-stop.sh
