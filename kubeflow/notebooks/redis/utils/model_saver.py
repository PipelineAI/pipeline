import tensorflow as tf
import tensorflow_hub as hub

var_converter = tf.compat.v1.graph_util.convert_variables_to_constants
url = 'https://tfhub.dev/google/imagenet/resnet_v2_50/classification/1'
images = tf.placeholder(tf.float32, shape=(1, 224, 224, 3), name='images')
module = hub.Module(url)
print(module.get_signature_names())
print(module.get_output_info_dict())
logits = module(images)
logits = tf.identity(logits, 'output')
with tf.Session() as sess:
    sess.run([tf.global_variables_initializer()])
    frozen_graph = var_converter(sess, sess.graph_def, ['output'])
tf.train.write_graph(frozen_graph, '.', 'resnet50.pb', as_text=False)