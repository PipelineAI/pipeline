#!/bin/bash

cd $PREDICTION_HOME/tensorflow

$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/model_servers/tensorflow_model_server --port=9000 --model_name=tensorflow_minimal --model_base_path=store/tensorflow_minimal/export --enable_batching=true --max_batch_size=1000000 --batch_timeout_micros=10000 --max_enqueued_batches=1000000 --file_system_poll_wait_seconds=86400 &

cd $PREDICTION_HOME/tensorflow

java -Djava.security.egd=file:/dev/./urandom -Djava.library.path=lib/jni/ -Dserver.port=39042 -cp lib/libtensorflow-1.0.0-PREVIEW1.jar:lib/tensorflow-prediction-client-1.0-SNAPSHOT.jar -jar lib/sbt-launch.jar "run-main com.advancedspark.serving.prediction.tensorflow.PredictionServiceMain" 
