# Copyright 2018, The TensorFlow Authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""Differentially private optimizers for TensorFlow."""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import tensorflow as tf

from privacy.optimizers import gaussian_query


def make_optimizer_class(cls):
  """Constructs a DP optimizer class from an existing one."""
  if (tf.train.Optimizer.compute_gradients.__code__ is
      not cls.compute_gradients.__code__):
    tf.logging.warning(
        'WARNING: Calling make_optimizer_class() on class %s that overrides '
        'method compute_gradients(). Check to ensure that '
        'make_optimizer_class() does not interfere with overridden version.',
        cls.__name__)

  class DPOptimizerClass(cls):
    """Differentially private subclass of given class cls."""

    def __init__(self, l2_norm_clip, noise_multiplier, num_microbatches, *args,
                 **kwargs):
      super(DPOptimizerClass, self).__init__(*args, **kwargs)
      stddev = l2_norm_clip * noise_multiplier
      self._num_microbatches = num_microbatches
      self._private_query = gaussian_query.GaussianAverageQuery(
          l2_norm_clip, stddev, num_microbatches)
      self._global_state = self._private_query.initial_global_state()

    def compute_gradients(self,
                          loss,
                          var_list,
                          gate_gradients=tf.train.Optimizer.GATE_OP,
                          aggregation_method=None,
                          colocate_gradients_with_ops=False,
                          grad_loss=None):

      # Note: it would be closer to the correct i.i.d. sampling of records if
      # we sampled each microbatch from the appropriate binomial distribution,
      # although that still wouldn't be quite correct because it would be
      # sampling from the dataset without replacement.
      microbatches_losses = tf.reshape(loss, [self._num_microbatches, -1])
      sample_params = (
          self._private_query.derive_sample_params(self._global_state))

      def process_microbatch(i, sample_state):
        """Process one microbatch (record) with privacy helper."""
        grads, _ = zip(*super(cls, self).compute_gradients(
            tf.gather(microbatches_losses, [i]), var_list, gate_gradients,
            aggregation_method, colocate_gradients_with_ops, grad_loss))
        grads_list = list(grads)
        sample_state = self._private_query.accumulate_record(
            sample_params, sample_state, grads_list)
        return [tf.add(i, 1), sample_state]

      i = tf.constant(0)

      if var_list is None:
        var_list = (
            tf.trainable_variables() + tf.get_collection(
                tf.GraphKeys.TRAINABLE_RESOURCE_VARIABLES))
      sample_state = self._private_query.initial_sample_state(
          self._global_state, var_list)

      # Use of while_loop here requires that sample_state be a nested structure
      # of tensors. In general, we would prefer to allow it to be an arbitrary
      # opaque type.
      _, final_state = tf.while_loop(
          lambda i, _: tf.less(i, self._num_microbatches), process_microbatch,
          [i, sample_state])
      final_grads, self._global_state = (
          self._private_query.get_noised_average(final_state,
                                                 self._global_state))

      return list(zip(final_grads, var_list))

  return DPOptimizerClass


DPAdagradOptimizer = make_optimizer_class(tf.train.AdagradOptimizer)
DPAdamOptimizer = make_optimizer_class(tf.train.AdamOptimizer)
DPGradientDescentOptimizer = make_optimizer_class(
    tf.train.GradientDescentOptimizer)
