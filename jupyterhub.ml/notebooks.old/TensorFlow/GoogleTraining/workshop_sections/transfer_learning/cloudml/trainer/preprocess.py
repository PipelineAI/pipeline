# Copyright 2016 Google Inc. All Rights Reserved.
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
"""Example dataflow pipeline for preparing image training data.

The tool requires two main input files:

'input' - URI to csv file, using format:
gs://image_uri1,labela,labelb,labelc
gs://image_uri2,labela,labeld
...

'input_dict' - URI to a text file listing all labels (one label per line):
labela
labelb
labelc

The output data is in format accepted by Cloud ML framework.

This tool produces outputs as follows.
It creates one training example per each line of the created csv file.
When processing CSV file:
- all labels that are not present in input_dict are skipped

To execute this pipeline locally using default options, run this script
with no arguments. To execute on cloud pass single argument --cloud.

To execute this pipeline on the cloud using the Dataflow service and
non-default options:
python -E preprocess.py \
--input_path=PATH_TO_INPUT_CSV_FILE \
--input_dict=PATH_TO_INPUT_DIC_TXT_FILE \
--output_path=YOUR_OUTPUT_PATH \
--cloud

For other flags, see PrepareImagesOptions() bellow.

To run this pipeline locally run the above command without --cloud.

"""
import argparse
import csv
import datetime
import io
import logging
import os
import subprocess
import sys

import apache_beam as beam
from apache_beam.utils.options import PipelineOptions
from google.cloud.ml.io import SaveFeatures
from PIL import Image

import tensorflow as tf
from tensorflow.contrib.slim.python.slim.nets import inception_v3 as inception
from tensorflow.python.framework import errors
from tensorflow.python.lib.io import file_io

slim = tf.contrib.slim

error_count = beam.Aggregator('errorCount')
missing_label_count = beam.Aggregator('missingLabelCount')
csv_rows_count = beam.Aggregator('csvRowsCount')
labels_count = beam.Aggregator('labelsCount')
labels_without_ids = beam.Aggregator('labelsWithoutIds')
existing_file = beam.Aggregator('existingFile')
non_existing_file = beam.Aggregator('nonExistingFile')
skipped_empty_line = beam.Aggregator('skippedEmptyLine')
embedding_good = beam.Aggregator('embedding_good')
embedding_bad = beam.Aggregator('embedding_bad')
incompatible_image = beam.Aggregator('incompatible_image')
invalid_uri = beam.Aggregator('invalid_file_name')
ignored_unlabeled_image = beam.Aggregator('ignored_unlabeled_image')


class Default(object):
  """Default values of variables."""
  FORMAT = 'jpeg'

  # Make sure to update the default checkpoint file if using another
  # inception graph or when a newer checkpoint file is available. See
  # https://research.googleblog.com/2016/08/improving-inception-and-image.html
  IMAGE_GRAPH_CHECKPOINT_URI = (
      'gs://cloud-ml-data/img/flower_photos/inception_v3_2016_08_28.ckpt')
  # The latest CloudML package.
  CML_PACKAGE = 'gs://cloud-ml/sdk/cloudml.latest.tar.gz'


class ExtractLabelIdsDoFn(beam.DoFn):
  """Extracts (uri, label_ids) tuples from CSV rows.
  """

  def start_bundle(self, context, *unused_args, **unused_kwargs):
    self.label_to_id_map = {}

  def process(self, context, all_labels):
    if not self.label_to_id_map:
      for i, label in enumerate(all_labels):
        label = label.strip()
        if label:
          self.label_to_id_map[label] = i

    # Row format is:
    # image_uri(,label_ids)*
    row = context.element
    if not row:
      context.aggregate_to(skipped_empty_line, 1)
      return

    context.aggregate_to(csv_rows_count, 1)
    uri = row[0]
    if not uri or not uri.startswith('gs://'):
      context.aggregate_to(invalid_uri, 1)
      return

    # In a real-world system, you may want to provide a default id for labels
    # that were not in the dictionary.  In this sample, we will throw an error.
    # This code already supports multi-label problems if you want to use it.
    label_ids = [self.label_to_id_map[label.strip()] for label in row[1:]]
    context.aggregate_to(labels_count, len(label_ids))

    if not label_ids:
      context.aggregate_to(ignored_unlabeled_image, 1)
    yield row[0], label_ids


class ReadImageAndConvertToJpegDoFn(beam.DoFn):
  """Read files from GCS and convert images to JPEG format.

  We do this even for JPEG images to remove variations such as different number
  of channels.
  """

  def process(self, context):
    uri, label_ids = context.element

    try:
      with file_io.FileIO(uri, mode='r') as f:
        image_bytes = f.read()
        img = Image.open(io.BytesIO(image_bytes)).convert('RGB')

    # A variety of different calling libraries throw different exceptions here.
    # They all correspond to an unreadable file so we treat them equivalently.
    # pylint: disable broad-except
    except Exception as e:
      logging.exception('Error processing image %s: %s', uri, str(e))
      context.aggregate_to(error_count, 1)
      return

    # Convert to desired format and output.
    output = io.BytesIO()
    img.save(output, Default.FORMAT)
    image_bytes = output.getvalue()
    yield uri, label_ids, image_bytes


