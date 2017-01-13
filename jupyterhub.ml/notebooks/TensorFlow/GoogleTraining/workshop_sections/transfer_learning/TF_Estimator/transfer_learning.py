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
"""Simple transfer learning with an Inception v3 architecture model which
displays summaries in TensorBoard.

This example shows how to take a Inception v3 architecture model trained on
ImageNet images, and train a new top layer that can recognize other classes of
images.

This example is based on the code here:
https://github.com/tensorflow/tensorflow/blob/master/tensorflow/examples/image_retraining/retrain.py
but has been further modified in a number of ways, including use of a custom
tf.contrib.learn.Estimator to support training, evaluation, prediction,
model checkpointing, and summary generation.
(The support for optional image distortion has also been removed in order to
simplify the example).

The top layer receives as input a 2048-dimensional vector for each image. We
train a softmax layer on top of this representation. Assuming the softmax layer
contains N labels, this corresponds to learning N + 2048*N model parameters
corresponding to the learned biases and weights.

Here's an example, which assumes you have a folder containing class-named
subfolders, each full of images for each label.
An example folder 'flower_photos' could have a structure like this:

~/flower_photos/daisy/photo1.jpg
~/flower_photos/daisy/photo2.jpg
...
~/flower_photos/rose/anotherphoto77.jpg
...
~/flower_photos/sunflower/somepicture.jpg

The subfolder names are important, since they define what label is applied to
each image, but the filenames themselves don't matter. Once your images are
prepared, you can process them and then run training on the images like this:

  python transfer_learning.py --image_dir <your_toplevel_image_dir>

You can replace the image_dir argument with any folder containing subfolders of
images. The label for each image is taken from the name of the subfolder it's
in.

To view summary information in tensorboard, point it to the <model_dir>
(which if not specified will be automatically generated for you):
  tensorboard --logdir=<model_dir>

"""
from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import argparse
import glob
import hashlib
import json
import os
import re
import struct
import sys
import tarfile
import time

import numpy as np
from six.moves import urllib
import tensorflow as tf
from tensorflow.contrib.learn import ModeKeys

from tensorflow.python.platform import gfile
from tensorflow.python.util import compat
from tensorflow.contrib.learn.python.learn import metric_spec
from tensorflow.contrib.metrics.python.ops import metric_ops

FLAGS = tf.app.flags.FLAGS
ARGFLAGS = None
LABELS_FILENAME = "output_labels.json"

# comment out for less info during the training runs.
tf.logging.set_verbosity(tf.logging.INFO)

# These are all parameters that are tied to the particular model architecture
# we're using for Inception v3. These include things like tensor names and
# their sizes. If you want to adapt this script to work with another model,
# you will need to update these to reflect the values in the network
# you're using.
# pylint: disable=line-too-long
DATA_URL = 'http://download.tensorflow.org/models/image/imagenet/inception-2015-12-05.tgz'
# pylint: enable=line-too-long
BOTTLENECK_TENSOR_NAME = 'pool_3/_reshape:0'
BOTTLENECK_TENSOR_SIZE = 2048
MODEL_INPUT_WIDTH = 299
MODEL_INPUT_HEIGHT = 299
MODEL_INPUT_DEPTH = 3
JPEG_DATA_TENSOR_NAME = 'DecodeJpeg/contents:0'
RESIZED_INPUT_TENSOR_NAME = 'ResizeBilinear:0'
MAX_NUM_IMAGES_PER_CLASS = 2 ** 27 - 1  # ~134M


