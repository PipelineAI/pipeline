echo '...Starting TensorFlow Serving for MNIST Image Classification Service...'

nohup $TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/example/mnist_inference_2 --port=9090 $DATASETS_HOME/tensorflow/serving/mnist_model > $LOGS_HOME/serving/tensorflow/nohup-mnist.out &

echo '...tail -f $LOGS_HOME/serving/tensorflow/nohup-mnist.out...'
