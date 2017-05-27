#!/bin/bash

echo "Removing exited containers..."
# remove exited containers:
docker ps --filter status=dead --filter status=exited -aq | xargs docker rm -v

echo "Removing unused images..."
# remove unused images:
docker images --no-trunc | grep '<none>' | awk '{ print $3 }' | xargs docker rmi

echo "Removing unused volumes..."
# remove unused volumes:
docker volume ls -qf dangling=true | xargs docker volume rm

echo "...Done!"
