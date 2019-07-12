# Copyright 2019 Google LLC. All Rights Reserved.
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
"""Utils to query a TFX pipeline's ml-metadata store in a notebook."""

import os
import time

#import papermill as pm
import tensorflow_data_validation as tfdv
import tensorflow_model_analysis as tfma
import utils

from ml_metadata.metadata_store import metadata_store
from ml_metadata.proto import metadata_store_pb2


class TFXArtifactTypes(object):
  """Constants for different TFX artifact type names."""
  EXAMPLES = 'ExamplesPath'
  SCHEMA = 'SchemaPath'
  EXAMPLE_STATS = 'ExampleStatisticsPath'
  EXAMPLE_VALIDATION = 'ExampleValidationPath'
  TRANSFORMED_EXAMPLES = 'TransformPath'
  MODEL = 'ModelExportPath'
  MODEL_EVAL = 'ModelEvalPath'


class TFXExecutionTypes(object):
  """Constants for different TFX execution type names."""
  EXAMPLE_GEN = 'examples_gen'
  STATISTICS_GEN = 'statistics_gen'
  SCHEMA_GEN = 'schema_gen'
  EXAMPLE_VALIDATION = 'example_validation'
  TRANSFORM = 'transform'
  TRAINER = 'trainer'
  EVALUATOR = 'evaluator'


