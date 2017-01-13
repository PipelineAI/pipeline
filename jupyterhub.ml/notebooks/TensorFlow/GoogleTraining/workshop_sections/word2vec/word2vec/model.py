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
from tensorflow.contrib.tensorboard.plugins import projector


def make_model_fn(vocab_file=None,
                  output_path=None,
                  num_partitions=1,
                  embedding_size=128,
                  vocab_size=2 ** 15,
                  num_sim=8,
                  num_sampled=64,
                  num_skips=4,
                  skip_window=8,
                  learning_rate=0.1,
                  **unused_args):
  def _model_fn(inputs, context_indices, mode):
    if mode == ModeKeys.INFER:
      sparse_index_tensor = tf.string_split(
          [tf.read_file(vocab_file)],
          delimiter='\n'
      )
      index_tensor = tf.squeeze(tf.sparse_to_dense(
          sparse_index_tensor.indices,
          [1, vocab_size],
          sparse_index_tensor.values,
          default_value='UNK'
      ))
      reverse_index = tf.contrib.lookup.HashTable(
          tf.contrib.lookup.KeyValueTensorInitializer(
              index_tensor,
              tf.constant(range(vocab_size), dtype=tf.int64)
          ),
          0
      )
      target_indices = reverse_index.lookup(inputs)
    else:
      target_indices = inputs

    with tf.device(tf.train.replica_device_setter()):
      with tf.variable_scope('nce',
                             partitioner=tf.fixed_size_partitioner(
                                 num_partitions)):

        embeddings = tf.get_variable(
            'embeddings',
            shape=[vocab_size, embedding_size],
            dtype=tf.float32,
            initializer=tf.random_uniform_initializer(-1.0, 1.0)
        )
        if mode in [ModeKeys.TRAIN, ModeKeys.EVAL]:
          nce_weights = tf.get_variable(
              'nce_weights',
              shape=[vocab_size, embedding_size],
              dtype=tf.float32,
              initializer=tf.truncated_normal_initializer(
                  stddev=1.0 / math.sqrt(embedding_size)
              )
          )
          nce_biases = tf.get_variable(
              'nce_biases',
              initializer=tf.zeros_initializer([vocab_size]),
              dtype=tf.float32
          )

      tensors, loss, train_op = ({}, None, None)

      if mode in [ModeKeys.TRAIN, ModeKeys.EVAL]:
        embedded = tf.nn.embedding_lookup(embeddings, target_indices)

        loss = tf.reduce_mean(tf.nn.nce_loss(
            nce_weights,
            nce_biases,
            embedded,
            context_indices,
            num_sampled,
            vocab_size
        ))
        tf.summary.scalar('loss', loss)
        tf.summary.scalar('training/hptuning/metric', loss)

        # Embedding Visualizer
        embedding_writer = tf.summary.FileWriter(output_path)
        config = projector.ProjectorConfig()
        embedding = config.embeddings.add()
        embedding.tensor_name = embeddings.name
        embedding.metadata_path = vocab_file
        projector.visualize_embeddings(embedding_writer, config)

      if mode == ModeKeys.TRAIN:
        train_op = tf.train.GradientDescentOptimizer(
            learning_rate
        ).minimize(
            loss,
            global_step=tf.contrib.framework.get_or_create_global_step()
        )

      if mode == ModeKeys.INFER:
        # Compute the cosine similarity between examples and embeddings.
        norm = tf.sqrt(tf.reduce_sum(tf.square(embeddings), 1, keep_dims=True))
        normalized_embeddings = embeddings / norm
        valid_embeddings = tf.nn.embedding_lookup(
            normalized_embeddings, tf.squeeze(target_indices))
        similarity = tf.matmul(
            valid_embeddings, normalized_embeddings, transpose_b=True)
        tensors['values'], predictions = tf.nn.top_k(
            similarity, sorted=True, k=num_sim)
        index_tensor = tf.concat(0, [tf.constant(['UNK']), index_tensor])
        tensors['predictions'] = tf.gather(index_tensor, predictions)

      return tensors, loss, train_op
  return _model_fn


