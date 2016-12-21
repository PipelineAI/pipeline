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

# This file is a modification of the code here:
# https://github.com/dennybritz/cnn-text-classification-tf

import datetime
import os
import time
import json

import data_helpers2 as data_helpers

import numpy as np

import tensorflow as tf

from text_cnn import TextCNN

# Parameters
# ==================================================

# Model Hyperparameters
# set default embedding dim to be 200 to match the word2vec embeddings
tf.flags.DEFINE_integer(
    "embedding_dim", 200,
    "Dimensionality of character embedding (default: 200)")
tf.flags.DEFINE_string(
    "filter_sizes", "3,4,5", "Comma-separated filter sizes (default: '3,4,5')")
tf.flags.DEFINE_integer(
    "num_filters", 128, "Number of filters per filter size (default: 128)")
tf.flags.DEFINE_float(
    "dropout_keep_prob", 0.5, "Dropout keep probability (default: 0.5)")
tf.flags.DEFINE_float(
    "l2_reg_lambda", 0.0, "L2 regularization lambda (default: 0.0)")

# Training parameters
tf.flags.DEFINE_integer("batch_size", 64, "Batch Size (default: 64)")
tf.flags.DEFINE_integer(
    "num_epochs", 200, "Number of training epochs (default: 200)")
tf.flags.DEFINE_integer(
    "evaluate_every", 100,
    "Evaluate model on dev set after this many steps (default: 100)")
tf.flags.DEFINE_integer(
    "checkpoint_every", 100, "Save model after this many steps (default: 100)")
# Misc Parameters
tf.flags.DEFINE_string(
    "embeds_file", None, "File containing learned word embeddings")
tf.flags.DEFINE_string(
    "data_file", "/var/tensorflow/data/prepared_training_data.npz", "File containing npz file with \"sentences\" and \"labels\" arrays")
tf.flags.DEFINE_string(
    "output_dir", "/var/tensorflow/output", "Path to the directory in which to write checkpoints, outputs, and summaries")
tf.flags.DEFINE_string(
    "vocab_file", "/var/tensorflow/data/vocabpregen.json", "JSON File with work -> index mappings")

FLAGS = tf.flags.FLAGS
FLAGS._parse_flags()
print("\nParameters:")
for attr, value in sorted(FLAGS.__flags.items()):
    print("{}={}".format(attr.upper(), value))
print("")

# Data Preparation
# ==================================================
print("Loading data...")
data = np.load(FLAGS.data_file)
x = data["sentences"]
y = data["labels"]

print("Load vocabulary")
with open(FLAGS.vocab_file) as vocab_file:
    vocabulary = json.load(vocab_file)

# Randomly shuffle data
np.random.seed(10)
shuffle_indices = np.random.permutation(np.arange(len(y)))
x_shuffled = x[shuffle_indices]
y_shuffled = y[shuffle_indices]
# Split train/test set
# TODO: This is very crude, should use cross-validation
dev_size = 1000
x_train, x_dev = x_shuffled[:-dev_size], x_shuffled[-dev_size:]
y_train, y_dev = y_shuffled[:-dev_size], y_shuffled[-dev_size:]
print("Train/Dev split: {:d}/{:d}".format(len(y_train), len(y_dev)))

# just for epoch counting
num_batches_per_epoch = int(len(x_train)/FLAGS.batch_size) + 1

batches = enumerate(data_helpers.batch_iter(
    list(zip(x_train, y_train)), FLAGS.batch_size, FLAGS.num_epochs))

# Training
# ==================================================
batch_num = 0

# Output directory for models and summaries
out_dir = os.path.abspath(os.path.join(
    FLAGS.output_dir, "runs", str(int(time.time()))))

if not os.path.exists(out_dir):
    os.makedirs(out_dir)

print("Writing to {}\n".format(out_dir))

graph = tf.Graph()

with graph.as_default():
    cnn = TextCNN(
        sequence_length=x_train.shape[1],
        num_classes=2,
        vocab_size=len(vocabulary),
        embedding_size=FLAGS.embedding_dim,
        filter_sizes=list(map(int, FLAGS.filter_sizes.split(","))),
        num_filters=FLAGS.num_filters,
        l2_reg_lambda=FLAGS.l2_reg_lambda,
        embeds_file=FLAGS.embeds_file)

    def feed_fn():
        batch_num, batch = next(batches)
        print("Processing batch number: {}".format(batch_num))
        x_batch, y_batch = zip(*batch)
        return {
            cnn.input_x: x_batch,
            cnn.input_y: y_batch,
            cnn.dropout_keep_prob: FLAGS.dropout_keep_prob
        }


    # Define Training procedure
    global_step = tf.Variable(0, name="global_step", trainable=False)
    optimizer = tf.train.AdamOptimizer(1e-3)
    grads_and_vars = optimizer.compute_gradients(cnn.loss)
    train_op = optimizer.apply_gradients(grads_and_vars, global_step=global_step)

    # Keep track of gradient values and sparsity (optional)
    for g, v in grads_and_vars:
        if g is not None:
            grad_hist_summary = tf.histogram_summary(
                "{}/grad/hist".format(v.name), g)
            sparsity_summary = tf.scalar_summary(
                "{}/grad/sparsity".format(v.name), tf.nn.zero_fraction(g))


    # Summaries for loss and accuracy
    loss_summary = tf.scalar_summary("loss", cnn.loss)
    acc_summary = tf.scalar_summary("accuracy", cnn.accuracy)



tf.contrib.learn.train(
    graph,
    out_dir,
    train_op,
    cnn.loss,
    feed_fn=feed_fn,
    supervisor_save_model_secs=60,
    supervisor_save_summaries_steps=5,
    steps=num_batches_per_epoch*FLAGS.num_epochs
)
