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
"""Builds the MNIST network.
Provides example_parser and make_model_fn for use in a custom Estimator
"""
import tensorflow as tf
from tensorflow.contrib import layers, framework
from tensorflow.contrib.learn import ModeKeys
from tensorflow.contrib.learn.python.learn import metric_spec
from tensorflow.contrib.metrics.python.ops import metric_ops

# The MNIST dataset has 10 classes, representing the digits 0 through 9.
NUM_CLASSES = 10


def make_model_fn(args):
  def model_fn(features, labels, mode):
    """Builds generic graph for training or eval."""

    # Build a Graph that computes predictions from the inference model.
    logits = inference(features, args.hidden1, args.hidden2)

    tensors = {}
    # Add to the Graph the Ops for loss calculation.
    if mode == ModeKeys.INFER:
      tensors['digit'] = tf.argmax(logits, 1)
      loss_op = None
    else:
      loss_op = loss(logits, labels)
      tensors['loss'] = loss_op
      tf.scalar_summary('loss', loss_op)

    if mode == ModeKeys.EVAL:
      # Add to the Graph the Ops for accuracy calculation.
      accuracy_op = evaluation(logits, labels)
      tensors['accuracy'] = accuracy_op
      tf.scalar_summary('training/hptuning/metric', accuracy_op)

    # Add to the Graph the Ops that calculate and apply gradients.
    if mode == ModeKeys.TRAIN:
      global_step = framework.get_global_step()
      # Create the gradient descent optimizer with the given learning rate.
      optimizer = tf.train.GradientDescentOptimizer(args.learning_rate)
      # Create a variable to track the global step.
      # Use the optimizer to apply the gradients that minimize the loss
      # (and also increment the global step counter) as a single training step.
      train_op = optimizer.minimize(loss_op, global_step=global_step)
      # Add streaming means.
    else:
      train_op = None

    return tensors, loss_op, train_op
  return model_fn


METRICS = {
    'loss': metric_spec.MetricSpec(
        metric_fn=metric_ops.streaming_mean,
        prediction_key='loss'
    ),
    'accuracy': metric_spec.MetricSpec(
        metric_fn=metric_ops.streaming_mean,
        prediction_key='accuracy'
    )
}


def inference(images, hidden1_units, hidden2_units):
  """Build the MNIST model up to where it may be used for inference.

  Args:
    images: Images placeholder, from inputs().
    hidden1_units: Size of the first hidden layer.
    hidden2_units: Size of the second hidden layer.
  Returns:
    softmax_linear: Output tensor with the computed logits.
  """
  hidden1 = layers.fully_connected(images, hidden1_units)
  hidden2 = layers.fully_connected(hidden1, hidden2_units)
  return layers.fully_connected(hidden2, NUM_CLASSES, activation_fn=None)


def loss(logits, labels):
  """Calculates the loss from the logits and the labels.

  Args:
    logits: Logits tensor, float - [batch_size, NUM_CLASSES].
    labels: Labels tensor, int32 - [batch_size].
  Returns:
    loss: Loss tensor of type float.
  """
  labels = tf.to_int64(labels)
  cross_entropy = tf.nn.sparse_softmax_cross_entropy_with_logits(
      logits, labels, name='xentropy')
  return tf.reduce_mean(cross_entropy, name='xentropy_mean')


def evaluation(logits, labels):
  """Evaluate the quality of the logits at predicting the label.

  Args:
    logits: Logits tensor, float - [batch_size, NUM_CLASSES].
    labels: Labels tensor, int32 - [batch_size], with values in the
      range [0, NUM_CLASSES).
  Returns:
    A scalar float tensor with the ratio of examples (out of batch_size)
    that were predicted correctly.
  """
  # For a classifier model, we can use the in_top_k Op.
  # It returns a bool tensor with shape [batch_size] that is true for
  # the examples where the label is in the top k (here k=1)
  # of all logits for that example.
  correct = tf.nn.in_top_k(logits, tf.cast(labels, tf.int32), 1)
  return tf.reduce_mean(tf.cast(correct, tf.float32))
