#!/usr/bin/env python

import numpy as np
import os
import tensorflow as tf
from tensorflow.contrib.session_bundle import exporter
import time

flags = tf.app.flags
FLAGS = flags.FLAGS
flags.DEFINE_integer("batch_size", 10, "The batch size to train")
flags.DEFINE_integer("epoch_number", 10, "Number of epochs to run trainer")
flags.DEFINE_integer("steps_to_validate", 1,
                     "Steps to validate and print loss")
flags.DEFINE_string("checkpoint_dir", "./checkpoint/",
                    "indicates the checkpoint dirctory")
flags.DEFINE_string("model_path", "./model/", "The export path of the model")
flags.DEFINE_integer("export_version", 1, "The version number of the model")
flags.DEFINE_integer("benchmark_batch_size", 1, "")
flags.DEFINE_integer("benchmark_test_number", 10000, "")

def main():
  # Define training data
  x = np.ones(FLAGS.batch_size)
  y = np.ones(FLAGS.batch_size)

  # Define the model
  X = tf.placeholder(tf.float32, shape=[None])
  Y = tf.placeholder(tf.float32, shape=[None])
  w = tf.Variable(1.0, name="weight")
  b = tf.Variable(1.0, name="bias")
  loss = tf.square(Y - tf.mul(X, w) - b)
  train_op = tf.train.GradientDescentOptimizer(0.01).minimize(loss)
  predict_op  = tf.mul(X, w) + b

  saver = tf.train.Saver()
  checkpoint_dir = FLAGS.checkpoint_dir
  checkpoint_file = checkpoint_dir + "/checkpoint.ckpt"
  if not os.path.exists(checkpoint_dir):
    os.makedirs(checkpoint_dir)

  # Start the session
  with tf.Session() as sess:
    sess.run(tf.initialize_all_variables())

    ckpt = tf.train.get_checkpoint_state(checkpoint_dir)
    if ckpt and ckpt.model_checkpoint_path:
      print("Continue training from the model {}".format(ckpt.model_checkpoint_path))
      saver.restore(sess, ckpt.model_checkpoint_path)

    # Start training
    start_time = time.time()

    request_number = FLAGS.benchmark_test_number
    batch_size = FLAGS.benchmark_batch_size
    predict_x = np.ones(batch_size)

    start_time = time.time()
    for i in range(request_number):
      sess.run(predict_op, feed_dict={X: predict_x})

    end_time = time.time()
    print("Average latency is: {} ms".format((end_time - start_time) * 1000 / request_number))


if __name__ == "__main__":
  main()
