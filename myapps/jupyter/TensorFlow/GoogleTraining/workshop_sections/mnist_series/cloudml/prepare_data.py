import argparse
import os
import tensorflow as tf

from tensorflow.examples.tutorials.mnist import input_data
from tensorflow.python.lib.io.tf_record import TFRecordCompressionType

def dataset_to_file(feature_iter, filename):
  writer = tf.python_io.TFRecordWriter(
      filename, 
      options=tf.python_io.TFRecordOptions(
          compression_type=TFRecordCompressionType.GZIP))
  with writer:
    for feature in feature_iter:
      writer.write(tf.train.Example(features=tf.train.Features(
          feature=feature
      )).SerializeToString())

def mnist_feature_fn(dataset):
  for image, label in zip(dataset.images.tolist(), dataset.labels.tolist()):
    assert len(image) == 784
    yield {
        'labels': tf.train.Feature(
            int64_list=tf.train.Int64List(value=[label])),
        'images': tf.train.Feature(
            float_list=tf.train.FloatList(value=image)),
    }

if __name__ == '__main__':
  parser = argparse.ArgumentParser()
  parser.add_argument('output_dir', type=os.path.abspath)
  args = parser.parse_args()
  mnist = input_data.read_data_sets("data",
                                    one_hot=False,
                                    validation_size=0)

  train_path = os.path.join(args.output_dir, 'train.pb2')
  print('Writing train data to: {}'.format(train_path))
  dataset_to_file(mnist_feature_fn(mnist.train), train_path)

  eval_path = os.path.join(args.output_dir, 'eval.pb2')
  print('Writing eval data to: {}'.format(eval_path))
  dataset_to_file(mnist_feature_fn(mnist.test), eval_path)
