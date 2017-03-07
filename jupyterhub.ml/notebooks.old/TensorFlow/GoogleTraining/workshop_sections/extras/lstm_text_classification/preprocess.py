import argparse
import datetime
import os

import apache_beam as beam
from apache_beam.utils.options import PipelineOptions
from google.cloud import ml


def text_classification_features(tf, element):
  def _bytes_feature(value):
    return tf.train.Feature(bytes_list=tf.train.BytesList(value=value))

  def _int64_feature(value):
    return tf.train.Feature(int64_list=tf.train.Int64List(value=value))

  return {
      'words': _bytes_feature([s.encode('utf_8')
                               for s in element[1]]),
      'category': _int64_feature([element[0]])
  }


class EncodeExampleDoFn(beam.DoFn):
  """Resuable example encoding function"""

  def __init__(self, element_to_feature_fn):
    self.element_to_feature_fn = element_to_feature_fn
    self.tf = __import__('tensorflow')

  def process(self, context):
    yield self.tf.train.Example(features=self.tf.train.Features(
      feature=self.element_to_feature_fn(self.tf, context.element)))


def tokenize_and_index(row):
  def clean_str(string):
    import re
    """
    Tokenization/string cleaning for all datasets except for SST.
    Original taken from
    https://github.com/yoonkim/CNN_sentence/blob/master/process_data.py
    """
    string = re.sub(r"[^A-Za-z0-9(),!?\'\`]", " ", string)
    string = re.sub(r"\'s", " \'s", string)
    string = re.sub(r"\'ve", " \'ve", string)
    string = re.sub(r"n\'t", " n\'t", string)
    string = re.sub(r"\'re", " \'re", string)
    string = re.sub(r"\'d", " \'d", string)
    string = re.sub(r"\'ll", " \'ll", string)
    string = re.sub(r",", " , ", string)
    string = re.sub(r"!", " ! ", string)
    string = re.sub(r"\(", " \( ", string)
    string = re.sub(r"\)", " \) ", string)
    string = re.sub(r"\?", " \? ", string)
    string = re.sub(r"\s{2,}", " ", string)
    return string.strip().lower()

  subreddits = {'aww': 0, 'news': 1}
  return subreddits[row['subreddit']], clean_str(row['title'])


def main(args, unknown):
  if args.cloud:
    options = {
        'staging_location':
        os.path.join(args.output_dir, 'tmp', 'staging'),
        'temp_location':
        os.path.join(args.output_dir, 'tmp'),
        'job_name': ('cloud-ml-sample-reddit-data-preprocess-{}'.format(
            datetime.datetime.now().strftime('%Y%m%d%H%M%S'))),
        'extra_packages': [ml.sdk_location],
        'teardown_policy': 'TEARDOWN_ALWAYS',
        'no_save_main_session': True,
        'autoscaling_algorithm': 'THROUGHPUT_BASED'
    }
    opts = beam.pipeline.PipelineOptions(flags=unknown, **options)
    pipeline = beam.Pipeline('BlockingDataflowPipelineRunner', options=opts)
  else:
    pipeline = beam.Pipeline(
        'apache_beam.runners.inprocess.InProcessPipelineRunner')
  pipeline | beam.io.Read(
      beam.io.BigQuerySource(
          project='oscon-tf-workshop',
          dataset='textclassification',
          table='full201509',
          validate=True
      )
  ) | beam.Map(
      'tokenize words',
      tokenize_and_index
  ) | beam.ParDo(
      'Make TF examples',
      EncodeExampleDoFn(text_classification_features)
  ) | 'Write examples to file' >> ml.io.SaveFeatures(args.output_dir + 'data')

  pipeline.run()

if __name__ == '__main__':
  parser = argparse.ArgumentParser()
  parser.add_argument('--output-dir', type=str, required=True)
  parser.add_argument('--cloud', action='store_true', default=False)
  args, unknown = parser.parse_known_args()
  main(args, unknown)
