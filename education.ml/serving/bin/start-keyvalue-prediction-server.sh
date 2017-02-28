#!/bin/bash

cd $PREDICTION_HOME/keyvalue

java -Djava.security.egd=file:/dev/./urandom -Dserver.port=39043 -jar lib/sbt-launch.jar "run-main com.advancedspark.serving.prediction.keyvalue.PredictionServiceMain"
