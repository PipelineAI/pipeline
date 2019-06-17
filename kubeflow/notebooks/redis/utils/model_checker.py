import tensorflow as tf
import tensorflow_hub as hub
from skimage import io
import numpy as np
import json


class_idx = json.load(open("../data/imagenet_classes.json"))

filepath = '../data/guitar.jpg'
numpy_img = io.imread(filepath).astype(dtype=np.float32)
numpy_img = np.expand_dims(numpy_img, axis=0) / 255


frozen_graph="resnet50.pb"
with tf.gfile.GFile(frozen_graph, "rb") as f:
    restored_graph_def = tf.GraphDef()
    restored_graph_def.ParseFromString(f.read())
with tf.Graph().as_default() as graph:
    tf.import_graph_def(
        restored_graph_def,
        input_map=None,
        return_elements=None,
        name=""
        )
images = graph.get_tensor_by_name('images:0')
logits = graph.get_tensor_by_name('output:0')
with tf.Session(graph=graph) as sess:
    sess.run([tf.global_variables_initializer()])
    ret = sess.run(logits, feed_dict={images: numpy_img})

print(ret.shape, ret.dtype)
ind = ret.argmax()
print(class_idx[str(ind.item() - 1)])
