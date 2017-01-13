# Copyright 2016 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the 'License');
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an 'AS IS' BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ==============================================================================
from __future__ import division
from __future__ import print_function

import tensorflow as tf

tf.logging.set_verbosity(tf.logging.INFO)


def skipgrams(word_tensor,
              num_skips,
              skip_window,
              windows_per_batch,
              num_epochs=None):
  window_size = 2 * skip_window + 1
  num_windows = tf.shape(word_tensor)[0] - window_size
  batch_size = windows_per_batch * num_skips
  range_queue = tf.train.range_input_producer(
      num_windows,
      shuffle=False,
      capacity=windows_per_batch * 2,
      num_epochs=num_epochs
  )
  indices = range_queue.dequeue_many(windows_per_batch)

  # Shape [windows_per_batch * num_skips]
  # e.g. [1, 1, ... , 1, ..., windows_per_batch ..., windows_per_batch]
  window_indices = tf.reshape(
      tf.transpose(tf.tile(indices, [num_skips])),
      [-1]
  )

  possible_indices = range(window_size)
  del possible_indices[skip_window]

  possible_indices_tensor = tf.constant(possible_indices, dtype=tf.int32)
  index_indices = tf.random_uniform(
      [batch_size],
      maxval=window_size - 2,
      dtype=tf.int32
  )

  skip_indices = tf.gather(possible_indices_tensor, index_indices)
  true_skip_indices = skip_indices + window_indices
  skips = tf.gather(word_tensor, true_skip_indices)

  target_indices = tf.constant(skip_window, dtype=tf.int32, shape=[batch_size])
  true_target_indices = target_indices + window_indices
  targets = tf.gather(word_tensor, true_target_indices)

  return targets, skips


def make_input_fn(word_indices_file,
                  batch_size,
                  num_skips,
                  skip_window,
                  vocab_size,
                  num_epochs=None):
  def _input_fn():
    with tf.name_scope('input'):
      word_tensor = tf.parse_tensor(
          tf.read_file(word_indices_file),
          tf.int64
      )

      targets, contexts = skipgrams(
          word_tensor,
          num_skips,
          skip_window,
          batch_size // num_skips,
          num_epochs=num_epochs
      )

      contexts_matrix = tf.expand_dims(contexts, -1)
      return targets, contexts_matrix

  return _input_fn
