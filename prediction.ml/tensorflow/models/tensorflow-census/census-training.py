# Copyright 2016 The TensorFlow Authors. All Rights Reserved.
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
"""Example code for TensorFlow Wide & Deep Tutorial using TF.Learn API."""
from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import tempfile
from six.moves import urllib

import pandas as pd
import tensorflow as tf
import json

flags = tf.app.flags
FLAGS = flags.FLAGS

flags.DEFINE_string("model_dir", "", "Base directory for output models.")
flags.DEFINE_string("model_type", "wide_n_deep",
                    "Valid model types: {'wide', 'deep', 'wide_n_deep'}.")
flags.DEFINE_integer("train_steps", 200, "Number of training steps.")
flags.DEFINE_string(
    "train_data",
    "",
    "Path to the training data.")
flags.DEFINE_string(
    "test_data",
    "",
    "Path to the test data.")

COLUMNS = ["age", "workclass", "fnlwgt", "education", "education_num",
           "marital_status", "occupation", "relationship", "race", "sex",
           "capital_gain", "capital_loss", "hours_per_week", "native_country",
           "income"]
LABEL_COLUMN = "label"
CATEGORICAL_COLUMNS = ["workclass", "education", "marital_status", "occupation",
                       "relationship", "race", "sex", "native_country"]
CONTINUOUS_COLUMNS = ["age", "education_num", "capital_gain", "capital_loss",
                      "hours_per_week"]


def get_or_create_model(model_dir):
  """ Get or create model."""
  # Sparse base columns.
  sex = tf.contrib.layers.sparse_column_with_keys(column_name="sex",
                                                     keys=["female", "male"])

  # Occupation One-Hot:
  # ?
  # Federal-gov
  # Local-gov
  # Private
  # Self-emp-not-inc
  # Self-emp-inc
  # State-gov
  # Without-pay
  # Never-worked

  education = tf.contrib.layers.sparse_column_with_hash_bucket(
      "education", hash_bucket_size=1000)
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

  # Wide columns and deep columns.
  wide_columns = [sex, education, relationship, workclass, occupation, native_country, age, 
                  education_num, capital_gain, capital_loss, hours_per_week]

  deep_columns = [
      tf.contrib.layers.embedding_column(workclass, dimension=8),
      tf.contrib.layers.embedding_column(education, dimension=8),
      tf.contrib.layers.embedding_column(sex, dimension=8),
      tf.contrib.layers.embedding_column(relationship, dimension=8),
      tf.contrib.layers.embedding_column(native_country,
                                         dimension=8),
      tf.contrib.layers.embedding_column(occupation, dimension=8),
      age,
      education_num,
      capital_gain,
      capital_loss,
      hours_per_week,
  ]

  if FLAGS.model_type == "wide":
    # Uses Ftrl Optimizer by default
    model = tf.contrib.learn.LinearClassifier(model_dir=model_dir,
                                              feature_columns=wide_columns,
                                              optimizer=tf.train.FtrlOptimizer(
                                                learning_rate=0.1,
                                                l1_regularization_strength=1.0,
                                                l2_regularization_strength=1.0),
                                              enable_centered_bias=True) # Not compatible with SDCAOptimizer
  elif FLAGS.model_type == "deep":
    model = tf.contrib.learn.DNNClassifier(model_dir=model_dir,
                                       feature_columns=deep_columns,
                                       hidden_units=[100, 50])
  else:
    model = tf.contrib.learn.DNNLinearCombinedClassifier(
        model_dir=model_dir,
        linear_feature_columns=wide_columns,
        dnn_feature_columns=deep_columns,
        dnn_hidden_units=[100, 50])
  return model


def input_fn(df):
  """Input builder function."""

  # Creates a dictionary mapping from each continuous feature column name (k) to
  # the values of that column stored in a constant Tensor.
  continuous_cols = {k: tf.constant(df[k].values) for k in CONTINUOUS_COLUMNS}

  # Creates a dictionary mapping from each categorical feature column name (k)
  # to the values of that column stored in a tf.SparseTensor.
  categorical_cols = {k: tf.SparseTensor(
      indices=[[i, 0] for i in range(df[k].size)],
      values=df[k].values,
      shape=[df[k].size, 1]) for k in CATEGORICAL_COLUMNS}

  # Merges the two dictionaries into one.
  feature_cols = dict(continuous_cols)
  feature_cols.update(categorical_cols)

  # Converts the label column into a constant Tensor.

  # Handle the predict_proba() case where no label is provided
  if (len(df.get(LABEL_COLUMN, [])) > 0):
    label = tf.constant(df[LABEL_COLUMN].values)
  else:
    label = None 
 
 # Returns the feature columns and the label.
  return feature_cols, label


def train(model, df):
  """Train the model."""
  # This is a fancy way of converting "<=50K" -> 0 and ">50K" -> 1
  df[LABEL_COLUMN] = (
      df["income"].apply(lambda x: ">50K" in x)).astype(int)

  # TODO:  Investigate partial_fit() for streaming use case
  model.fit(input_fn=lambda: input_fn(df), steps=FLAGS.train_steps)

  return model


def evaluate(model, df):
  """Evaluate the model with test data"""
  # This is a fancy way of converting "<=50K" -> 0 and ">50K" -> 1
  df[LABEL_COLUMN] = (
      df["income"].apply(lambda x: ">50K" in x)).astype(int)

  results = model.evaluate(input_fn=lambda: input_fn(df), steps=1)

  for key in sorted(results):
    print("%s: %s" % (key, results[key]))

  return


def predict_proba(model, df):
  """Predict on new data"""

  # TODO:  Preserve the natural key from the input (if there is one)
  results = model.predict_proba(input_fn=lambda: input_fn(df))

  # TODO:  Convert the result back to categorical ["<=50k", ">50k"]
  # results = results.apply(lambda result: ">50K" if result else "<=50K") 

  return results


