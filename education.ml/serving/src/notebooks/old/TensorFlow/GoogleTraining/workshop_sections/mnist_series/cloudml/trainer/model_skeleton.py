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
from tensorflow.contrib.metrics.python.ops import metric_ops

# The MNIST dataset has 10 classes, representing the digits 0 through 9.
NUM_CLASSES = 10


def make_model_fn(args):
  def model_fn(features, labels, mode):
    """Builds generic graph for training or eval."""

    # TODO logits = A tensor representing the pre-softmax likelyhood of
    # each digit. 
    tensors = {}
    # Add to the Graph the Ops for loss calculation.
    if mode == ModeKeys.INFER:
      # TODO tensors['digit'] = Tensor representing the predicted digit for 'features'
      # Since 'labels' is None we can't calculate a loss
      loss_op = None
    else:
      # TODO loss_op = Operation to calculate loss
      tensors['loss'] = loss_op
      tf.scalar_summary('loss', loss_op)

    # Add to the Graph the Ops for accuracy calculation.
    if mode == ModeKeys.EVAL:
      # TODO accuracy_op = Calculate the accuracy of the inferred digits given 'labels'
      tensors['accuracy'] = accuracy_op
      tf.scalar_summary('training/hptuning/metric', accuracy_op)

    # Add to the Graph the Ops that calculate and apply gradients.
    if mode == ModeKeys.TRAIN:
      global_step = framework.get_global_step()
      # TODO train_op = the gradient descent optimizer with the given learning rate
      # that minimizes the loss
    else:
      train_op = None

    return tensors, loss_op, train_op
  return model_fn


METRICS = {
    ('loss', 'loss'): metric_ops.streaming_mean,
    ('accuracy', 'accuracy'): metric_ops.streaming_mean,
}
