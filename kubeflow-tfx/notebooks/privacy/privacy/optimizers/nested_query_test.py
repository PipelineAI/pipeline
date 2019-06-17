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

"""Tests for NestedQuery."""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function


from absl.testing import parameterized
import numpy as np
import tensorflow as tf

from privacy.optimizers import gaussian_query
from privacy.optimizers import nested_query

nest = tf.contrib.framework.nest

_basic_query = gaussian_query.GaussianSumQuery(1.0, 0.0)

try:
    xrange
except NameError:
    xrange = range


def _run_query(query, records):
  """Executes query on the given set of records as a single sample.

  Args:
    query: A PrivateQuery to run.
    records: An iterable containing records to pass to the query.

  Returns:
    The result of the query.
  """
  global_state = query.initial_global_state()
  params = query.derive_sample_params(global_state)
  sample_state = query.initial_sample_state(global_state, next(iter(records)))
  for record in records:
    sample_state = query.accumulate_record(params, sample_state, record)
  result, _ = query.get_query_result(sample_state, global_state)
  return result


class NestedQueryTest(tf.test.TestCase, parameterized.TestCase):

  def test_nested_gaussian_sum_no_clip_no_noise(self):
    with self.cached_session() as sess:
      query1 = gaussian_query.GaussianSumQuery(
          l2_norm_clip=10.0, stddev=0.0)
      query2 = gaussian_query.GaussianSumQuery(
          l2_norm_clip=10.0, stddev=0.0)

      query = nested_query.NestedQuery([query1, query2])

      record1 = [1.0, [2.0, 3.0]]
      record2 = [4.0, [3.0, 2.0]]

      query_result = _run_query(query, [record1, record2])
      result = sess.run(query_result)
      expected = [5.0, [5.0, 5.0]]
      self.assertAllClose(result, expected)

  def test_nested_gaussian_average_no_clip_no_noise(self):
    with self.cached_session() as sess:
      query1 = gaussian_query.GaussianAverageQuery(
          l2_norm_clip=10.0, sum_stddev=0.0, denominator=5.0)
      query2 = gaussian_query.GaussianAverageQuery(
          l2_norm_clip=10.0, sum_stddev=0.0, denominator=5.0)

      query = nested_query.NestedQuery([query1, query2])

      record1 = [1.0, [2.0, 3.0]]
      record2 = [4.0, [3.0, 2.0]]

      query_result = _run_query(query, [record1, record2])
      result = sess.run(query_result)
      expected = [1.0, [1.0, 1.0]]
      self.assertAllClose(result, expected)

  def test_nested_gaussian_average_with_clip_no_noise(self):
    with self.cached_session() as sess:
      query1 = gaussian_query.GaussianAverageQuery(
          l2_norm_clip=4.0, sum_stddev=0.0, denominator=5.0)
      query2 = gaussian_query.GaussianAverageQuery(
          l2_norm_clip=5.0, sum_stddev=0.0, denominator=5.0)

      query = nested_query.NestedQuery([query1, query2])

      record1 = [1.0, [12.0, 9.0]]  # Clipped to [1.0, [4.0, 3.0]]
      record2 = [5.0, [1.0, 2.0]]   # Clipped to [4.0, [1.0, 2.0]]

      query_result = _run_query(query, [record1, record2])
      result = sess.run(query_result)
      expected = [1.0, [1.0, 1.0]]
      self.assertAllClose(result, expected)

  def test_complex_nested_query(self):
    with self.cached_session() as sess:
      query_ab = gaussian_query.GaussianSumQuery(
          l2_norm_clip=1.0, stddev=0.0)
      query_c = gaussian_query.GaussianAverageQuery(
          l2_norm_clip=10.0, sum_stddev=0.0, denominator=2.0)
      query_d = gaussian_query.GaussianSumQuery(
          l2_norm_clip=10.0, stddev=0.0)

      query = nested_query.NestedQuery(
          [query_ab, {'c': query_c, 'd': [query_d]}])

      record1 = [{'a': 0.0, 'b': 2.71828}, {'c': (-4.0, 6.0), 'd': [-4.0]}]
      record2 = [{'a': 3.14159, 'b': 0.0}, {'c': (6.0, -4.0), 'd': [5.0]}]

      query_result = _run_query(query, [record1, record2])
      result = sess.run(query_result)
      expected = [{'a': 1.0, 'b': 1.0}, {'c': (1.0, 1.0), 'd': [1.0]}]
      self.assertAllClose(result, expected)

  def test_nested_query_with_noise(self):
    with self.cached_session() as sess:
      sum_stddev = 2.71828
      denominator = 3.14159

      query1 = gaussian_query.GaussianSumQuery(
          l2_norm_clip=1.5, stddev=sum_stddev)
      query2 = gaussian_query.GaussianAverageQuery(
          l2_norm_clip=0.5, sum_stddev=sum_stddev, denominator=denominator)
      query = nested_query.NestedQuery((query1, query2))

      record1 = (3.0, [2.0, 1.5])
      record2 = (0.0, [-1.0, -3.5])

      query_result = _run_query(query, [record1, record2])

      noised_averages = []
      for _ in xrange(1000):
        noised_averages.append(nest.flatten(sess.run(query_result)))

      result_stddev = np.std(noised_averages, 0)
      avg_stddev = sum_stddev / denominator
      expected_stddev = [sum_stddev, avg_stddev, avg_stddev]
      self.assertArrayNear(result_stddev, expected_stddev, 0.1)

  @parameterized.named_parameters(
      ('type_mismatch', [_basic_query], (1.0,), TypeError),
      ('too_many_queries', [_basic_query, _basic_query], [1.0], ValueError),
      ('too_many_records', [_basic_query, _basic_query],
       [1.0, 2.0, 3.0], ValueError),
      ('query_too_deep', [_basic_query, [_basic_query]], [1.0, 1.0], TypeError))
  def test_record_incompatible_with_query(
      self, queries, record, error_type):
    with self.assertRaises(error_type):
      _run_query(nested_query.NestedQuery(queries), [record])


if __name__ == '__main__':
  tf.test.main()
