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

def main():
  # Define training data
  x = np.ones(FLAGS.batch_size)
  y = np.ones(FLAGS.batch_size)

  # Define the model
  X = tf.placeholder(tf.float32, shape=[None], name="X")
  Y = tf.placeholder(tf.float32, shape=[None], name="yhat")
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

    saver_def = saver.as_saver_def()
    print(saver_def.filename_tensor_name)
    print(saver_def.restore_op_name)

    # Start training
    start_time = time.time()
    for epoch in range(FLAGS.epoch_number):
      sess.run(train_op, feed_dict={X: x, Y: y})

      # Start validating
      if epoch % FLAGS.steps_to_validate == 0:
        end_time = time.time()
        print("[{}] Epoch: {}".format(end_time - start_time, epoch))

        saver.save(sess, checkpoint_file)
        tf.train.write_graph(sess.graph_def, checkpoint_dir, 'trained_model.pb', as_text=False)
        tf.train.write_graph(sess.graph_def, checkpoint_dir, 'trained_model.txt', as_text=True)  

        start_time = end_time

    # Print model variables
    w_value, b_value = sess.run([w, b])
    print("The model of w: {}, b: {}".format(w_value, b_value))

    # Export the model
    print("Exporting trained model to {}".format(FLAGS.model_path))
    model_exporter = exporter.Exporter(saver)
    model_exporter.init(
      sess.graph.as_graph_def(),
      named_graph_signatures={
        'inputs': exporter.generic_signature({"features": X}),
        'outputs': exporter.generic_signature({"prediction": predict_op})
      })
    model_exporter.export(FLAGS.model_path, tf.constant(FLAGS.export_version), sess)
    print('Done exporting!')

if __name__ == "__main__":
  main()
