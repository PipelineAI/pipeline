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
  tf.summary.image('hidden_weights', tf.expand_dims([hidden_weights], -1))

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
  tf.summary.scalar('loss', loss)

  accuracy, update_acc = tf.contrib.metrics.streaming_accuracy(
      predictions > 0.5, labels)
  tf.summary.scalar('accuracy', accuracy)

  gs = tf.Variable(0, trainable=False)
  optimizer = tf.train.GradientDescentOptimizer(0.2)

  grads_and_vars = optimizer.compute_gradients(loss)

  gradients = zip(grads_and_vars)[0]
  tf.summary.histogram('gradients', gradients)

  train_op = optimizer.apply_gradients(grads_and_vars, global_step=gs)

  return train_op, loss, gs, update_acc


def main(output_dir, summaries_every, num_steps):
  graph = tf.Graph()


  with graph.as_default():
    features = tf.placeholder(tf.float32, shape=[4, 2])
    labels = tf.placeholder(tf.int32, shape=[4])

    train_op, loss, gs, update_acc = make_graph(features, labels)
    init = tf.global_variables_initializer()
    init_local = tf.local_variables_initializer()
    summary_op = tf.summary.merge_all()


  writer = tf.summary.FileWriter(output_dir, graph=graph, flush_secs=1)

  with tf.Session(graph=graph) as sess:
    init.run()
    init_local.run()
    step = 0
    xy = np.array([
        [True, False],
        [True, True],
        [False, False],
        [False, True]
    ], dtype=np.float)
    y_ = np.array([True, False, False, True], dtype=np.int32)
    while step < num_steps:

      _, _, step, loss_value, summaries = sess.run(
          [train_op, update_acc, gs, loss, summary_op],
          feed_dict={features: xy, labels: y_}
      )
      if step % summaries_every == 0:
        writer.add_summary(summaries, global_step=step)
        tf.logging.info('Wrote summaries at step {}'.format(step))


if __name__ == '__main__':
  parser = argparse.ArgumentParser()
  parser.add_argument('--num-steps', type=int, default=5000)
  parser.add_argument(
      '--output-dir',
      help='GCS or local path for summary writing',
      required=True
  )
  parser.add_argument('--summaries-every', type=int, default=5)
  args = parser.parse_args()
  main(args.output_dir, args.summaries_every, args.num_steps)
