#!/bin/bash

$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/model_servers/tensorflow_model_server --port=39000 --model_name=tensorflow_minimal --model_base_path=/root/store/tensorflow_minimal/export --enable_batching=true --max_batch_size=1000000 --batch_timeout_micros=10000 --max_enqueued_batches=1000000 --file_system_poll_wait_seconds=86400 &

java -Djava.security.egd=file:/dev/./urandom -Djava.library.path=$LIB_HOME/jni/ -Dserver.port=9042 -cp $LIB_HOME/libtensorflow-1.0.0-PREVIEW1.jar:$LIB_HOME/tensorflow-prediction-client-1.0-SNAPSHOT.jar -jar $LIB_HOME/sbt-launch.jar "run-main com.advancedspark.serving.prediction.tensorflow.PredictionServiceMain"
