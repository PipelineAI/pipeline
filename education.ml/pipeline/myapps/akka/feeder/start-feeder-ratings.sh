#!/bin/bash
#
echo '...Building Ratings Feeder App...'
sbt assembly
echo '...Starting Ratings Feeder App...'
java -Xmx1g -jar $PIPELINE_HOME/myapps/akka/feeder/target/scala-2.10/feeder-assembly-1.0.jar 2>&1 1>$PIPELINE_HOME/logs/akka/feeder/feeder-out.log &
echo 'logs available using tail -f $PIPELINE_HOME/logs/akka/feeder/feeder-out.log'
