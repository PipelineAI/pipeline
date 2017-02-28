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
"""Reusable input_fn factory."""

import multiprocessing

import tensorflow as tf
from tensorflow.python.lib.io.tf_record import TFRecordCompressionType

# The MNIST images are always 28x28 pixels.
IMAGE_SIZE = 28
IMAGE_PIXELS = IMAGE_SIZE * IMAGE_SIZE


def parse_examples(examples):
  feature_map = {
      'labels': tf.FixedLenFeature(
          shape=[], dtype=tf.int64, default_value=[-1]),
      'images': tf.FixedLenFeature(
          shape=[IMAGE_PIXELS], dtype=tf.float32),
  }
  features = tf.parse_example(examples, features=feature_map)
  return features['images'], features['labels']


def make_input_fn(files,
                  example_parser,
                  batch_size,
                  num_epochs=None):
  def _input_fn():
    """Creates readers and queues for reading example protos."""
    thread_count = multiprocessing.cpu_count()

    # The minimum number of instances in a queue from which examples are drawn
    # randomly. The larger this number, the more randomness at the expense of
    # higher memory requirements.
    min_after_dequeue = 1000

    # When batching data, the queue's capacity will be larger than the
    # batch_size by some factor. The recommended formula is (num_threads +
    # a small safety margin). For now, we use a single thread for reading,
    # so this can be small.
    queue_size_multiplier = thread_count + 3

    # Build a queue of the filenames to be read.
    filename_queue = tf.train.string_input_producer(
        files, num_epochs=num_epochs)

    example_id, encoded_examples = tf.TFRecordReader(
        options=tf.python_io.TFRecordOptions(
            compression_type=TFRecordCompressionType.GZIP
        )
    ).read_up_to(filename_queue, batch_size)

    features, targets = example_parser(encoded_examples)
    capacity = min_after_dequeue + queue_size_multiplier * batch_size
    return tf.train.shuffle_batch(
        [features, targets],
        batch_size,
        capacity,
        min_after_dequeue,
        enqueue_many=True,
        num_threads=thread_count)
  return _input_fn