class TFXReadonlyMetadataStore(utils.ReadonlyMetadataStore):
  """A TFX ml-metadata store that provides read-only methods for notebooks."""

  @staticmethod
  def from_sqlite_db(filename_uri):
    """Returns a `TFXReadonlyMetadataStore` based off a SQLITE db uri.

    Args:
      filename_uri: A `str` indicating the path to the SQLITE db.

    Returns:
      A `TFXReadonlyMetadataStore` based off a SQLITE db uri.
    """
    c = metadata_store_pb2.ConnectionConfig()
    c.sqlite.filename_uri = filename_uri
    return TFXReadonlyMetadataStore(metadata_store.MetadataStore(c))

  def display_tfma_analysis(self, model_id, slicing_column=None):
    """Displays TFMA metrics for `model_id` sliced by `slicing_column`.

    Args:
      model_id: A `int` indicating the id of a `TFXArtifactTypes.MODEL` artifact
      slicing_column: (Optional) A `str` indicating the slicing column for
          the TFMA metrics.

    Returns:
      A SlicingMetricsViewer object if in Jupyter notebook; None if in Colab.
    """
    tfma_artifact = self.get_dest_artifact_of_type(
        model_id, TFXArtifactTypes.MODEL_EVAL)
    if tfma_artifact:
      return tfma.view.render_slicing_metrics(
          tfma.load_eval_result(tfma_artifact.uri),
          slicing_column=slicing_column)

  def compare_tfma_analysis(self, model_id, other_model_id):
    """Compares TFMA metrics for `model_id` and `other_model_id`.

    Args:
      model_id: A `int` indicating the id of a `TFXArtifactTypes.MODEL` artifact
      other_model_id: A `int` indicating the id of another
          `TFXArtifactTypes.MODEL` artifact.

    Returns:
      A TimeSeriesViewer object if in Jupyter notebook; None if in Colab.
    """
    tfma_artifact, other_tfma_artifact = (
        self.get_dest_artifact_of_type(model_id, TFXArtifactTypes.MODEL_EVAL),
        self.get_dest_artifact_of_type(other_model_id,
                                       TFXArtifactTypes.MODEL_EVAL)
    )
    if tfma_artifact and other_tfma_artifact:
      eval_results = tfma.make_eval_results(
          [
              tfma.load_eval_result(tfma_artifact.uri),
              tfma.load_eval_result(other_tfma_artifact.uri)
          ], tfma.constants.MODEL_CENTRIC_MODE)
      return tfma.view.render_time_series(
          eval_results, tfma.slicer.slicer.SingleSliceSpec())

  def display_stats_for_examples(self, examples_id):
    """Displays stats for `examples_id`.

    Args:
      examples_id: A `int` indicating the id of a `TFXArtifactTypes.EXAMPLES`
          artifact.
    """
    stats_artifact = self.get_dest_artifact_of_type(
        examples_id, TFXArtifactTypes.EXAMPLE_STATS)
    if stats_artifact:
      tfdv.visualize_statistics(
          tfdv.load_statistics(os.path.join(stats_artifact.uri,
                                            'stats_tfrecord')))

  def compare_stats_for_examples(self, examples_id, other_examples_id,
                                 name='', other_name=''):
    """Compares stats for `examples_id` and `other_examples_id`.

    Args:
      examples_id: A `int` indicating the id of one `TFXArtifactTypes.EXAMPLES`
          artifact.
      other_examples_id: A `int` indicating the id of another
          `TFXArtifactTypes.EXAMPLES` artifact.
      name: (Optional) A `str` indicating the label to use for stats of
          `examples_id`.
      other_name: (Optional) A `str` indicating the label to use for stats of
          `other_examples_id`.
    """
    stats_artifact, other_stats_artifact = (
        self.get_dest_artifact_of_type(
            examples_id, TFXArtifactTypes.EXAMPLE_STATS),
        self.get_dest_artifact_of_type(
            other_examples_id, TFXArtifactTypes.EXAMPLE_STATS)
    )
    if stats_artifact and other_stats_artifact:
      tfdv.visualize_statistics(
          tfdv.load_statistics(stats_artifact.uri),
          rhs_statistics=tfdv.load_statistics(other_stats_artifact.uri),
          lhs_name=name, rhs_name=other_name)

  def display_examples_stats_for_model(self, model_id):
    """Displays stats for examples used to train `model_id`."""
    examples_artifact = self.get_source_artifact_of_type(
        model_id, TFXArtifactTypes.EXAMPLES)
    if examples_artifact:
      self.display_stats_for_examples(examples_artifact.id)

  def compare_examples_stats_for_models(self, model_id, other_model_id):
    """Compares stats for examples to train `model_id` & `other_model_id`."""
    examples_artifact, other_examples_artifact = (
        self.get_source_artifact_of_type(model_id, TFXArtifactTypes.EXAMPLES),
        self.get_source_artifact_of_type(
            other_model_id, TFXArtifactTypes.EXAMPLES)
    )
    if examples_artifact and other_examples_artifact:
      self.compare_stats_for_examples(
          examples_artifact.id, other_examples_artifact.id,
          name='model_'+str(model_id), other_name='model_'+str(other_model_id))

  def display_tensorboard(self, model_id, *other_model_ids):
    """Returns a Tensorboard link for `model_id` and `other_model_ids`.

    Args:
      model_id: A `int` indicating the id of a `TFXArtifactTypes.MODEL`
          artifact.
      *other_model_ids: (Optional) A list of `int` indicating the ids of other
          `TFXArtifactTypes.MODEL` artifacts to also include in the Tensorboard
          invocation for comparison.
    """
    model_ids = [model_id] + list(other_model_ids)
    model_artifacts = self.metadata_store.get_artifacts_by_id(model_ids)
    model_ids_str = '-'.join([str(m) for m in model_ids])
    log_file = os.path.join(
        os.environ['HOME'],
        'tensorboard_model_{}_log.txt'.format(model_ids_str),
    )
    output_notebook_path = os.path.join(
        os.environ['HOME'],
        'spawn_tensorboard_{}_output.ipynb'.format(model_ids_str),
    )
    tensorboard_logdir = ','.join([
        'model_{}:{}'.format(m.id, m.uri) for m in model_artifacts
    ])
    pm.execute_notebook(
        'spawn_tensorboard.ipynb',
        output_notebook_path,
        parameters=dict(tb_logdir=tensorboard_logdir, tb_run_log=log_file),
        progress_bar=False)
    time.sleep(5)  # Give it some time for log_filename to be flushed.
    with open(log_file) as f:
      for l in f:
        if 'TensorBoard' in l:
          # "TensorBoard 1.12.2 at http://... (Press CTRL+C to quit)"
          return l.split(' ')[3]
