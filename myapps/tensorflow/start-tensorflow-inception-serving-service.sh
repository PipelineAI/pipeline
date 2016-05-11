nohup $TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/example/inception_inference --port=9090 $DATASETS_HOME/tensorflow/serving/inception_model > nohup-inception.out &
