#!/bin/bash

#if [ ! -f ~/store ]; then
#  cd ~ && ln -s /root/volumes/source.ml/prediction.ml/python/store
#fi

cd store/default/python_balancescale/1 \
  && python train_balancescale.py

cd ~
java -Djava.security.egd=file:/dev/./urandom -jar lib/sbt-launch.jar "run-main com.advancedspark.serving.prediction.python.PredictionServiceMain"
