# Copyright 2016 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the 'License');
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an 'AS IS' BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ==============================================================================
from __future__ import division
from __future__ import print_function

import argparse
import json
import os

import tensorflow as tf
from tensorflow.contrib.learn import Estimator, Experiment
from tensorflow.contrib.learn.python.learn import learn_runner

import model
import util


tf.logging.set_verbosity(tf.logging.INFO)


def make_experiment_fn(args):
  train_input_fn = util.make_input_fn(
      args.train_data_file,
      args.batch_size,
      args.num_skips,
      args.skip_window,
      args.vocab_size,
      num_epochs=args.num_epochs
  )
  eval_input_fn = util.make_input_fn(
      args.eval_data_file,
      args.batch_size,
      args.num_skips,
      args.skip_window,
      args.vocab_size,
      num_epochs=args.num_epochs
  )

  def experiment_fn(output_dir):
    return Experiment(
        Estimator(
            model_fn=model.make_model_fn(**args.__dict__),
            model_dir=output_dir
        ),
        train_input_fn=train_input_fn,
        eval_input_fn=eval_input_fn,
        continuous_eval_throttle_secs=args.min_eval_seconds,
        min_eval_frequency=args.min_train_eval_rate,
        # Until Experiment moves to train_and_evaluate call internally
        local_eval_frequency=args.min_train_eval_rate
    )
  return experiment_fn

def model_args(parser):
  group = parser.add_argument_group(title='Model Arguments')
  group.add_argument('--reference-words', nargs='*', type=str)
  group.add_argument('--num-partitions', default=1, type=int)
  group.add_argument('--embedding-size', default=128, type=int)
  group.add_argument('--vocab-size', default=2 ** 15, type=int)
  group.add_argument('--num-sim', default=8, type=int)
  group.add_argument('--num-sampled', default=64, type=int)
  group.add_argument('--num-skips', default=4, type=int)
  group.add_argument('--skip-window', default=8, type=int)
  group.add_argument('--learning-rate', default=0.1, type=float)
  group.add_argument(
      '--vocab-file',
      required=True,
      help='Path to a TSV file containing the vocab index'
  )
  return group


if __name__ == '__main__':
  parser = argparse.ArgumentParser()
  parser.add_argument(
      '--train-data-file',
      help='Binary files for training',
      type=str
  )
  parser.add_argument(
      '--output-path',
      help='GCS path to output files',
      required=True
  )
  parser.add_argument(
      '--eval-data-file',
      help='Binary files for evaluation',
      type=str
  )
  parser.add_argument(
      '--batch-size',
      help="""\
      Batch size for skipgrams. Note that batch size may be approximate.
      Actual batch_size is (batch_size // num_skips) * num_skips.\
      """,
      type=int,
      default=512
  )
  parser.add_argument(
      '--num-epochs',
      help='Number of epochs for training',
      type=int,
      default=1
  )
  parser.add_argument(
      '--min-eval-seconds',
      type=float,
      default=5,
      help="""\
      Minimal interval between calculating evaluation metrics and saving
      evaluation summaries.\
      """
  )
  parser.add_argument(
      '--min-train-eval-rate',
      type=int,
      default=20,
      help="""\
      Minimal train / eval time ratio on master:
      The number of steps between evaluations
      """
  )
  model_args(parser)
  args = parser.parse_args()
  # Extend output path with trial ID if it exists
  args.output_path = os.path.join(
      args.output_path,
      json.loads(
          os.environ.get('TF_CONFIG', '{}')
      ).get('task', {}).get('trial', '')
  )
  learn_runner.run(make_experiment_fn(args), args.output_path)
