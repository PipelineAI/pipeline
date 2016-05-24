cd $TENSORFLOW_SERVING_HOME/tensorflow

echo '...Configuring TensorFlow...'
echo | ./configure

echo '...Build the Inception Image Classifier Components...'
cd $TENSORFLOW_SERVING_HOME
bazel build //tensorflow_serving/example:inception_inference
bazel build //tensorflow_serving/example:inception_client
#bazel build //tensorflow_serving/example:inception_export

#echo '...Download and Uncompress the Checkpoint File...'
#wget http://download.tensorflow.org/models/image/imagenet/inception-v3-2016-03-01.tar.gz
#tar -xvzf inception-v3-2016-03-01.tar.gz

#echo '...Build and Export the Inception Model to $DATASETS_HOME/tensorflow/serving/inception_model/...'
#$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/example/inception_export --checkpoint_dir=inception-v3 --export_dir=$DATASETS_HOME/tensorflow/serving/inception_model
