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

import math

import tensorflow as tf

from tensorflow.contrib.learn import ModeKeys


def model_args(parser):
  group = parser.add_argument_group(title='Model Arguments')
  group.add_argument('--reference-words', nargs='*', type=str)
  group.add_argument('--num-partitions', default=1, type=int)
  group.add_argument('--embedding-size', default=128, type=int)
  group.add_argument('--vocab-size', default=2 ** 15, type=int)
  group.add_argument('--num-sim', default=8, type=int)
  group.add_argument('--num-sampled', default=64, type=int)
  group.add_argument('--learning-rate', default=0.1, type=float)
  return group


def make_model_fn(args):
  def _model_fn(inputs, context_words, mode):
    target_words = inputs['targets']
    index_tensor = inputs['index']
    reverse_index = tf.contrib.lookup.HashTable(
        tf.contrib.lookup.KeyValueTensorInitializer(
            index_tensor,
            tf.constant(range(1, args.vocab_size), dtype=tf.int64)
        ),
        0
    )

    # tf.contrib.learn.Estimator.fit adds an addition dimension to input
    target_words_squeezed = tf.squeeze(target_words, squeeze_dims=[1])
    target_indices = reverse_index.lookup(target_words_squeezed)

    with tf.device(tf.train.replica_device_setter()):
      with tf.variable_scope('nce',
                             partitioner=tf.fixed_size_partitioner(
                                 args.num_partitions)):

        embeddings = tf.get_variable(
            'embeddings',
            shape=[args.vocab_size, args.embedding_size],
            dtype=tf.float32,
            initializer=tf.random_uniform_initializer(-1.0, 1.0)
        )
        if mode in [ModeKeys.TRAIN, ModeKeys.EVAL]:
          nce_weights = tf.get_variable(
              'nce_weights',
              shape=[args.vocab_size, args.embedding_size],
              dtype=tf.float32,
              initializer=tf.truncated_normal_initializer(
                  stddev=1.0 / math.sqrt(args.embedding_size)
              )
          )
          nce_biases = tf.get_variable(
              'nce_biases',
              initializer=tf.zeros_initializer([args.vocab_size]),
              dtype=tf.float32
          )

      tensors, loss, train_op = ({}, None, None)

      if mode in [ModeKeys.TRAIN, ModeKeys.EVAL]:
        context_indices = tf.expand_dims(
            reverse_index.lookup(context_words), 1)
        embedded = tf.nn.embedding_lookup(embeddings, target_indices)

        loss = tf.reduce_mean(tf.nn.nce_loss(
            nce_weights,
            nce_biases,
            embedded,
            context_indices,
            args.num_sampled,
            args.vocab_size
        ))
        tf.scalar_summary('loss', loss)
        tf.scalar_summary('training/hptuning/metric', loss)

      if mode == ModeKeys.TRAIN:
        train_op = tf.train.GradientDescentOptimizer(
            args.learning_rate
        ).minimize(
            loss, global_step=tf.contrib.framework.get_global_step()
        )

      if mode == ModeKeys.INFER:
        # Compute the cosine similarity between examples and embeddings.
        norm = tf.sqrt(tf.reduce_sum(tf.square(embeddings), 1, keep_dims=True))
        normalized_embeddings = embeddings / norm
        valid_embeddings = tf.nn.embedding_lookup(
            normalized_embeddings, target_indices)
        similarity = tf.matmul(
            valid_embeddings, normalized_embeddings, transpose_b=True)
        tensors['values'], predictions = tf.nn.top_k(
            similarity, sorted=True, k=args.num_sim)
        index_tensor = tf.concat(0, [tf.constant(['UNK']), index_tensor])
        tensors['predictions'] = tf.gather(index_tensor, predictions)

      return tensors, loss, train_op
  return _model_fn


