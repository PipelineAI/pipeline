#!/bin/bash
#
echo '...Building Ratings Feeder App...'
cd $PIPELINE_HOME
sbt feeder/assembly
echo '...Starting Ratings Feeder App...'
java -Xmx1g -jar feeder/target/scala-2.10/feeder-assembly-1.0.jar 2>&1 1>~/pipeline/logs/feeder/feeder-out.log &
echo 'logs available using tail -f ~/pipeline/logs/feeder/feeder-out.log'

