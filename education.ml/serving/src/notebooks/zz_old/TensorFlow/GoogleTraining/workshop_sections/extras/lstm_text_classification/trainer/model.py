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
import tensorflow as tf
from tensorflow.contrib.learn import ModeKeys


def make_input_fn(filenames,
                  batch_size,
                  sentence_length,
                  vocab_size,
                  num_epochs=None):
  def _input_fn():
    with tf.name_scope('input'):
        filename_queue = tf.train.string_input_producer(
            filenames, num_epochs=num_epochs)

        reader = tf.TFRecordReader()
        _, serialized_example = reader.read_up_to(filename_queue)

        features = tf.parse_single_example(
            serialized_examples,
            {
                'words': tf.VarLenFeature(tf.string),
                'subreddit': tf.FixedLenFeature([1], tf.int64)
            }
        )
        padded_words = tf.sparse_to_dense(
            features['words'].indices,
            [sentence_length],
            features['words'].values,
            default_value='UNK'
        )
        word_indices = tf.string_to_hash_bucket_fast(
            padded_words,
            vocab_size)

        sentences, subreddits = tf.train.shuffle_batch(
            [word_indices, features['subreddit']],
            batch_size,
            capacity=1000 + 3 * batch_size,
            min_after_dequeue=1000,
            enqueue_many=False
        )
    return sentences, subreddits


def make_model_fn(args):
  def _model_fn(words,  # Shape [batch_size, sentence_length]
                subreddits,  # Shape [batch_size, 1]
                mode):
    tensors, loss, train_op = ({}, None, None)
    def partitioner(shape, **unused_args):
        partitions_list = [1] * len(shape)
        partitions_list[0] = min(args.num_param_servers, shape[0].value)
        return partitions_list

    # Shape [vocab_size, embedding_size]
    with tf.variable_scope('embeddings',
                           partitioner=partitioner):
        embeddings = tf.get_variable(
            'embeddings',
            shape=[args.vocab_size, args.embedding_size]
        )

    # Shape [batch_size, sentence_length, embedding_size]
    word_embeddings = tf.nn.embedding_lookup(embeddings, words)

    lstm = tf.nn.rnn_cell.BasicLSTMCell(args.lstm_size)

    outputs, state = tf.nn.rnn(
        lstm,
        [word_embeddings[:, i, :] for i in range(args.sentence_length)],
        dtype=tf.float32,
        scope='rnn'
    )

    # Shape [sentence_length, batch_size]
    outputs_concat = tf.squeeze(tf.pack(outputs))

    # Shape [batch_size]
    predictions = tf.reduce_mean(
        tf.exp(outputs_concat), reduction_indices=[0])

    tf.histogram_summary('predictions', predictions)

    if mode in [ModeKeys.TRAIN, ModeKeys.EVAL]:
      loss = tf.reduce_mean(tf.square(predictions - scores_cast))

    if mode == ModeKeys.TRAIN:
      train_op = tf.contrib.layers.optimize_loss(
          loss,
          tf.framework.get_global_step(),
          learning_rate,
          tf.train.AdamOptimizer,
          clip_gradients=1.0,
      )
    return tensors, loss, train_op
  return _model_fn
