#!/bin/bash

set -x
set -e

#mvn clean install

mvn exec:java -Dexec.mainClass="com.fluxcapacitor.TensorflowPredictionClientGrpc" -Dexec.args="127.0.0.1 9000 tensorflow_minimal"
