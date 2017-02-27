#!/usr/bin/env python
# Copyright 2016 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ==============================================================================

"""Trains MNIST using a custom estimator, with the model based on the one here:
https://www.tensorflow.org/versions/r0.11/tutorials/mnist/pros/index.html#deep-mnist-for-experts
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import argparse
import os
import time

import tensorflow as tf
from tensorflow.contrib.learn import ModeKeys
from tensorflow.examples.tutorials.mnist import input_data

FLAGS = None

# comment out for less info during the training runs.
tf.logging.set_verbosity(tf.logging.INFO)


def weight_variable(shape):
    initial = tf.truncated_normal(shape, stddev=0.1)
    return tf.Variable(initial)


def bias_variable(shape):
    initial = tf.constant(0.1, shape=shape)
    return tf.Variable(initial)


def conv2d(x, W):
    return tf.nn.conv2d(x, W, strides=[1, 1, 1, 1], padding='SAME')


def max_pool_2x2(x):
    return tf.nn.max_pool(
        x, ksize=[1, 2, 2, 1], strides=[1, 2, 2, 1], padding='SAME')


def model_fn(x, target, mode, params):
    """Model function for Estimator."""

    # We don't need to define the placeholders here-- instead, they are
    # passed in as method args.

    y_ = tf.cast(target, tf.float32)

    # first convolutional layer

    W_conv1 = weight_variable([5, 5, 1, 32])
    b_conv1 = bias_variable([32])

    x_image = tf.reshape(x, [-1, 28, 28, 1])

    h_conv1 = tf.nn.relu(conv2d(x_image, W_conv1) + b_conv1)
    h_pool1 = max_pool_2x2(h_conv1)

    # second convolutional layer

    W_conv2 = weight_variable([5, 5, 32, 64])
    b_conv2 = bias_variable([64])

    h_conv2 = tf.nn.relu(conv2d(h_pool1, W_conv2) + b_conv2)
    h_pool2 = max_pool_2x2(h_conv2)

    # densely connected layer

    W_fc1 = weight_variable([7 * 7 * 64, 1024])
    b_fc1 = bias_variable([1024])

    h_pool2_flat = tf.reshape(h_pool2, [-1, 7*7*64])
    h_fc1 = tf.nn.relu(tf.matmul(h_pool2_flat, W_fc1) + b_fc1)


    if mode == ModeKeys.TRAIN:
        h_fc1_drop = tf.nn.dropout(h_fc1, params["dropout"])
    else:
        h_fc1_drop = h_fc1

    # readout layer

    W_fc2 = weight_variable([1024, 10])
    b_fc2 = bias_variable([10])

    y_conv = tf.matmul(h_fc1_drop, W_fc2) + b_fc2

    cross_entropy = tf.reduce_mean(
        tf.nn.softmax_cross_entropy_with_logits(y_conv, y_))
    # train_step = tf.train.AdamOptimizer(1e-4).minimize(cross_entropy)
    train_op = tf.contrib.layers.optimize_loss(
        loss=cross_entropy,
        global_step=tf.contrib.framework.get_global_step(),
        learning_rate=params["learning_rate"],
        # optimizer=tf.train.AdamOptimizer
        optimizer="Adam")

    prediction = tf.argmax(y_conv, 1)
    # you can alternately construct and return a prediction dict
    return prediction, cross_entropy, train_op


def run_cnn_classifier():
    """Run a CNN classifier using a custom Estimator."""

    print("Downloading and reading data sets...")
    mnist = input_data.read_data_sets(FLAGS.data_dir, one_hot=True)

    # Set model params
    model_params = {"learning_rate": 1e-4, "dropout": 0.5}

    cnn = tf.contrib.learn.Estimator(
        model_fn=model_fn, params=model_params,
        model_dir=FLAGS.model_dir)

    print("Starting training for %s steps max" % FLAGS.num_steps)
    cnn.fit(x=mnist.train.images,
            y=mnist.train.labels, batch_size=50,
            max_steps=FLAGS.num_steps)

    # Evaluate accuracy.
    print(cnn.evaluate(mnist.test.images, mnist.test.labels))

    # Print out some predictions, just drawn from the test data.
    batch = mnist.test.next_batch(20)
    predictions = cnn.predict(x=batch[0], as_iterable=True)
    for i, p in enumerate(predictions):
        print("Prediction: %s for correct answer %s" %
              (p, list(batch[1][i]).index(1)))


def main(_):
    run_cnn_classifier()


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--data_dir', type=str, default='/tmp/MNIST_data',
                        help='Directory for storing data')
    parser.add_argument('--model_dir', type=str,
                        default=os.path.join(
                            "/tmp/tfmodels/mnist_estimator",
                            str(int(time.time()))),
                        help='Directory for storing model info')
    parser.add_argument('--num_steps', type=int,
                        default=20000,
                        help='Number of training steps to run')
    FLAGS = parser.parse_args()
    tf.app.run()
