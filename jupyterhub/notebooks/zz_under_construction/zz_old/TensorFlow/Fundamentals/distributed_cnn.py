import tensorflow as tf
import numpy as np

# Modules required for file download and extraction
import os
import sys
import tarfile
from six.moves.urllib.request import urlretrieve
from scipy import ndimage


outdir = '/tmp/pipeline/datasets/notmist/'

def maybe_download(filename, url, force=False):
  """Download a file if not present."""
  if force or not os.path.exists(outdir + filename):
    filename, _ = urlretrieve(url + filename, outdir + filename)
    print('\nDownload complete for {}'.format(filename))
  else:
    print('File {} already present.'.format(filename))
  print(filename)
  return outdir + filename

def maybe_extract(filename, force=False):
  root = os.path.splitext(os.path.splitext(filename)[0])[0]  # remove .tar.gz
  if os.path.isdir(root) and not force:
    # You may override by setting force=True.
    print('{} already present - don\'t need to extract {}.'.format(root, filename))
  else:
    print('Extracting data for {}. This may take a while. Please wait.'.format(root))
    print(filename)
    tar = tarfile.open(filename)
    sys.stdout.flush()
    tar.extractall(root[0:root.rfind('/') + 1])
    tar.close()
  data_folders = [
    os.path.join(root, d) for d in sorted(os.listdir(root))
    if os.path.isdir(os.path.join(root, d))]
  print(data_folders)
  return data_folders

# Locations to download data:
url = 'http://yaroslavvb.com/upload/notMNIST/'

# Download two datasets
train_zip_path = maybe_download('notMNIST_small.tar.gz', url)

# Extract datasets
train_folders = maybe_extract(train_zip_path)



image_height = 28  # Pixel height of images
image_width = 28  # Pixel width of images
pixel_depth = 255.0  # Number of levels per pixel
expected_img_shape = (image_height, image_width)  # Black and white image, no 3rd dimension
num_labels = len(train_folders)

def load_image_folder(folder):
  """Load the data for a single image label."""

  # Create a list of image paths inside the folder
  image_files = os.listdir(folder)
  # Create empty numpy array to hold data
  dataset = np.ndarray(shape=(len(image_files), image_height, image_width),
                         dtype=np.float32)
  num_images = 0  # Counter for number of successful images loaded
  for image in image_files:
    image_file = os.path.join(folder, image)
    try:
      # Read in image pixel data as floating point values
      image_data = ndimage.imread(image_file).astype(float)
      # Scale values: [0.0, 255.0] => [-1.0, 1.0]
      image_data = (image_data - pixel_depth / 2) / (pixel_depth / 2)
      if image_data.shape != expected_img_shape:
        print('File {} has unexpected dimensions: '.format(str(image_data.shape)))
        continue
      # Add image to the numpy array dataset
      dataset[num_images, :, :] = image_data
      num_images = num_images + 1
    except IOError as e:
      print('Could not read:', image_file, ':', e, '- skipping this file and moving on.')

  # Trim dataset to remove unused space
  dataset = dataset[0:num_images, :, :]
  return dataset

def make_data_label_arrays(num_rows, image_height, image_width):
  """
  Creates and returns empty numpy arrays for input data and labels
  """
  if num_rows:
    dataset = np.ndarray((num_rows, image_height, image_width), dtype=np.float32)
    labels = np.ndarray(num_rows, dtype=np.int32)
  else:
    dataset, labels = None, None
  return dataset, labels

def collect_datasets(data_folders):
  datasets = []
  total_images = 0
  for label, data_folder in enumerate(data_folders):
    # Bring all test folder images in as numpy arrays
    dataset = load_image_folder(data_folder)
    num_images = len(dataset)
    total_images += num_images
    datasets.append((dataset, label, num_images))
  return datasets, total_images

def merge_train_test_datasets(datasets, total_images, percent_test):
    num_train = total_images * (1.0 - percent_test)
    num_test = total_images * percent_test
    train_dataset, train_labels = make_data_label_arrays(num_train, image_height, image_width)
    test_dataset, test_labels = make_data_label_arrays(num_test, image_height, image_width)

    train_counter = 0
    test_counter = 0
    dataset_counter = 1
    for dataset, label, num_images in datasets:
      np.random.shuffle(dataset)
      if dataset_counter != len(datasets):
        n_v = num_images // (1.0 / percent_test)
        n_t = num_images - n_v
      else:
        # Last label, make sure dataset sizes match up to what we created
        n_v = len(test_dataset) - test_counter
        n_t = len(train_dataset) - train_counter
      train_dataset[train_counter: train_counter + n_t] = dataset[:n_t]
      train_labels[train_counter: train_counter + n_t] = label
      test_dataset[test_counter: test_counter + n_v] = dataset[n_t: n_t + n_v]
      test_labels[test_counter: test_counter + n_v] = label
      train_counter += n_t
      test_counter += n_v
      dataset_counter += 1
    return train_dataset, train_labels, test_dataset, test_labels

