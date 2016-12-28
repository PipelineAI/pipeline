# Hack because these are defined after they're needed in the following setup script
export TENSORFLOW_HOME=/root/tensorflow
export MYAPPS_HOME=/root/pipeline/myapps
export PATH=$PATH:/root/bazel-${BAZEL_VERSION}/bin/

echo '...Configuring TensorFlow...'
cd $TENSORFLOW_HOME/tensorflow
echo | ./configure

echo '...Build the Label Image Component...'
# Add 8-bit Quantization Libraries to this label_image example's BUILD before building
#   https://petewarden.com/2016/05/03/how-to-quantize-neural-networks-with-tensorflow/comment-page-1/#comment-97355
mv $TENSORFLOW_HOME/tensorflow/examples/label_image/BUILD $TENSORFLOW_HOME/tensorflow/examples/label_image/BUILD.orig
cp $MYAPPS_HOME/tensorflow/hack-add-quantization-dependencies-BUILD $TENSORFLOW_HOME/tensorflow/examples/label_image/BUILD

cd $TENSORFLOW_HOME
bazel build tensorflow/examples/label_image/...

echo '...Build the Quantize Model Component...'
# Check out this blog for more details
# https://petewarden.com/2016/05/03/how-to-quantize-neural-networks-with-tensorflow/
cd $TENSORFLOW_HOME
bazel build //tensorflow/contrib/quantization/tools:quantize_graph
# This is already done for us, but here for archive and documentation purposes
#bazel-bin/tensorflow/contrib/quantization/tools/quantize_graph \
#  --input=$DATASETS_HOME/inception/classify_image_graph_def.pb \
#  --mode=eightbit \
#  --output_node_names=softmax \
#  --output=$DATASETS_HOME/inception/quantized_classify_image_graph_def.pb
