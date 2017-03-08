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

    # YOUR CODE HERE.

    # Build your model graph, including the loss function and training op.
    # You don’t need to define ‘placeholders’— they are passed in
    # as the first 2 args (here, x and target).
    # You can use the mode values — ModeKeys.TRAIN, ModeKeys.EVAL, and
    # ModeKeys.INFER, if there is part of the graph that you only want to
    # build under some of the contexts.

    # Note that instead of one 'prediction' value, you can alternately
    # construct and return a prediction dict
    return prediction, cross_entropy, train_op


def run_cnn_classifier():
    """Run a CNN classifier using a custom Estimator."""

    print("Downloading and reading data sets...")
    mnist = input_data.read_data_sets(FLAGS.data_dir, one_hot=True)

    # Set model params
    model_params = {"learning_rate": 1e-4, "dropout": 0.5}

    # YOUR CODE HERE
    # Create a tf.contrib.learn.Estimator called 'cnn', and call its 'fit()'
    # method to train it.

    # ....

    # After training, evaluate accuracy.
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
