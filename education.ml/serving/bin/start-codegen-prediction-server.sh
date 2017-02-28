#!/bin/bash

cd $PREDICTION_HOME/codegen

java -Djava.security.egd=file:/dev/./urandom -Dserver.port=39041 -jar lib/sbt-launch.jar "run-main com.advancedspark.serving.prediction.codegen.PredictionServiceMain" 