def main(_):
  ######################################
  # Create Model
  ######################################
  model_dir = tempfile.mkdtemp() if not FLAGS.model_dir else FLAGS.model_dir
  print("model directory = %s" % model_dir)

  checkpoint_dir = "%s/checkpoint" % model_dir
  export_dir = "%s/export" % model_dir

  model = get_or_create_model(model_dir=checkpoint_dir)


  ######################################
  # Train/Fit Model
  ######################################

  train_file_name = FLAGS.train_data
 
  # TODO:  Read from S3
  #   Reference:
  #     http://pandas.pydata.org/pandas-docs/stable/generated/pandas.read_csv.html#pandas-read-csv
  df_train = pd.read_csv(
      tf.gfile.Open(train_file_name),
      names=COLUMNS,
      skipinitialspace=True,
      engine="python")

  # remove NaN elements
  df_train = df_train.dropna(how='any', axis=0)

  trained_model = train(model, df_train)

  print(trained_model)


  ######################################
  # Evaluate the Model
  ######################################

  test_file_name = FLAGS.test_data

  # TODO:  Read from S3
  #   Reference:
  #     http://pandas.pydata.org/pandas-docs/stable/generated/pandas.read_csv.html#pandas-read-csv
  df_test = pd.read_csv(
      tf.gfile.Open(test_file_name),
      names=COLUMNS,
      skipinitialspace=True,
      skiprows=1,
      engine="python")

  df_test = df_test.dropna(how='any', axis=0)

  evaluate(trained_model, df_test)


  ######################################
  # Export Model for Serving
  ######################################
  trained_model.export(export_dir)


  ######################################
  # Predict from the Model             
  ######################################

  # 25, Private, 226802, 11th, 7, Never-married, Machine-op-inspct, Own-child, Black, Male, 0, 0, 40, United-States, <=50K.
  # 38, Private, 89814, HS-grad, 9, Married-civ-spouse, Farming-fishing, Husband, White, Male, 0, 0, 50, United-States, <=50K.
  # 28, Local-gov, 336951, Assoc-acdm, 12, Married-civ-spouse, Protective-serv, Husband, White, Male, 0, 0, 40, United-States, >50K.
  # 43, Private, 346189, Masters, 14, Married-civ-spouse, Exec-managerial, Husband, White, Male, 0, 0, 50, United-States, >50K.

  predict_json_str = '[ \
            { \
             "age":25, "workclass":"Private", "fnlwgt":226802, "education":"11th", "education_num":7, \
             "marital_status":"Never-married", "occupation":"Machine-op-inspct", "relationship":"Own-child", \
             "race":"Black", "sex":"Male","capital_gain":0, "capital_loss":0, "hours_per_week":40, "native_country":"United-States", \
             "income":"-1" \
            }, \
            { \
             "age":38, "workclass":"Private", "fnlwgt":89814, "education":"HS-grad", "education_num":9, \
             "marital_status":"Married-civ-spouse", "occupation":"Farming-fishing", "relationship":"Husband", \
             "race":"White", "sex":"Male", "capital_gain":0, "capital_loss":0, "hours_per_week":50, "native_country":"United-States", \
             "income":"-1" \
            }, \
            { \
             "age":28, "workclass":"Local-gov", "fnlwgt":336951, "education":"Assoc-acdm", "education_num":12, \
             "marital_status":"Married-civ-spouse", "occupation":"Protective-serv", "relationship":"Husband", \
             "race":"White", "sex":"Male", "capital_gain":0, "capital_loss":0, "hours_per_week":40, "native_country":"United-States", \
             "income":"-1" \
            }, \
            { \
             "age":43, "workclass":"Private", "fnlwgt":346189, "education":"Masters", "education_num":14, \
             "marital_status":"Married-civ-spouse", "occupation":"Exec-managerial", "relationship":"Husband", \
             "race":"White", "sex":"Male", "capital_gain":0, "capital_loss":0, "hours_per_week":50, "native_country":"United-States", \
             "income":"-1" \
            } \
           ]'
  # TODO:  Load from S3
  #   Reference:
  #     http://pandas.pydata.org/pandas-docs/stable/generated/pandas.read_json.html#pandas-read-json
  df_predict = pd.read_json(predict_json_str)

  df_predict = df_predict.dropna(how='any', axis=0)

  predictions = predict_proba(trained_model, df_predict)

  print("Predictions:\n{}".format(str(predictions)))


  ########################################
  # Restore Model + Parameters/Variables
  ########################################

  # Restore the model + parameters/variables from --model_dir
  #   Note:  TensorFlowEstimate.restore() doesn't seem to be working per 
  #     https://github.com/tensorflow/tensorflow/issues/3855
  restored_trained_model = get_or_create_model(model_dir=checkpoint_dir) 

  # Predict using the restored model
  restored_predictions = predict_proba(restored_trained_model, df_predict)
  print("Restored Predictions:\n{}".format(str(restored_predictions)))


  ########################################
  # TODO:  Inspect Checkpoint Files!
  ########################################

  # TODO:  inspect_checkpoint.py:  print_tensors_in_checkpoint_file()
  #  Reference:
  #    https://raw.githubusercontent.com/tensorflow/tensorflow/master/tensorflow/python/tools/inspect_checkpoint.py

  ########################################
  # TODO:  Tensorboard!
  ########################################
  
  # TODO:  https://github.com/tensorflow/tensorflow/blob/master/tensorflow/contrib/learn/python/learn/README.md#summaries
  #  tensorboard --logdir=<model_dir>

if __name__ == "__main__":
  tf.app.run()