def create_image_lists(image_dir, testing_percentage, validation_percentage):
  """Builds a list of training images from the file system.

  Analyzes the sub folders in the image directory, splits them into stable
  training, testing, and validation sets, and returns a data structure
  describing the lists of images for each label and their paths.

  If the model_dir already has a label list in it, use that to define the label
  ordering as the images are processed.

  Args:
    image_dir: String path to a folder containing subfolders of images.
    testing_percentage: Integer percentage of the images to reserve for tests.
    validation_percentage: Integer percentage of images reserved
    for validation.

  Returns:
    A dictionary containing an entry for each label subfolder, with images
    split into training, testing, and validation sets within each label.
  """
  if not gfile.Exists(image_dir):
    print("Image directory '" + image_dir + "' not found.")
    return None

  # See if the model dir contains an existing labels list. This will only be
  # the case if training using that model has ocurred previously.
  labels_list = None
  output_labels_file = os.path.join(ARGFLAGS.model_dir, LABELS_FILENAME)
  if gfile.Exists(output_labels_file):
    with open(output_labels_file, 'r') as lfile:
      labels_string = lfile.read()
      labels_list = json.loads(labels_string)
      print("Found labels list: %s" % labels_list)

  result = {}
  if labels_list:
    for l in labels_list:
      result[l] = {}

  sub_dirs = [x[0] for x in os.walk(image_dir)]
  # The root directory comes first, so skip it.
  is_root_dir = True
  for sub_dir in sub_dirs:
    if is_root_dir:
      is_root_dir = False
      continue
    extensions = ['jpg', 'jpeg', 'JPG', 'JPEG']
    file_list = []
    dir_name = os.path.basename(sub_dir)
    if dir_name == image_dir:
      continue
    print("Looking for images in '" + dir_name + "'")
    for extension in extensions:
      file_glob = os.path.join(image_dir, dir_name, '*.' + extension)
      file_list.extend(glob.glob(file_glob))
    if not file_list:
      print('No files found')
      continue
    if len(file_list) < 20:
      print('WARNING: Folder has less than 20 images, which may cause issues.')
    elif len(file_list) > MAX_NUM_IMAGES_PER_CLASS:
      print('WARNING: Folder {} has more than {} images. Some images will '
            'never be selected.'.format(dir_name, MAX_NUM_IMAGES_PER_CLASS))
    label_name = re.sub(r'[^a-z0-9]+', ' ', dir_name.lower())
    training_images = []
    testing_images = []
    validation_images = []
    for file_name in file_list:
      base_name = os.path.basename(file_name)
      # We want to ignore anything after '_nohash_' in the file name.
      hash_name = re.sub(r'_nohash_.*$', '', file_name)
      # This looks a bit magical, but we need to decide whether this file
      # should go into the training, testing, or validation sets, and we
      # want to keep existing files in the same set even if more files
      # are subsequently added.
      # To do that, we need a stable way of deciding based on just the
      # file name itself, so we do a hash of that and then use that to
      # generate a probability value that we use to assign it.
      hash_name_hashed = hashlib.sha1(compat.as_bytes(hash_name)).hexdigest()
      percentage_hash = ((int(hash_name_hashed, 16) %
                          (MAX_NUM_IMAGES_PER_CLASS + 1)) *
                         (100.0 / MAX_NUM_IMAGES_PER_CLASS))
      if percentage_hash < validation_percentage:
        validation_images.append(base_name)
      elif percentage_hash < (testing_percentage + validation_percentage):
        testing_images.append(base_name)
      else:
        training_images.append(base_name)
    result[label_name] = {
        'dir': dir_name,
        'training': training_images,
        'testing': testing_images,
        'validation': validation_images,
    }
  return result


def get_image_path(image_lists, label_name, index, image_dir, category):
  """"Returns a path to an image for a label at the given index.

  Args:
    image_lists: Dictionary of training images for each label.
    label_name: Label string we want to get an image for.
    index: Int offset of the image we want. This will be moduloed by the
    available number of images for the label, so it can be arbitrarily large.
    image_dir: Root folder string of the subfolders containing the training
    images.
    category: Name string of set to pull images from - training, testing, or
    validation.

  Returns:
    File system path string to an image that meets the requested parameters.

  """
  if label_name not in image_lists:
    tf.logging.fatal('Label does not exist %s.', label_name)
  label_lists = image_lists[label_name]
  if category not in label_lists:
    tf.logging.fatal('Category does not exist %s.', category)
  category_list = label_lists[category]
  if not category_list:
    tf.logging.fatal('Label %s has no images in the category %s.',
                     label_name, category)
  mod_index = index % len(category_list)
  base_name = category_list[mod_index]
  sub_dir = label_lists['dir']
  full_path = os.path.join(image_dir, sub_dir, base_name)
  return full_path


