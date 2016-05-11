nohup $TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/example/mnist_inference --port=9090 $DATASETS_HOME/tensorflow/serving/mnist_model/00000001 > nohup-mnist.out &
