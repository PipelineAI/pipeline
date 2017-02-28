# Copyright 2016 Google Inc. All Rights Reserved. Licensed under the Apache
# License, Version 2.0 (the "License"); you may not use this file except in
# compliance with the License. You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import time

import tensorflow as tf

tf.logging.set_verbosity(tf.logging.ERROR)

CATEGORICAL_COLUMNS = ["workclass", "education", "marital_status",
                       "occupation", "relationship", "race", "gender",
                       "native_country"]

COLUMNS = ["age", "workclass", "fnlwgt", "education", "education_num",
           "marital_status", "occupation", "relationship", "race", "gender",
           "capital_gain", "capital_loss", "hours_per_week", "native_country",
           "income_bracket"]


def build_estimator(model_dir):
  """Build an estimator."""
  # Sparse base columns.
  gender = tf.contrib.layers.sparse_column_with_keys(column_name="gender",
                                                     keys=["female", "male"])
  race = tf.contrib.layers.sparse_column_with_keys(column_name="race",
                                                   keys=["Amer-Indian-Eskimo",
                                                         "Asian-Pac-Islander",
                                                         "Black", "Other",
                                                         "White"])

  education = tf.contrib.layers.sparse_column_with_hash_bucket(
      "education", hash_bucket_size=1000)
  marital_status = tf.contrib.layers.sparse_column_with_hash_bucket(
      "marital_status", hash_bucket_size=100)
  relationship = tf.contrib.layers.sparse_column_with_hash_bucket(
      "relationship", hash_bucket_size=100)
  workclass = tf.contrib.layers.sparse_column_with_hash_bucket(
      "workclass", hash_bucket_size=100)
  occupation = tf.contrib.layers.sparse_column_with_hash_bucket(
      "occupation", hash_bucket_size=1000)
  native_country = tf.contrib.layers.sparse_column_with_hash_bucket(
      "native_country", hash_bucket_size=1000)

  # Continuous base columns.
  age = tf.contrib.layers.real_valued_column("age")
  education_num = tf.contrib.layers.real_valued_column("education_num")
  capital_gain = tf.contrib.layers.real_valued_column("capital_gain")
  capital_loss = tf.contrib.layers.real_valued_column("capital_loss")
  hours_per_week = tf.contrib.layers.real_valued_column("hours_per_week")

  # Transformations.
  age_buckets = tf.contrib.layers.bucketized_column(
      age, boundaries=[18, 25, 30, 35, 40, 45, 50, 55, 60, 65])
  education_occupation = tf.contrib.layers.crossed_column(
      [education, occupation], hash_bucket_size=int(1e4))
  age_race_occupation = tf.contrib.layers.crossed_column(
      [age_buckets, race, occupation], hash_bucket_size=int(1e6))
  country_occupation = tf.contrib.layers.crossed_column(
      [native_country, occupation], hash_bucket_size=int(1e4))

  # Wide columns and deep columns.
  wide_columns = [gender, native_country, education, occupation, workclass,
                  marital_status, relationship, age_buckets,
                  education_occupation, age_race_occupation,
                  country_occupation]

  deep_columns = [
      tf.contrib.layers.embedding_column(workclass, dimension=8),
      tf.contrib.layers.embedding_column(education, dimension=8),
      tf.contrib.layers.embedding_column(marital_status,
                                         dimension=8),
      tf.contrib.layers.embedding_column(gender, dimension=8),
      tf.contrib.layers.embedding_column(relationship, dimension=8),
      tf.contrib.layers.embedding_column(race, dimension=8),
      tf.contrib.layers.embedding_column(native_country,
                                         dimension=8),
      tf.contrib.layers.embedding_column(occupation, dimension=8),
      age,
      education_num,
      capital_gain,
      capital_loss,
      hours_per_week,
  ]

  # m = tf.contrib.learn.LinearClassifier(
  #   model_dir=model_dir, feature_columns=wide_columns)

  #  m = tf.contrib.learn.DNNClassifier(
  #         model_dir=model_dir,
  #         feature_columns=deep_columns,
  #         hidden_units=[100, 50])

  m = tf.contrib.learn.DNNLinearCombinedClassifier(
      model_dir=model_dir,
      linear_feature_columns=wide_columns,
      dnn_feature_columns=deep_columns,
      dnn_hidden_units=[100, 70, 50, 25])

  return m


def generate_input_fn(filename):
  def _input_fn():
    BATCH_SIZE = 40
    filename_queue = tf.train.string_input_producer([filename])
    reader = tf.TextLineReader()
    key, value = reader.read_up_to(filename_queue, num_records=BATCH_SIZE)

    record_defaults = [[0], [" "], [0], [" "], [0],
                       [" "], [" "], [" "], [" "], [" "],
                       [0], [0], [0], [" "], [" "]]
    columns = tf.decode_csv(
        value, record_defaults=record_defaults)

    features, income_bracket = dict(zip(COLUMNS, columns[:-1])), columns[-1]

    # remove the fnlwgt key, which is not used
    features.pop('fnlwgt', 'fnlwgt key not found')

    # works in 0.12 only
    for feature_name in CATEGORICAL_COLUMNS:
      features[feature_name] = tf.expand_dims(features[feature_name], -1)

    income_int = tf.to_int32(tf.equal(income_bracket, " >50K"))

    return features, income_int

  return _input_fn


def train_and_eval():

  train_file = "gs://tf-ml-workshop/widendeep/adult.data"
  test_file = "gs://tf-ml-workshop/widendeep/adult.test"
  train_steps = 1000

  model_dir = 'models/model_' + str(int(time.time()))
  print("model directory = %s" % model_dir)

  m = build_estimator(model_dir)
  print('estimator built')

  m.fit(input_fn=generate_input_fn(train_file), steps=train_steps)
  print('fit done')

  results = m.evaluate(input_fn=generate_input_fn(test_file), steps=1)
  print('evaluate done')

  print('Accuracy: %s' % results['accuracy'])


if __name__ == "__main__":
  print("TensorFlow version %s" % (tf.__version__))
  if tf.__version__ < '0.12.0':
    raise ValueError('This code requires tensorflow >= 0.12.0: See bug https://github.com/tensorflow/tensorflow/issues/5886')

  train_and_eval()