class EmbeddingsGraph(object):
  """Builds a graph and uses it to extract embeddings from images.
  """

  # These constants are set by Inception v3's expectations.
  WIDTH = 299
  HEIGHT = 299
  CHANNELS = 3

  def __init__(self, tf_session):
    self.tf_session = tf_session
    # input_jpeg is the tensor that contains raw image bytes.
    # It is used to feed image bytes and obtain embeddings.
    self.input_jpeg, self.embedding = self.build_graph()
    self.tf_session.run(tf.initialize_all_variables())
    self.restore_from_checkpoint(Default.IMAGE_GRAPH_CHECKPOINT_URI)

  def build_graph(self):
    """Forms the core by building a wrapper around the inception graph.

      Here we add the necessary input & output tensors, to decode jpegs,
      serialize embeddings, restore from checkpoint etc.

      To use other Inception models modify this file. Note that to use other
      models beside Inception, you should make sure input_shape matches
      their input. Resizing or other modifications may be necessary as well.
      See tensorflow/contrib/slim/python/slim/nets/inception_v3.py for
      details about InceptionV3.

    Returns:
      input_jpeg: A tensor containing raw image bytes as the input layer.
      embedding: The embeddings tensor, that will be materialized later.
    """

    input_jpeg = tf.placeholder(tf.string, shape=None)
    image = tf.image.decode_jpeg(input_jpeg, channels=self.CHANNELS)

    # Note resize expects a batch_size, but we are feeding a single image.
    # So we have to expand then squeeze.  Resize returns float32 in the
    # range [0, uint8_max]
    image = tf.expand_dims(image, 0)

    # convert_image_dtype also scales [0, uint8_max] -> [0 ,1).
    image = tf.image.convert_image_dtype(image, dtype=tf.float32)
    image = tf.image.resize_bilinear(
        image, [self.HEIGHT, self.WIDTH], align_corners=False)

    # Then rescale range to [-1, 1) for Inception.
    image = tf.sub(image, 0.5)
    inception_input = tf.mul(image, 2.0)

    # Build Inception layers, which expect a tensor of type float from [-1, 1)
    # and shape [batch_size, height, width, channels].
    with slim.arg_scope(inception.inception_v3_arg_scope()):
      _, end_points = inception.inception_v3(inception_input, is_training=False)

    embedding = end_points['PreLogits']
    return input_jpeg, embedding

  def restore_from_checkpoint(self, checkpoint_path):
    """To restore inception model variables from the checkpoint file.

       Some variables might be missing in the checkpoint file, so it only
       loads the ones that are avialable, assuming the rest would be
       initialized later.
    Args:
      checkpoint_path: Path to the checkpoint file for the Inception graph.
    """
    # Get all variables to restore. Exclude Logits and AuxLogits because they
    # depend on the input data and we do not need to intialize them from
    # checkpoint.
    all_vars = tf.contrib.slim.get_variables_to_restore(
        exclude=['InceptionV3/AuxLogits', 'InceptionV3/Logits', 'global_step'])

    saver = tf.train.Saver(all_vars)
    saver.restore(self.tf_session, checkpoint_path)

  def calculate_embedding(self, batch_image_bytes):
    """Get the embeddings for a given JPEG image.

    Args:
      batch_image_bytes: As if returned from [ff.read() for ff in file_list].

    Returns:
      The Inception embeddings (bottleneck layer output)
    """
    return self.tf_session.run(
        self.embedding, feed_dict={self.input_jpeg: batch_image_bytes})


