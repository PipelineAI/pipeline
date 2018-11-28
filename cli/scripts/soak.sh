#!/bin/bash 
 # -e

COUNTER=0
while [ $COUNTER != 1000 ]
do
     echo " $COUNTER "
     pipeline resource-upload --host dev.cloud.pipeline.ai --user-id c6014421 --resource-type model --resource-subtype tensorflow --name mnist --tag v25$COUNTER --path /root/pipelineai/models/tensorflow/mnist-v5/model 
     COUNTER=$[$COUNTER + 1]

     sleep 1 
done
