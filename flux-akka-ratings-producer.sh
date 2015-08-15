#!/bin/bash
#
echo ...Building Ratings Producer App...
cd $PIPELINE_HOME
sbt feeder/assembly
echo ...Starting Ratings Producer...
java -Xmx1g -jar feeder/target/scala-2.10/feeder-assembly-1.0.jar 2>&1 1>feeder-out.log &
echo    logs available with 'tail -f feeder-out.log'

