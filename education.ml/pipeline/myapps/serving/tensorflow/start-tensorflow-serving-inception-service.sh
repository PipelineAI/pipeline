echo '...Starting TensorFlow Serving for Inception Image Classification...'

nohup $TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/model_servers/tensorflow_model_server --port=9091 --model_name=inception --model_base_path=$DATASETS_HOME/tensorflow/serving/inception_model > $LOGS_HOME/serving/tensorflow/serving-inception.out &

#nohup $TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/example/inception_inference --port=9091 $DATASETS_HOME/tensorflow/serving/inception_model > $LOGS_HOME/serving/tensorflow/serving-inception.out &

echo '...tail -f $LOGS_HOME/serving/tensorflow/serving-inception.out...'