def get_bottleneck_path(image_lists, label_name, index, bottleneck_dir,
                        category):
  """"Returns a path to a bottleneck file for a label at the given index.

  Args:
    image_lists: Dictionary of training images for each label.
    label_name: Label string we want to get an image for.
    index: Integer offset of the image we want. This will be moduloed by the
    available number of images for the label, so it can be arbitrarily large.
    bottleneck_dir: Folder string holding cached files of bottleneck values.
    category: Name string of set to pull images from - training, testing, or
    validation.

  Returns:
    File system path string to an image that meets the requested parameters.
  """
  return get_image_path(image_lists, label_name, index, bottleneck_dir,
                        category) + '.txt'


def create_inception_graph(dest_dir):
  """"Creates a graph from saved GraphDef file and returns a Graph object.

  Returns:
    Graph holding the trained Inception network, and various tensors we'll be
    manipulating.
  """
  with tf.Session() as sess:
    model_filename = os.path.join(
        dest_dir, 'classify_image_graph_def.pb')
    with gfile.FastGFile(model_filename, 'rb') as f:
      graph_def = tf.GraphDef()
      graph_def.ParseFromString(f.read())
      bottleneck_tensor, jpeg_data_tensor, resized_input_tensor = (
          tf.import_graph_def(graph_def, name='', return_elements=[
              BOTTLENECK_TENSOR_NAME, JPEG_DATA_TENSOR_NAME,
              RESIZED_INPUT_TENSOR_NAME]))
  return sess.graph, bottleneck_tensor, jpeg_data_tensor, resized_input_tensor


def run_bottleneck_on_image(sess, image_data, image_data_tensor,
                            bottleneck_tensor):
  """Runs inference on an image to extract the 'bottleneck' summary layer.

  Args:
    sess: Current active TensorFlow Session.
    image_data: String of raw JPEG data.
    image_data_tensor: Input data layer in the graph.
    bottleneck_tensor: Layer before the final softmax.

  Returns:
    Numpy array of bottleneck values.
  """
  bottleneck_values = sess.run(
      bottleneck_tensor,
      {image_data_tensor: image_data})
  bottleneck_values = np.squeeze(bottleneck_values)
  return bottleneck_values


def maybe_download_and_extract(dest_dir='/tmp/imagenet'):
  """Download and extract model tar file.

  If the pretrained model we're using doesn't already exist, this function
  downloads it from the TensorFlow.org website and unpacks it into a directory.
  """
  if not os.path.exists(dest_dir):
    os.makedirs(dest_dir)
  filename = DATA_URL.split('/')[-1]
  filepath = os.path.join(dest_dir, filename)
  if not os.path.exists(filepath):

    def _progress(count, block_size, total_size):
      sys.stdout.write('\r>> Downloading %s %.1f%%' %
                       (filename,
                        float(count * block_size) / float(total_size) * 100.0))
      sys.stdout.flush()

    filepath, _ = urllib.request.urlretrieve(DATA_URL,
                                             filepath,
                                             _progress)
    print()
    statinfo = os.stat(filepath)
    print('Successfully downloaded', filename, statinfo.st_size, 'bytes.')
  tarfile.open(filepath, 'r:gz').extractall(dest_dir)


def ensure_dir_exists(dir_name):
  """Makes sure the folder exists on disk.

  Args:
    dir_name: Path string to the folder we want to create.
  """
  if not os.path.exists(dir_name):
    os.makedirs(dir_name)


