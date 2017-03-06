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

import argparse

import nltk
import numpy as np
import tensorflow as tf


def build_vocab(word_tensor, vocab_size):
  unique, idx = tf.unique(word_tensor)

  counts = tf.foldl(
      lambda counts, item: counts + tf.one_hot(
          tf.reshape(item, [-1]),
          tf.shape(unique)[0],
          dtype=tf.int32)[0],
      idx,
      initializer=tf.zeros_like(unique, dtype=tf.int32),
      back_prop=False
  )
  _, indices = tf.nn.top_k(counts, k=vocab_size)
  return tf.gather(unique, indices)


def build_string_index(word_array, vocab_size=2 ** 15):
  unique, inverse, counts = np.unique(
    word_array, return_inverse=True, return_counts=True)

  max_index = len(unique) - 1
  sort_unique = np.argsort(counts)
  shuffle_idx = np.searchsorted(counts[sort_unique], counts)
  unique_sorted = unique[sort_unique][::-1]

  index = np.concatenate((np.array(['UNK']), unique_sorted[:vocab_size - 1]))

  indices = max_index - shuffle_idx + 1
  indices = np.where(
    indices > vocab_size - 1,
    np.zeros(len(indices), dtype=np.int8),
    indices
  )

  word_indices = indices[inverse]
  return index, word_indices



def prepare_data_files(infile, outfile, vocab_size):
  print('Tokenizing Text File')
  with open(infile, 'r') as reader:
    words = np.array(nltk.word_tokenize(reader.read()))

  print('Tokenized {} words'.format(len(words)))

  print('Building String Index')
  index, word_indices = build_string_index(words)

  print('{} \ntransformed to\n{}\nwith\n{}'.format(
      words, word_indices, index))

  train_eval_split = int(len(words) * .9)
  train_words = word_indices[:train_eval_split]
  eval_words = word_indices[train_eval_split:]
  print('Writing training data')
  with open('{}-train.pb2'.format(outfile), 'wb') as writer:
    writer.write(tf.contrib.util.make_tensor_proto(
        train_words).SerializeToString())

  print('Writing eval data')
  with open('{}-eval.pb2'.format(outfile), 'wb') as writer:
    writer.write(tf.contrib.util.make_tensor_proto(
        eval_words).SerializeToString())

  print('Writing vocab file')
  with open('{}-vocab.tsv'.format(outfile), 'w') as writer:
    writer.write('\n'.join(index.tolist()))



if __name__=='__main__':
  parser = argparse.ArgumentParser()
  parser.add_argument('--vocab-size', type=int, default=2**15)
  parser.add_argument('--text-file', type=str)
  parser.add_argument('--output-path', type=str)
  args = parser.parse_args()
  prepare_data_files(args.text_file, args.output_path, args.vocab_size)