class TFExampleFromImageDoFn(beam.DoFn):
  """Embeds image bytes and labels, stores them in tensorflow.Example.

  (uri, label_ids, image_bytes) -> (tensorflow.Example).

  Output proto contains 'label', 'image_uri' and 'embedding'.
  The 'embedding' is calculated by feeding image into input layer of image
  neural network and reading output of the bottleneck layer of the network.

  Attributes:
    image_graph_uri: an uri to gcs bucket where serialized image graph is
                     stored.
  """

  def __init__(self):
    self.tf_session = None
    self.graph = None
    self.preprocess_graph = None

  def start_bundle(self, context):
    # There is one tensorflow session per instance of TFExampleFromImageDoFn.
    # The same instance of session is re-used between bundles.
    # Session is closed by the destructor of Session object, which is called
    # when instance of TFExampleFromImageDoFn() is destructed.
    if not self.graph:
      self.graph = tf.Graph()
      self.tf_session = tf.InteractiveSession(graph=self.graph)
      with self.graph.as_default():
        self.preprocess_graph = EmbeddingsGraph(self.tf_session)

  def process(self, context):

    def _bytes_feature(value):
      return tf.train.Feature(bytes_list=tf.train.BytesList(value=value))

    def _float_feature(value):
      return tf.train.Feature(float_list=tf.train.FloatList(value=value))

    uri, label_ids, image_bytes = context.element

    try:
      embedding = self.preprocess_graph.calculate_embedding(image_bytes)
    except errors.InvalidArgumentError as e:
      context.aggregate_to(incompatible_image, 1)
      logging.warning('Could not encode an image from %s: %s', uri, str(e))
      return

    if embedding.any():
      context.aggregate_to(embedding_good, 1)
    else:
      context.aggregate_to(embedding_bad, 1)

    example = tf.train.Example(features=tf.train.Features(feature={
        'image_uri': _bytes_feature([uri]),
        'embedding': _float_feature(embedding.ravel().tolist()),
    }))

    if label_ids:
      label_ids.sort()
      example.features.feature['label'].int64_list.value.extend(label_ids)

    yield example


def configure_pipeline(p, opt):
  """Specify PCollection and transformations in pipeline."""
  input_source = beam.io.TextFileSource(
      opt.input_path, strip_trailing_newlines=True)
  label_source = beam.io.TextFileSource(
      opt.input_dict, strip_trailing_newlines=True)
  labels = (p | 'Read dictionary' >> beam.Read(label_source))
  _ = (p
       | 'Read input' >> beam.Read(input_source)
       | 'Parse input' >> beam.Map(lambda line: csv.reader([line]).next())
       | 'Extract label ids' >> beam.ParDo(ExtractLabelIdsDoFn(),
                                           beam.pvalue.AsIter(labels))
       | 'Read and convert to JPEG'
       >> beam.ParDo(ReadImageAndConvertToJpegDoFn())
       | 'Embed and make TFExample' >> beam.ParDo(TFExampleFromImageDoFn())
       | 'Save to disk' >> SaveFeatures(opt.output_path))


def run(in_args=None):
  """Runs the pre-processing pipeline."""

  pipeline_options = PipelineOptions.from_dictionary(vars(in_args))
  p = beam.Pipeline(options=pipeline_options)
  configure_pipeline(p, in_args)
  p.run()


def get_cloud_project():
  cmd = ['gcloud', 'config', 'list', 'project', '--format=value(core.project)']
  with open(os.devnull, 'w') as dev_null:
    return subprocess.check_output(cmd, stderr=dev_null).strip()


def default_args(argv):
  """Provides default values for Workflow flags."""
  parser = argparse.ArgumentParser()

  parser.add_argument(
      '--input_path',
      required=True,
      help='Input specified as uri to CSV file. Each line of csv file '
      'contains colon-separated GCS uri to an image and labels.')
  parser.add_argument(
      '--input_dict',
      dest='input_dict',
      required=True,
      help='Input dictionary. Specified as text file uri. '
      'Each line of the file stores one label.')
  parser.add_argument(
      '--output_path',
      required=True,
      help='Output directory to write results to.')
  parser.add_argument(
      '--project',
      type=str,
      help='The cloud project name to be used for running this pipeline')

  parser.add_argument(
      '--job_name',
      type=str,
      default='flowers-' + datetime.datetime.now().strftime('%Y%m%d-%H%M%S'),
      help='A unique job identifier.')
  parser.add_argument(
      '--staging_location', type=str, help='Path to the staging location.')
  parser.add_argument(
      '--num_workers', default=10, type=int, help='The number of workers.')
  parser.add_argument('--cloud', default=False, action='store_true')
  parser.add_argument(
      '--runner',
      help='See Dataflow runners, may be blocking'
      ' or not, on cloud or not, etc.')

  parsed_args, _ = parser.parse_known_args(argv)

  if parsed_args.cloud:
    # Flags which need to be set for cloud runs.
    default_values = {
        'project':
            get_cloud_project(),
        'staging_location':
            os.path.join(os.path.dirname(parsed_args.output_path), 'staging'),
        'runner':
            # Use this runner if you want to block:
            # 'BlockingDataflowPipelineRunner',
            # This runner does not block -- you can track the job progress in
            # the developer's console.
            'DataflowPipelineRunner',
        'extra_package':
            Default.CML_PACKAGE,
        'save_main_session':
            True,
    }
  else:
    # Flags which need to be set for local runs.
    default_values = {
        'runner': 'DirectPipelineRunner',
    }

  for kk, vv in default_values.iteritems():
    if kk not in parsed_args or not vars(parsed_args)[kk]:
      vars(parsed_args)[kk] = vv

  return parsed_args


def main(argv):
  arg_dict = default_args(argv)
  run(arg_dict)


if __name__ == '__main__':
  main(sys.argv[1:])