def write_list_of_floats_to_file(list_of_floats, file_path):
  """Writes a given list of floats to a binary file.

  Args:
    list_of_floats: List of floats we want to write to a file.
    file_path: Path to a file where list of floats will be stored.

  """

  s = struct.pack('d' * BOTTLENECK_TENSOR_SIZE, *list_of_floats)
  with open(file_path, 'wb') as f:
    f.write(s)


def read_list_of_floats_from_file(file_path):
  """Reads list of floats from a given file.

  Args:
    file_path: Path to a file where list of floats was stored.
  Returns:
    Array of bottleneck values (list of floats).

  """

  with open(file_path, 'rb') as f:
    s = struct.unpack('d' * BOTTLENECK_TENSOR_SIZE, f.read())
    return list(s)


bottleneck_path_2_bottleneck_values = {}


def get_or_create_bottleneck(sess, image_lists, label_name, index, image_dir,
                             category, bottleneck_dir, jpeg_data_tensor,
                             bottleneck_tensor):
  """Retrieves or calculates bottleneck values for an image.

  If a cached version of the bottleneck data exists on-disk, return that,
  otherwise calculate the data and save it to disk for future use.

  Args:
    sess: The current active TensorFlow Session.
    image_lists: Dictionary of training images for each label.
    label_name: Label string we want to get an image for.
    index: Integer offset of the image we want. This will be modulo-ed by the
    available number of images for the label, so it can be arbitrarily large.
    image_dir: Root folder string  of the subfolders containing the training
    images.
    category: Name string of which  set to pull images from: training, testing,
    or validation.
    bottleneck_dir: Folder string holding cached files of bottleneck values.
    jpeg_data_tensor: The tensor to feed loaded jpeg data into.
    bottleneck_tensor: The output tensor for the bottleneck values.

  Returns:
    Numpy array of values produced by the bottleneck layer for the image.
  """
  label_lists = image_lists[label_name]
  sub_dir = label_lists['dir']
  sub_dir_path = os.path.join(bottleneck_dir, sub_dir)
  ensure_dir_exists(sub_dir_path)
  bottleneck_path = get_bottleneck_path(image_lists, label_name, index,
                                        bottleneck_dir, category)
  if not os.path.exists(bottleneck_path):
    print('Creating bottleneck at ' + bottleneck_path)
    image_path = get_image_path(image_lists, label_name, index, image_dir,
                                category)
    if not gfile.Exists(image_path):
      tf.logging.fatal('File does not exist %s', image_path)
    image_data = gfile.FastGFile(image_path, 'rb').read()
    bottleneck_values = run_bottleneck_on_image(sess, image_data,
                                                jpeg_data_tensor,
                                                bottleneck_tensor)
    bottleneck_string = ','.join(str(x) for x in bottleneck_values)
    with open(bottleneck_path, 'w') as bottleneck_file:
      bottleneck_file.write(bottleneck_string)

  with open(bottleneck_path, 'r') as bottleneck_file:
    bottleneck_string = bottleneck_file.read()
  bottleneck_values = [float(x) for x in bottleneck_string.split(',')]
  return bottleneck_values


def cache_bottlenecks(sess, image_lists, image_dir, bottleneck_dir,
                      jpeg_data_tensor, bottleneck_tensor):
  """Ensures all the training, testing, and validation bottlenecks are cached.

  Because we're likely to read the same image multiple times (if there are no
  distortions applied during training) it can speed things up a lot if we
  calculate the bottleneck layer values once for each image during
  preprocessing, and then just read those cached values repeatedly during
  training. Here we go through all the images we've found, calculate those
  values, and save them off.

  Args:
    sess: The current active TensorFlow Session.
    image_lists: Dictionary of training images for each label.
    image_dir: Root folder string of the subfolders containing the training
    images.
    bottleneck_dir: Folder string holding cached files of bottleneck values.
    jpeg_data_tensor: Input tensor for jpeg data from file.
    bottleneck_tensor: The penultimate output layer of the graph.

  Returns:
    Nothing.
  """
  how_many_bottlenecks = 0
  ensure_dir_exists(bottleneck_dir)
  for label_name, label_lists in image_lists.items():
    for category in ['training', 'testing', 'validation']:
      category_list = label_lists[category]
      for index, unused_base_name in enumerate(category_list):
        get_or_create_bottleneck(sess, image_lists, label_name, index,
                                 image_dir, category, bottleneck_dir,
                                 jpeg_data_tensor, bottleneck_tensor)
        how_many_bottlenecks += 1
        if how_many_bottlenecks % 100 == 0:
          print(str(how_many_bottlenecks) + ' bottleneck files created.')


