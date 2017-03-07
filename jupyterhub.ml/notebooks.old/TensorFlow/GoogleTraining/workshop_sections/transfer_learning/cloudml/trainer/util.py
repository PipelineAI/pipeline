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
"""Reusable utility functions.

This file is generic and can be reused by other models without modification.
"""

import multiprocessing

import tensorflow as tf
from tensorflow.python.lib.io import file_io


def read_examples(input_files, batch_size, shuffle, num_epochs=None):
  """Creates readers and queues for reading example protos."""
  files = []
  for e in input_files:
    for path in e.split(','):
      files.extend(file_io.get_matching_files(path))
  thread_count = multiprocessing.cpu_count()

  # The minimum number of instances in a queue from which examples are drawn
  # randomly. The larger this number, the more randomness at the expense of
  # higher memory requirements.
  min_after_dequeue = 1000

  # When batching data, the queue's capacity will be larger than the batch_size
  # by some factor. The recommended formula is (num_threads + a small safety
  # margin). For now, we use a single thread for reading, so this can be small.
  queue_size_multiplier = thread_count + 3

  # Convert num_epochs == 0 -> num_epochs is None, if necessary
  num_epochs = num_epochs or None

  # Build a queue of the filenames to be read.
  filename_queue = tf.train.string_input_producer(files, num_epochs, shuffle)

  options = tf.python_io.TFRecordOptions(
      compression_type=tf.python_io.TFRecordCompressionType.GZIP)
  example_id, encoded_example = tf.TFRecordReader(options=options).read_up_to(
      filename_queue, batch_size)

  if shuffle:
    capacity = min_after_dequeue + queue_size_multiplier * batch_size
    return tf.train.shuffle_batch(
        [example_id, encoded_example],
        batch_size,
        capacity,
        min_after_dequeue,
        enqueue_many=True,
        num_threads=thread_count)

  else:
    capacity = queue_size_multiplier * batch_size
    return tf.train.batch(
        [example_id, encoded_example],
        batch_size,
        capacity=capacity,
        enqueue_many=True,
        num_threads=thread_count)


def override_if_not_in_args(flag, argument, args):
  """Checks if flags is in args, and if not it adds the flag to args."""
  if flag not in args:
    args.extend([flag, argument])


def loss(loss_value):
  """Calculates aggregated mean loss."""
  total_loss = tf.Variable(0.0, False)
  loss_count = tf.Variable(0, False)
  total_loss_update = tf.assign_add(total_loss, loss_value)
  loss_count_update = tf.assign_add(loss_count, 1)
  loss_op = total_loss / tf.cast(loss_count, tf.float32)
  return [total_loss_update, loss_count_update], loss_op


def accuracy(logits, labels):
  """Calculates aggregated accuracy."""
  is_correct = tf.nn.in_top_k(logits, labels, 1)
  correct = tf.reduce_sum(tf.cast(is_correct, tf.int32))
  incorrect = tf.reduce_sum(tf.cast(tf.logical_not(is_correct), tf.int32))
  correct_count = tf.Variable(0, False)
  incorrect_count = tf.Variable(0, False)
  correct_count_update = tf.assign_add(correct_count, correct)
  incorrect_count_update = tf.assign_add(incorrect_count, incorrect)
  accuracy_op = tf.cast(correct_count, tf.float32) / tf.cast(
      correct_count + incorrect_count, tf.float32)
  return [correct_count_update, incorrect_count_update], accuracy_op
