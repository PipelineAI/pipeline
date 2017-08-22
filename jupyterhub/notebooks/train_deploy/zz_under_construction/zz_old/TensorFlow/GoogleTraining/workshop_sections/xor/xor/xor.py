# Copyright 2016 Google Inc. All Rights Reserved.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import argparse
import math

import numpy as np

import tensorflow as tf

tf.logging.set_verbosity(tf.logging.INFO)

def make_graph(features, labels, num_hidden=8):
  hidden_weights = tf.Variable(tf.truncated_normal(
      [2, num_hidden],
      stddev=1/math.sqrt(2)
  ))

  # Shape [4, num_hidden]
  hidden_activations = tf.nn.relu(tf.matmul(features, hidden_weights))

  output_weights = tf.Variable(tf.truncated_normal(
      [num_hidden, 1],
      stddev=1/math.sqrt(num_hidden)
  ))

  # Shape [4, 1]
  logits = tf.matmul(hidden_activations, output_weights)

  # Shape [4]
  predictions = tf.sigmoid(tf.squeeze(logits))
  loss = tf.reduce_mean(tf.square(predictions - tf.to_float(labels)))

  gs = tf.Variable(0, trainable=False)
  train_op = tf.train.GradientDescentOptimizer(0.2).minimize(
      loss, global_step=gs)

  return train_op, loss, gs

def main(num_steps):
  graph = tf.Graph()

  with graph.as_default():
    features = tf.placeholder(tf.float32, shape=[4, 2])
    labels = tf.placeholder(tf.int32, shape=[4])

    train_op, loss, gs = make_graph(features, labels)
    init = tf.global_variables_initializer()

  with tf.Session(graph=graph) as sess:
    init.run()
    step = 0
    xy = np.array([
        [True, False],
        [True, True],
        [False, False],
        [False, True]
    ], dtype=np.float)
    y_ = np.array([True, False, False, True], dtype=np.int32)
    while step < num_steps:

      _, step, loss_value = sess.run(
          [train_op, gs, loss],
          feed_dict={features: xy, labels: y_}
      )
    tf.logging.info('Final loss is: {}'.format(loss_value))


if __name__ == '__main__':
  parser = argparse.ArgumentParser()
  parser.add_argument('--num-steps', type=int, default=5000)
  args = parser.parse_args()
  main(args.num_steps)