def get_all_cached_bottlenecks(
    sess, image_lists, category, bottleneck_dir, image_dir, jpeg_data_tensor,
    bottleneck_tensor):

  bottlenecks = []
  ground_truths = []
  label_names = list(image_lists.keys())
  for label_index in range(len(label_names)):
    label_name = label_names[label_index]
    for image_index in range(len(image_lists[label_name][category])):
      bottleneck = get_or_create_bottleneck(
          sess, image_lists, label_name, image_index, image_dir, category,
          bottleneck_dir, jpeg_data_tensor, bottleneck_tensor)
      ground_truth = np.zeros(len(label_names), dtype=np.float32)
      ground_truth[label_index] = 1.0
      bottlenecks.append(bottleneck)
      ground_truths.append(ground_truth)

  return bottlenecks, ground_truths


def variable_summaries(var, name):
  """Attach a lot of summaries to a Tensor (for TensorBoard visualization)."""
  with tf.name_scope('summaries'):
    mean = tf.reduce_mean(var)
    tf.scalar_summary('mean/' + name, mean)
    with tf.name_scope('stddev'):
      stddev = tf.sqrt(tf.reduce_mean(tf.square(var - mean)))
    tf.scalar_summary('stddev/' + name, stddev)
    tf.scalar_summary('max/' + name, tf.reduce_max(var))
    tf.scalar_summary('min/' + name, tf.reduce_min(var))
    tf.histogram_summary(name, var)


def add_final_training_ops(
    class_count, mode, final_tensor_name,
    bottleneck_input, ground_truth_input):
  """Adds a new softmax and fully-connected layer for training.

  We need to retrain the top layer to identify our new classes, so this
  function adds the right operations to the graph, along with some variables
  to hold the weights, and then sets up all the gradients for the backward
  pass.

  The set up for the softmax and fully-connected layers is based on:
  https://tensorflow.org/versions/master/tutorials/mnist/beginners/index.html

  Args:
    class_count: Integer of how many categories of things we're trying to
    recognize.
    final_tensor_name: Name string for the new final node that produces
    results.
    bottleneck_tensor: The output of the main CNN graph.

  Returns:
    The tensors for the training and cross entropy results, and tensors for the
    bottleneck input and ground truth input.
  """

  # Organizing the following ops as `final_training_ops` so they're easier
  # to see in TensorBoard
  train_step = None
  cross_entropy_mean = None

  layer_name = 'final_training_ops'
  with tf.name_scope(layer_name):
    with tf.name_scope('weights'):
      layer_weights = tf.Variable(
          tf.truncated_normal(
              [BOTTLENECK_TENSOR_SIZE, class_count],
              stddev=0.001), name='final_weights')
      variable_summaries(layer_weights, layer_name + '/weights')
    with tf.name_scope('biases'):
      layer_biases = tf.Variable(tf.zeros([class_count]), name='final_biases')
      variable_summaries(layer_biases, layer_name + '/biases')
    with tf.name_scope('Wx_plus_b'):
      logits = tf.matmul(bottleneck_input, layer_weights) + layer_biases
      tf.histogram_summary(layer_name + '/pre_activations', logits)

  final_tensor = tf.nn.softmax(logits, name=final_tensor_name)
  tf.histogram_summary(final_tensor_name + '/activations', final_tensor)

  if mode in [ModeKeys.EVAL, ModeKeys.TRAIN]:
    with tf.name_scope('cross_entropy'):
      cross_entropy = tf.nn.softmax_cross_entropy_with_logits(
          logits, ground_truth_input)
      with tf.name_scope('total'):
        cross_entropy_mean = tf.reduce_mean(cross_entropy)
      tf.scalar_summary('cross entropy', cross_entropy_mean)

    with tf.name_scope('train'):
      train_step = tf.train.GradientDescentOptimizer(
          ARGFLAGS.learning_rate).minimize(
              cross_entropy_mean,
              global_step=tf.contrib.framework.get_global_step())

  return (train_step, cross_entropy_mean, final_tensor)


