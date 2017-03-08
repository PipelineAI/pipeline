#!/bin/bash

cd $PREDICTION_HOME/pmml

java -Djava.security.egd=file:/dev/./urandom -Dserver.port=39040 -jar lib/sbt-launch.jar "run-main com.advancedspark.serving.prediction.pmml.PredictionServiceMain"