train_test_datasets, train_test_total_images = collect_datasets(train_folders)

train_dataset, train_labels, test_dataset, test_labels = \
  merge_train_test_datasets(train_test_datasets, train_test_total_images, 0.1)

# Convert data examples into 3-D tensors
num_channels = 1  # grayscale
def reformat(dataset, labels):
  dataset = dataset.reshape(
    (-1, image_height, image_width, num_channels)).astype(np.float32)
  labels = (np.arange(num_labels) == labels[:,None]).astype(np.float32)
  return dataset, labels

train_dataset, train_labels = reformat(train_dataset, train_labels)
test_dataset, test_labels = reformat(test_dataset, test_labels)

print('Training set', train_dataset.shape, train_labels.shape)
print('Test set', test_dataset.shape, test_labels.shape)

def shuffle_data_with_labels(dataset, labels):
    indices = range(len(dataset))
    np.random.shuffle(indices)
    new_data = np.ndarray(dataset.shape, dataset.dtype)
    new_labels = np.ndarray(labels.shape, dataset.dtype)
    n = 0
    for i in indices:
        new_data[n] = dataset[i]
        new_labels[n] = labels[i]
        n += 1
    return new_data, new_labels

train_dataset, train_labels = shuffle_data_with_labels(train_dataset, train_labels)

CLUSTER_SPEC= """
{
    'ps' : ['tensorflow0.pipeline.io:8888', 'tensorflow1.pipeline.io:8888'],
    'worker' : ['tensorflow2.pipeline.io:8888','tensorflow3.pipeline.io:8888'],
}
"""
import ast

cluster_spec = ast.literal_eval(CLUSTER_SPEC)
spec = tf.train.ClusterSpec(cluster_spec)
workers = ['/job:worker/task:{}'.format(i) for i in range(len(cluster_spec['worker']))]
param_servers = ['/job:ps/task:{}'.format(i) for i in range(len(cluster_spec['ps']))]

sess_config = tf.ConfigProto(
    allow_soft_placement=True,
    log_device_placement=True)

graph = tf.Graph()
print_versions = []
with graph.as_default():
    for worker in workers:
        with tf.device(worker):
            version = tf.Print(["active"], ["version"], message="worker is ")
            print_versions.append(version)

target = "grpc://tensorflow0.pipeline.io:8888"

with tf.Session(target, graph=graph, config=sess_config) as session:
    print(session.run(print_versions))


patch_size = 5
depth = 16
num_hidden = 64


def variable_summaries(var, name):
    with tf.name_scope("summaries"):
      mean = tf.reduce_mean(var)
      tf.scalar_summary('mean/' + name, mean)
      with tf.name_scope('stddev'):
        stddev = tf.sqrt(tf.reduce_sum(tf.square(var - mean)))
      tf.scalar_summary('sttdev/' + name, stddev)
      tf.scalar_summary('max/' + name, tf.reduce_max(var))
      tf.scalar_summary('min/' + name, tf.reduce_min(var))
      tf.histogram_summary(name, var)

def weight_variable(shape, name):
    return tf.Variable(tf.truncated_normal(
      shape, stddev=0.1), name=name)

def bias_variable(shape, name):
    return tf.Variable(tf.constant(0.1, shape=shape), name=name)

def conv2D(data, W, b):
    conv = tf.nn.conv2d(data, W, [1, 2, 2, 1], padding='SAME', name="2DConvolution")
    return tf.nn.relu(conv + b, name="ReLu")

def fc(data, W, b):
    shape = data.get_shape().as_list()
    reshape = tf.reshape(data, [-1, shape[1] * shape[2] * shape[3]])
    return tf.nn.relu(tf.nn.xw_plus_b(reshape, W, b), name="ReLu")