def add_evaluation_step(result_tensor, ground_truth_tensor):
  """Inserts the operations we need to evaluate the accuracy of our results.

  Args:
    result_tensor: The new final node that produces results.
    ground_truth_tensor: The node we feed ground truth data
    into.

  Returns:
    Nothing.
  """
  with tf.name_scope('accuracy'):
    with tf.name_scope('correct_prediction'):
      correct_prediction = tf.equal(tf.argmax(result_tensor, 1), \
                                    tf.argmax(ground_truth_tensor, 1))
    with tf.name_scope('accuracy'):
      evaluation_step = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
    tf.scalar_summary('accuracy', evaluation_step)
  return evaluation_step


def make_model_fn(class_count, final_tensor_name):

  def _make_model(bottleneck_input, ground_truth_input, mode, params):

    prediction_dict = {}
    train_step = None
    cross_entropy = None

    # Add the new layer that we'll be training.
    (train_step, cross_entropy,
     final_tensor) = add_final_training_ops(
        class_count, mode, final_tensor_name,
        bottleneck_input, ground_truth_input)

    if mode == ModeKeys.EVAL:
      prediction_dict['loss'] = cross_entropy
      # Create the operations we need to evaluate accuracy
      acc = add_evaluation_step(final_tensor, ground_truth_input)
      prediction_dict['accuracy'] = acc

    if mode == ModeKeys.INFER:
      predclass = tf.argmax(final_tensor, 1)
      prediction_dict["class_vector"] = final_tensor
      prediction_dict["index"] = predclass

    return prediction_dict, cross_entropy, train_step

  return _make_model

METRICS = {
    'loss': metric_spec.MetricSpec(
        metric_fn=metric_ops.streaming_mean,
        prediction_key='loss'
    ),
    'accuracy': metric_spec.MetricSpec(
        metric_fn=metric_ops.streaming_mean,
        prediction_key='accuracy'
    )
}


def make_image_predictions(
    classifier, jpeg_data_tensor, bottleneck_tensor, path_list, labels_list):
  """Use the learned model to make predictions."""

  if not labels_list:
    output_labels_file = os.path.join(ARGFLAGS.model_dir, LABELS_FILENAME)
    if gfile.Exists(output_labels_file):
      with open(output_labels_file, 'r') as lfile:
        labels_string = lfile.read()
        labels_list = json.loads(labels_string)
        print("labels list: %s" % labels_list)
    else:
      print("Labels list %s not found" % output_labels_file)
      return None

  sess = tf.Session()
  bottlenecks = []
  print("Predicting for images: %s" % path_list)
  for img_path in path_list:
    # get bottleneck for an image path. Don't cache the bottleneck values here.
    if not gfile.Exists(img_path):
      tf.logging.fatal('File does not exist %s', img_path)
    image_data = gfile.FastGFile(img_path, 'rb').read()
    bottleneck_values = run_bottleneck_on_image(sess, image_data,
                                                jpeg_data_tensor,
                                                bottleneck_tensor)
    bottlenecks.append(bottleneck_values)
  prediction_input = np.array(bottlenecks)
  predictions = classifier.predict(x=prediction_input, as_iterable=True)
  print("Predictions:")
  for _, p in enumerate(predictions):
    print("---------")
    for k in p.keys():
      print("%s is: %s " % (k, p[k]))
      if k == "index":
        print("index label is: %s" % labels_list[p[k]])


