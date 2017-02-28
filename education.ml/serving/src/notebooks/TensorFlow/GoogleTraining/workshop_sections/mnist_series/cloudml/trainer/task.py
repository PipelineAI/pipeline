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
"""Example implementation of code to run on the Cloud ML service.

This file is generic and can be reused by other models without modification.
The only assumption this module has is that there exists model module that
implements create_model() function. The function creates class implementing
problem specific implementations of build_train_graph(), build_eval_graph(),
build_prediction_graph() and format_metric_values().
"""
import argparse
import json
import logging
import os

import util
import model

from tensorflow.contrib import learn
from tensorflow.contrib.learn.python.learn import learn_runner

import tensorflow as tf

tf.logging.set_verbosity(tf.logging.INFO)


def make_experiment_fn(args):
  train_input_fn = util.make_input_fn(
      args.train_data_paths,
      util.parse_examples,
      args.batch_size,
      num_epochs=args.num_epochs
  )
  eval_input_fn = util.make_input_fn(
      args.eval_data_paths,
      util.parse_examples,
      args.batch_size,
      num_epochs=args.num_epochs
  )

  def _experiment_fn(output_dir):
      return learn.Experiment(
          learn.Estimator(
              model_fn=model.make_model_fn(args),
              model_dir=output_dir
          ),
          train_input_fn=train_input_fn,
          eval_input_fn=eval_input_fn,
          train_steps=args.max_steps,
          eval_metrics=model.METRICS,
          continuous_eval_throttle_secs=args.min_eval_seconds,
          min_eval_frequency=args.min_train_eval_rate,
          # Until learn_runner is updated to use train_and_evaluate
          local_eval_frequency=args.min_train_eval_rate
      )
  return _experiment_fn


def main(args):
  env = json.loads(os.environ.get('TF_CONFIG', '{}'))

  # Print the job data as provided by the service.
  logging.info('Original job data: %s', env.get('job', {}))

  # First find out if there's a task value on the environment variable.
  # If there is none or it is empty define a default one.
  task_data = env.get('task', {'type': 'master', 'index': 0})
  trial = task_data.get('trial')
  if trial is not None:
    args.output_path = os.path.join(args.output_path, trial)

  learn_runner.run(make_experiment_fn(args), args.output_path)


def model_arguments(parser):
  """Add model specific args to the parser"""
  group = parser.add_argument_group(
      title='Model Arguments',
      description="""\
      These flags are set by Cloud ML from the hyperparameters defined
      in the API call.  They will be passed in as normal command line flags.
      """
  )
  group.add_argument('--learning-rate', type=float, default=0.01)
  group.add_argument('--hidden1', type=int, default=128)
  group.add_argument('--hidden2', type=int, default=32)
  return group


def path_arguments(parser):
  group = parser.add_argument_group(title='Data Paths Arguments')

  group.add_argument(
      '--train-data-paths',
      type=str,
      required=True,
      nargs='+',
      help='File paths for training. Local or GCS file paths.'
  )
  group.add_argument(
      '--eval-data-paths',
      type=str,
      required=True,
      nargs='+',
      help='File path used for evaluation. Local or GCS file paths.'
  )
  group.add_argument(
      '--output-path',
      type=str,
      required=True,
      help="""\
      The path to which checkpoints and other outputs
      should be saved. This can be either a local or GCS
      path.\
      """
  )
  return group

def termination_arguments(parser):
  group = parser.add_mutually_exclusive_group(required=True)
  group.add_argument(
      '--num-epochs',
      type=int,
      help="""\
      Maximum number of times to run through training/evaluation data
      This should only be set if --max-steps is not set
      """
  )
  group.add_argument(
      '--max-steps',
      type=int,
      help="""\
      Maximum number of steps to take in training
      This should only be set if --num-epochs is not set
      """
  )
  return group

def training_arguments(parser):
  group = parser.add_argument_group('Misc Training Arguments')
  group.add_argument(
      '--batch-size',
      type=int,
      default=64,
      help='Number of examples to be processed per mini-batch.')
  group.add_argument(
      '--min-eval-seconds',
      type=float,
      default=5,
      help="""\
      Minimal interval between calculating evaluation metrics and saving
      evaluation summaries.\
      """
  )
  group.add_argument(
      '--min-train-eval-rate',
      type=int,
      default=20,
      help="""\
      Minimal train / eval time ratio on master:
      The number of steps between
      """
  )
  return group


if __name__ == '__main__':
  """Runs the training loop."""
  parser = argparse.ArgumentParser()
  path_arguments(parser)
  model_arguments(parser)
  termination_arguments(parser)
  training_arguments(parser)
  main(parser.parse_args())