def model(data):
    with tf.name_scope("Layer1"):
      activations = conv2D(data, layer1_weights, layer1_biases)
      dropped = tf.nn.dropout(activations, 0.5, name="Dropout")

    with tf.name_scope("Layer2"):
      activations = conv2D(dropped, layer2_weights, layer2_biases)
      dropped = tf.nn.dropout(activations, 0.5, name="Dropout")

    with tf.name_scope("Layer3"):
      activations = fc(dropped, layer3_weights, layer3_biases)
    return tf.matmul(activations, layer4_weights) + layer4_biases

graph = tf.Graph()

# divide the input across the cluster:
reduce_loss = []
with graph.as_default():
    device_setter = tf.train.replica_device_setter(cluster=cluster_spec)
    with tf.device(device_setter):
        global_step = tf.Variable(0, name="global_step", trainable=False)

        # Input data.
        input_data = tf.placeholder(
        tf.float32, shape=(None, image_height, image_width, num_channels), name="input_data")
        input_labels = tf.placeholder(tf.float32, shape=(None, num_labels), name="input_labels")

        layer1_weights = weight_variable([patch_size, patch_size, num_channels, depth], "L1Weights")
        layer1_biases =  bias_variable([depth], "L1Bias")

        layer2_weights = weight_variable([patch_size, patch_size, depth, depth], "L2Weights")
        layer2_biases = bias_variable([depth], "L2Bias")

        layer3_weights = weight_variable([image_height // 4 * image_width // 4 * depth, num_hidden], "L3Weights")
        layer3_biases = bias_variable([num_hidden], "L3Bias")

        layer4_weights = weight_variable([num_hidden, num_labels], "L4Weights")
        layer4_biases = bias_variable([num_labels], "L4Bias")

        splitted = tf.split(0, len(workers), input_data)
        label_splitted = tf.split(0, len(workers), input_labels)

        # Add variable summaries
        for v in [layer1_weights, layer2_weights, layer3_weights, layer4_weights, layer1_biases, layer2_biases, layer3_biases, layer4_biases]:
            variable_summaries(v, v.name)

    for idx, (portion, worker, label_portion) in enumerate(zip(splitted, workers, label_splitted)):
        with tf.device(worker):
          # Training computation.
          local_reduce = tf.Print(portion, ["portion"], message="portion is")

          logits = model(portion)
          loss = tf.nn.softmax_cross_entropy_with_logits(logits, label_portion)

          loss = tf.Print(loss, [tf.reduce_sum(loss), global_step], message="loss, global_step = ")
          reduce_loss.append(loss)

    with tf.device(device_setter):
        # Optimizer.
        mean_loss = tf.reduce_mean(tf.pack(reduce_loss))
        optimizer = tf.train.RMSPropOptimizer(0.01).minimize(mean_loss, global_step=global_step)
        init = tf.initialize_all_variables()

        # Predictions for the training and test data.
        model_prediction = tf.nn.softmax(logits, name="prediction")
        label_prediction = tf.argmax(model_prediction, 1, name="predicted_label")

        with tf.name_scope('summaries'):
            tf.scalar_summary('loss', mean_loss)

        with tf.name_scope('accuracy'):
            correct_prediction = tf.equal(label_prediction, tf.argmax(label_portion, 1))
            model_accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
            tf.scalar_summary('accuracy', model_accuracy)

        merged_summaries = tf.merge_all_summaries()

sv = tf.train.Supervisor(is_chief=True,
                         graph=graph,
                         logdir="/tmp/cnn_distributed",
                         init_op=init,
                         global_step=global_step)
# Directory to export TensorBoard summary statistics, graph data, etc.
TB_DIR = '/tmp/tensorboard/tf_cnn'

num_steps = 1000
batch_size = 256

with sv.prepare_or_wait_for_session(target, config=sess_config) as session:
    writer = tf.train.SummaryWriter(TB_DIR, graph=session.graph)

    for step in range(num_steps):

        offset = (step * batch_size) % (train_labels.shape[0] - batch_size)
        batch_data = train_dataset[offset:(offset + batch_size), :, :, :]
        batch_labels = train_labels[offset:(offset + batch_size), :]

        feed_dict = {input_data : batch_data, input_labels : batch_labels}

        _, l, g_step = session.run(
        [optimizer, loss, global_step], feed_dict=feed_dict)

        if step % 50 == 0:
            print('Minibatch loss at global_step %s: %s' % (g_step, np.mean(l)))

    test_dict = {input_data : test_dataset, input_labels : test_labels}

    test_accuracy = session.run(model_accuracy, feed_dict=test_dict)
    print('Test accuracy: {}'.format(test_accuracy))

    writer.close()


