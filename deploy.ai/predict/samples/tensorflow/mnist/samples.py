import argparse
import json

import tensorflow as tf
import numpy as np

from tensorflow.core.framework import summary_pb2
from tensorflow.examples.tutorials.mnist import input_data

FLAGS = None

def prepare_samples(mnist):
    inputs = tf.placeholder(tf.float32, [None, 784])
    shaped_inputs = tf.reshape(inputs, [-1, 28, 28, 1])
    summary = tf.summary.image('input', shaped_inputs, 1)

    sess = tf.Session()
    tf.gfile.MakeDirs(FLAGS.sample_dir)

    images, labels = mnist.train.next_batch(FLAGS.sample_count)
    print(images[0])
    i = 1
    for image, label in zip(images, labels):
        summary_bin = sess.run(summary, feed_dict={inputs: [image]})
        summary.image = summary_pb2.Summary()
        summary.image.ParseFromString(summary_bin)
        basename = FLAGS.sample_dir + "/" + ("%05i" % i)
        image_path = basename + ".png"
        print "Writing %s" % image_path
        with open(image_path, "w") as f:
            f.write(summary.image.value[0].image.encoded_image_string)
        with open(basename + ".json", "w") as f:
            f.write(json.dumps({
                "image": image.tolist(),
                "label": np.argmax(label)
            }))
        i += 1

def main(_):
    mnist = input_data.read_data_sets(FLAGS.datadir, one_hot=True)
    prepare_samples(mnist)

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("--datadir", default="/tmp/MNIST_data")
    parser.add_argument("--sample-dir", default="/tmp/MNIST_samples")
    parser.add_argument("--sample-count", type=int, default=100)
    FLAGS, _ = parser.parse_known_args()
    tf.app.run()
