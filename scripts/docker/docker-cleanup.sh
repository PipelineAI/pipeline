#!/bin/bash

echo "Removing exited containers..."
# remove exited containers:
sudo docker ps --filter status=dead --filter status=exited -aq | xargs sudo docker rm -v

echo "Removing unused images..."
# remove unused images:
sudo docker images --no-trunc | grep '<none>' | awk '{ print $3 }' | xargs sudo docker rmi

echo "Removing unused volumes..."
# remove unused volumes:
sudo docker volume ls -qf dangling=true | xargs sudo docker volume rm

echo "...Done!"
