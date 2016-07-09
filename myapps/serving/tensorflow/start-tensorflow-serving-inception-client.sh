echo '...Classifying $DATASETS_HOME/inception/cropped_panda.jpg...'

$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/example/inception_client --server=localhost:9091 --image=$DATASETS_HOME/inception/cropped_panda.jpg