def get_prediction_images(img_dir):
  """Grab images from the prediction directory."""
  extensions = ['jpg', 'jpeg', 'JPG', 'JPEG']
  file_list = []
  if not gfile.Exists(img_dir):
    print("Image directory '" + img_dir + "' not found.")
    return None
  print("Looking for images in '" + img_dir + "'")
  for extension in extensions:
    file_glob = os.path.join(img_dir, '*.' + extension)
    file_list.extend(glob.glob(file_glob))
  if not file_list:
    print('No image files found')
  return file_list


def main(_):

  print("Using model directory %s" % ARGFLAGS.model_dir)

  # Set up the pre-trained graph.
  maybe_download_and_extract(dest_dir=ARGFLAGS.incp_model_dir)
  graph, bottleneck_tensor, jpeg_data_tensor, resized_image_tensor = (
      create_inception_graph(ARGFLAGS.incp_model_dir))

  sess = tf.Session()
  labels_list = None

  if not ARGFLAGS.predict_only:

    # Look at the folder structure, and create lists of all the images.
    image_lists = create_image_lists(
        ARGFLAGS.image_dir, ARGFLAGS.testing_percentage,
        ARGFLAGS.validation_percentage)
    class_count = len(image_lists.keys())
    if class_count == 0:
      print('No valid folders of images found at ' + ARGFLAGS.image_dir)
      return -1
    if class_count == 1:
      print('Only one valid folder of images found at ' + ARGFLAGS.image_dir +
            ' - multiple classes are needed for classification.')
      return -1

    # We'll make sure we've calculated the 'bottleneck' image summaries and
    # cached them on disk.
    cache_bottlenecks(
        sess, image_lists, ARGFLAGS.image_dir, ARGFLAGS.bottleneck_dir,
        jpeg_data_tensor, bottleneck_tensor)
  else:
    # load the labels list, needed to create the model; exit if it's not there
    output_labels_file = os.path.join(ARGFLAGS.model_dir, LABELS_FILENAME)
    if gfile.Exists(output_labels_file):
      with open(output_labels_file, 'r') as lfile:
        labels_string = lfile.read()
        labels_list = json.loads(labels_string)
        print("labels list: %s" % labels_list)
        class_count = len(labels_list)
    else:
      print("Labels list %s not found" % output_labels_file)
      return None

  # Define the custom estimator
  model_fn = make_model_fn(class_count, ARGFLAGS.final_tensor_name)
  model_params = {}
  classifier = tf.contrib.learn.Estimator(
      model_fn=model_fn, params=model_params, model_dir=ARGFLAGS.model_dir)

  if not ARGFLAGS.predict_only:
    train_bottlenecks, train_ground_truth = get_all_cached_bottlenecks(
        sess, image_lists, 'training',
        ARGFLAGS.bottleneck_dir, ARGFLAGS.image_dir, jpeg_data_tensor,
        bottleneck_tensor)
    train_bottlenecks = np.array(train_bottlenecks)
    train_ground_truth = np.array(train_ground_truth)

    # then run the training, unless doing prediction only
    print("Starting training for %s steps max" % ARGFLAGS.num_steps)
    classifier.fit(
        x=train_bottlenecks.astype(np.float32),
        y=train_ground_truth, batch_size=50,
        max_steps=ARGFLAGS.num_steps)

    # We've completed our training, so run a test evaluation on
    # some new images we haven't used before.
    test_bottlenecks, test_ground_truth = get_all_cached_bottlenecks(
        sess, image_lists, 'testing',
        ARGFLAGS.bottleneck_dir, ARGFLAGS.image_dir, jpeg_data_tensor,
        bottleneck_tensor)
    test_bottlenecks = np.array(test_bottlenecks)
    test_ground_truth = np.array(test_ground_truth)
    print("evaluating....")
    print(classifier.evaluate(
        test_bottlenecks.astype(np.float32), test_ground_truth, metrics=METRICS))

    # write the output labels file if it doesn't already exist
    output_labels_file = os.path.join(ARGFLAGS.model_dir, LABELS_FILENAME)
    if gfile.Exists(output_labels_file):
      print("Labels list file already exists; not writing.")
    else:
      output_labels = json.dumps(list(image_lists.keys()))
      with gfile.FastGFile(output_labels_file, 'w') as f:
        f.write(output_labels)

  print("\nPredicting...")
  img_list = get_prediction_images(ARGFLAGS.prediction_img_dir)
  if not img_list:
    print("No images found in %s" % ARGFLAGS.prediction_img_dir)
  else:
    make_image_predictions(
        classifier, jpeg_data_tensor, bottleneck_tensor, img_list, labels_list)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    # Input and output file flags.
    parser.add_argument('--image_dir', type=str, required=True,
                        help="Path to folders of labeled images.")
    # where the model information lives
    parser.add_argument('--model_dir', type=str,
                        default=os.path.join(
                            "/tmp/tfmodels/img_classify",
                            str(int(time.time()))),
                        help='Directory for storing model info')
    # whether to run prediction only.
    parser.add_argument(
        '--predict_only', dest='predict_only', action='store_true',
        help="Run prediction only; checkpointed model must exist.")
    parser.set_defaults(predict_only=False)
    parser.add_argument(
        '--prediction_img_dir', type=str, default='prediction_images',
        help="Directory of images to use for predictions")

    # Details of the training configuration.
    parser.add_argument(
        '--num_steps', type=int, default=15000,
        help="How many training steps to run before ending.")
    parser.add_argument(
        '--learning_rate', type=float, default=0.01,
        help="How large a learning rate to use when training.")
    parser.add_argument(
        '--testing_percentage', type=int, default=10,
        help="What percentage of images to use as a test set.")
    parser.add_argument(
        '--validation_percentage', type=int, default=10,
        help="What percentage of images to use as a validation set.")
    parser.add_argument(
        '--eval_step_interval', type=int, default=10,
        help="How often to evaluate the training results.")
    parser.add_argument(
        '--train_batch_size', type=int, default=100,
        help="How many images to train on at a time.")
    parser.add_argument(
        '--test_batch_size', type=int, default=500,
        help="""How many images to test on at a time. This
        test set is only used infrequently to verify
        the overall accuracy of the model.""")
    parser.add_argument(
        '--validation_batch_size', type=int, default=100,
        help="""How many images to use in an evaluation batch. This validation
        set is used much more often than the test set, and is an early
        indicator of how accurate the model is during training.""")

    # File-system cache locations.
    parser.add_argument(
        '--incp_model_dir', type=str, default='/tmp/imagenet',
        help="""Path to classify_image_graph_def.pb,
        imagenet_synset_to_human_label_map.txt, and
        imagenet_2012_challenge_label_map_proto.pbtxt.""")
    parser.add_argument(
        '--bottleneck_dir', type=str, default='/tmp/bottleneck',
        help="Path to cache bottleneck layer values as files.")
    parser.add_argument(
        '--final_tensor_name', type=str, default='final_result',
        help="""The name of the output classification
        layer in the retrained graph.""")

    # Controls the distortions used during training.
    parser.add_argument(
        '--flip_hz', dest='flip_hz', action='store_true',
        help="Flip half of the training images horizontally")
    parser.add_argument(
        '--no-flip_hz', dest='flip_hz', action='store_false',
        help="Don't flip half the training images horizontally.  The default.")
    parser.set_defaults(flip_hz=False)
    parser.add_argument(
        '--random_crop', type=int, default=0,
        help="""A percentage determining how much of a margin to randomly
        crop off the training images.""")
    parser.add_argument(
        '--random_scale', type=int, default=0,
        help="""A percentage determining how much to randomly scale up
        the size of the training images by.""")
    parser.add_argument(
        '--random_brightness', type=int, default=0,
        help="""A percentage determining how much to randomly multiply
        the training image input pixels up or down by.""")

    ARGFLAGS = parser.parse_args()
    tf.app.run()
