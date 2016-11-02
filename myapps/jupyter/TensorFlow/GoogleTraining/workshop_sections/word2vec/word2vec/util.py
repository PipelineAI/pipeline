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

import nltk
import numpy as np
import tensorflow as tf

from tensorflow.python.lib.io.tf_record import TFRecordCompressionType


GCS_SKIPGRAMS = [
    'gs://ml-workshop/skipgrams/batches-{}.pb2'.format(i)
    for i in range(4)
]


def _rolling_window(a, window):
  shape = a.shape[:-1] + (a.shape[-1] - window + 1, window)
  strides = a.strides + (a.strides[-1],)
  return np.lib.stride_tricks.as_strided(a, shape=shape, strides=strides)



def to_skipgrams(window, skip_window, num_skips):
  contexts = np.random.choice(np.concatenate(
      (window[:skip_window], window[skip_window + 1:])
  ), size=num_skips, replace=False)
  targets = np.repeat(window[skip_window], num_skips)
  return np.concatenate((targets, contexts))


def generate_batches(word_array, num_skips=2, skip_window=1):
  assert num_skips <= 2 * skip_window
  span = 2 * skip_window + 1

  span_windows = _rolling_window(word_array, span)
  batches = np.apply_along_axis(
    to_skipgrams, 1, span_windows, skip_window, num_skips)
  # Separate targets and contexts
  batches_sep = np.reshape(batches, (-1, 2, num_skips))
  # Gather targets and contexts
  batches_gathered = np.transpose(batches_sep, (1, 0, 2))
  # Squash targets and contexts
  batches_squashed = np.reshape(batches_gathered, (2, -1))
  return batches_squashed[0], batches_squashed[1]

def build_string_index(string, vocab_size=2 ** 15):
  word_array = np.array(nltk.word_tokenize(string))

  unique, counts = np.unique(word_array, return_counts=True)

  sort_unique = np.argsort(counts)
  sorted_counts = counts[sort_unique][::-1][:vocab_size - 1]
  unique_sorted = unique[sort_unique][::-1][:vocab_size - 1]

  return unique_sorted, sorted_counts, word_array


def write_index_to_file(index, filename):
  with open(filename, 'wb') as writer:
    writer.write(tf.contrib.util.make_tensor_proto(index).SerializeToString())


def make_input_fn(filenames,
                  batch_size,
                  index_file,
                  num_epochs=None):
  def _input_fn():
    with tf.name_scope('input'):
      index = tf.parse_tensor(tf.read_file(index_file), tf.string)
      filename_queue = tf.train.string_input_producer(
          filenames, num_epochs=num_epochs)
      reader = tf.TFRecordReader(
          options=tf.python_io.TFRecordOptions(
              compression_type=TFRecordCompressionType.GZIP
          )
      )
      _, serialized_example = reader.read(filename_queue)

      words = tf.parse_single_example(
          serialized_example,
          {
              'target_words': tf.FixedLenFeature([batch_size], tf.string),
              'context_words': tf.FixedLenFeature([batch_size], tf.string)
          }
      )
      return {
          'targets': tf.expand_dims(words['target_words'], 1),
          'index': index
      }, words['context_words']

  return _input_fn


def write_batches_to_file(filename,
                          batch_size,
                          word_array,
                          num_skips=8,
                          skip_window=4,
                          num_shards=4):
    span = 2 * skip_window + 1
    span_windows = _rolling_window(word_array, span)
    span_batch_size = batch_size // num_skips
    span_windows_len = (len(span_windows) // span_batch_size) * span_batch_size
    span_windows_trunc = span_windows[:span_windows_len]
    window_batches = np.reshape(
        span_windows_trunc, (-1, span_batch_size, span))

    shard_size = len(window_batches) // num_shards

    options = tf.python_io.TFRecordOptions(
        compression_type=TFRecordCompressionType.GZIP)
    for shard, index in enumerate(range(0, len(window_batches), shard_size)):
      shard_file = '{}-{}.pb2'.format(filename, shard)
      with tf.python_io.TFRecordWriter(shard_file, options=options) as writer:
        for windows in window_batches[index:index+shard_size]:
          batches = np.apply_along_axis(
              to_skipgrams, 1, windows, skip_window, num_skips)
          # Separate targets and contexts
          batches_sep = np.reshape(batches, (-1, 2, num_skips))
          # Gather targets and contexts
          batches_gathered = np.transpose(batches_sep, (1, 0, 2))
          # Squash targets and contexts
          batches_squashed = np.reshape(batches_gathered, (2, -1))

          writer.write(
              tf.train.Example(features=tf.train.Features(feature={
                  'target_words': tf.train.Feature(
                      bytes_list=tf.train.BytesList(
                          value=batches_squashed[0])),
                  'context_words': tf.train.Feature(
                      bytes_list=tf.train.BytesList(
                          value=batches_squashed[1]))
              })).SerializeToString()
          )
