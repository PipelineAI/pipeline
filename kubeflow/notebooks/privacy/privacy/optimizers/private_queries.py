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

"""An interface for differentially private query mechanisms.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import abc


class PrivateQuery(object):
  """Interface for differentially private query mechanisms."""

  __metaclass__ = abc.ABCMeta

  @abc.abstractmethod
  def initial_global_state(self):
    """Returns the initial global state for the PrivateQuery."""
    pass

  @abc.abstractmethod
  def derive_sample_params(self, global_state):
    """Given the global state, derives parameters to use for the next sample.

    Args:
      global_state: The current global state.

    Returns:
      Parameters to use to process records in the next sample.
    """
    pass

  @abc.abstractmethod
  def initial_sample_state(self, global_state, tensors):
    """Returns an initial state to use for the next sample.

    Args:
      global_state: The current global state.
      tensors: A structure of tensors used as a template to create the initial
        sample state.

    Returns: An initial sample state.
    """
    pass

  @abc.abstractmethod
  def accumulate_record(self, params, sample_state, record):
    """Accumulates a single record into the sample state.

    Args:
      params: The parameters for the sample.
      sample_state: The current sample state.
      record: The record to accumulate.

    Returns:
      The updated sample state.
    """
    pass

  @abc.abstractmethod
  def get_query_result(self, sample_state, global_state):
    """Gets query result after all records of sample have been accumulated.

    Args:
      sample_state: The sample state after all records have been accumulated.
      global_state: The global state.

    Returns:
      A tuple (result, new_global_state) where "result" is the result of the
      query and "new_global_state" is the updated global state.
    """
    pass


class PrivateSumQuery(PrivateQuery):
  """Interface for differentially private mechanisms to compute a sum."""

  @abc.abstractmethod
  def get_noised_sum(self, sample_state, global_state):
    """Gets estimate of sum after all records of sample have been accumulated.

    Args:
      sample_state: The sample state after all records have been accumulated.
      global_state: The global state.

    Returns:
      A tuple (estimate, new_global_state) where "estimate" is the estimated
      sum of the records and "new_global_state" is the updated global state.
    """
    pass

  def get_query_result(self, sample_state, global_state):
    """Delegates to get_noised_sum."""
    return self.get_noised_sum(sample_state, global_state)


class PrivateAverageQuery(PrivateQuery):
  """Interface for differentially private mechanisms to compute an average."""

  @abc.abstractmethod
  def get_noised_average(self, sample_state, global_state):
    """Gets average estimate after all records of sample have been accumulated.

    Args:
      sample_state: The sample state after all records have been accumulated.
      global_state: The global state.

    Returns:
      A tuple (estimate, new_global_state) where "estimate" is the estimated
      average of the records and "new_global_state" is the updated global state.
    """
    pass

  def get_query_result(self, sample_state, global_state):
    """Delegates to get_noised_average."""
    return self.get_noised_average(sample_state, global_state)
