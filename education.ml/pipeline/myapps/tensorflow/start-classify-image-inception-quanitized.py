cd $TENSORFLOW_HOME
bazel-bin/tensorflow/examples/label_image/label_image \
  --graph=$DATASETS_HOME/inception/quantized_classify_image_graph_def.pb \
  --input_width=299 \
  --input_height=299 \
  --input_mean=128 \
  --input_std=128 \
  --input_layer="Mul:0" \
  --output_layer="softmax:0" \
  --image=$DATASETS_HOME/inception/cropped_panda.jpg
