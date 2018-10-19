"""
This file was scratch work. Check out main.py for refactored version.

Weird phenomenon: This file runs about three times slower than the main.py
version, but not sure why. The main parts of the code are similar.
Needs more investigation.
"""

import tensorflow as tf
import numpy as np
from scipy import ndimage
from scipy.misc import imresize
import time
import os

# GET DATA
num_labels = 2
pixel_depth = 255.0
resized_height = 64
resized_width = 64
folder = 'train/'
files = [folder + f for f in os.listdir(folder)]

# QUEUE GRAPH
batch_size = 32
capacity = 2000
num_channels = 3
queue_graph = tf.Graph()
with queue_graph.as_default():
    with queue_graph.device('/cpu:0'):
        filename_queue = tf.train.string_input_producer(files)
        reader = tf.WholeFileReader()
        key, value = reader.read(filename_queue)
        image = tf.image.decode_jpeg(value, channels=num_channels)
        image = (tf.cast(image, tf.float32) - 128.0) / 255.0
        resized = tf.image.resize_images([image], resized_height, resized_width)
        image_batch, label_batch = tf.train.batch(
                                        [resized, key],
                                        batch_size=batch_size,
                                        capacity=capacity)
        image_squeezed = tf.squeeze(image_batch)
        batch_data = tf.identity(image_squeezed, name='batch_data')
        batch_labels = tf.identity(label_batch)

# TODO: Replace with reading in a GraphDef or from another file
train_graph = tf.Graph()
with train_graph.as_default():
    input_p = tf.placeholder(tf.float32, [None, resized_height, resized_width,
                             num_channels], name='input')
    label_p = tf.placeholder(tf.int32, [None], name='label')
    one_hot_labels = tf.one_hot(label_p, num_labels)
    shape = input_p.get_shape().as_list()
    reshaped = tf.reshape(input_p, [-1, shape[1] * shape[2] * shape[3]])
    weights = tf.Variable(tf.truncated_normal([shape[1] * shape[2] * shape[3],
                                              num_labels], stddev=0.1))
    bias = tf.Variable(tf.constant(0.0, shape=[num_labels]))
    logits = tf.matmul(reshaped, weights) + bias
    loss = tf.reduce_mean(
        tf.nn.softmax_cross_entropy_with_logits(logits, one_hot_labels,
                                                name='loss'))
    train = tf.train.GradientDescentOptimizer(0.01).minimize(loss)
    init = tf.initialize_all_variables()

    model_prediction = tf.nn.softmax(logits)
    label_prediction = tf.argmax(model_prediction, 1, name='predicted_label')
    correct_prediction = tf.equal(label_prediction, tf.argmax(one_hot_labels,
                                                              1))
    model_accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))

queue_sess = tf.Session(graph=queue_graph)
train_sess = tf.Session(graph=train_graph)
train_sess.run(init)
coord = tf.train.Coordinator()
threads = tf.train.start_queue_runners(sess=queue_sess, coord=coord)
try:
    time_list = []
    while not coord.should_stop():
        t_start = time.clock()
        data_input, labels = queue_sess.run([image_squeezed, batch_labels])
        labels = [
            1
            if b'dog' in s
            else 0
            for s in labels
        ]
        feed_dict = {input_p: data_input, label_p: labels}
        loss_val, _, acc = train_sess.run([loss, train, model_accuracy],
                                          feed_dict=feed_dict)
        t_end = time.clock()
        time_list.append(t_end - t_start)
        if len(time_list) >= 30:
            print(sum(time_list) / len(time_list))
            time_list = []
except tf.errors.OutOfRangeError:
    print('Done training -- epoch limit reached')
finally:
    # When done, ask the threads to stop
    coord.request_stop()

coord.join(threads)
queue_sess.close()
