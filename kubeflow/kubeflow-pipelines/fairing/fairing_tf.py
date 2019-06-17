# Copyright 2015 The TensorFlow Authors. All Rights Reserved.
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

"""Trains and Evaluates the MNIST network using a feed dictionary."""
import os

from six.moves import xrange  # pylint: disable=redefined-builtin
import tensorflow as tf
from tensorflow.examples.tutorials.mnist import input_data
from tensorflow.examples.tutorials.mnist import mnist

import fairing

INPUT_DATA_DIR = '/tmp/tensorflow/mnist/input_data/'
MAX_STEPS = 2000
BATCH_SIZE = 100
LEARNING_RATE = 0.3
HIDDEN_1 = 128
HIDDEN_2 = 32

# HACK: Ideally we would want to have a unique subpath for each instance of the job, but since we can't
# we are instead appending HOSTNAME to the logdir
LOG_DIR = os.path.join(os.getenv('TEST_TMPDIR', '/tmp'),
                       'tensorflow/mnist/logs/fully_connected_feed/', os.getenv('HOSTNAME', ''))
MODEL_DIR = os.path.join(LOG_DIR, 'model.ckpt')


class TensorflowModel():
    def train(self, **kwargs):
        tf.logging.set_verbosity(tf.logging.ERROR)
        self.data_sets = input_data.read_data_sets(INPUT_DATA_DIR)
        self.images_placeholder = tf.placeholder(
            tf.float32, shape=(BATCH_SIZE, mnist.IMAGE_PIXELS))
        self.labels_placeholder = tf.placeholder(tf.int32, shape=(BATCH_SIZE))

        logits = mnist.inference(self.images_placeholder,
                                 HIDDEN_1,
                                 HIDDEN_2)

        self.loss = mnist.loss(logits, self.labels_placeholder)
        self.train_op = mnist.training(self.loss, LEARNING_RATE)
        self.summary = tf.summary.merge_all()
        init = tf.global_variables_initializer()
        self.sess = tf.Session()
        self.summary_writer = tf.summary.FileWriter(LOG_DIR, self.sess.graph)
        self.sess.run(init)

        data_set = self.data_sets.train
        for step in xrange(MAX_STEPS):
            images_feed, labels_feed = data_set.next_batch(BATCH_SIZE, False)
            feed_dict = {
                self.images_placeholder: images_feed,
                self.labels_placeholder: labels_feed,
            }

            _, loss_value = self.sess.run([self.train_op, self.loss],
                                     feed_dict=feed_dict)
            if step % 100 == 0:
                print("At step {}, loss = {}".format(step, loss_value))
                summary_str = self.sess.run(self.summary, feed_dict=feed_dict)
                self.summary_writer.add_summary(summary_str, step)
                self.summary_writer.flush()


if __name__ == '__main__':
    fairing.config.set_builder(name='cluster')
    fairing.config.set_model(TensorflowModel())
    fairing.config.run()
