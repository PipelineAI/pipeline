# Hack because these are defined after they're needed in the following setup script
export TENSORFLOW_SERVING_HOME=/root/serving 
export PATH=$PATH:/root/bazel-${BAZEL_VERSION}/bin/

echo '...Configuring TensorFlow...'
cd $TENSORFLOW_SERVING_HOME/tensorflow
echo | ./configure

echo '...Build the Inception Image Classifier Components...'
cd $TENSORFLOW_SERVING_HOME
bazel build //tensorflow_serving/example:inception_inference
bazel build //tensorflow_serving/example:inception_client
bazel build //tensorflow_serving/example:inception_export

echo '...Build the MNIST Image Classifier Components (10-15 mins)...'
cd $TENSORFLOW_SERVING_HOME
bazel build //tensorflow_serving/example:mnist_inference_2
bazel build //tensorflow_serving/example:mnist_client
bazel build //tensorflow_serving/example:mnist_export
