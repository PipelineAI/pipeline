from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import argparse
import tensorflow as tf
import numpy as np
import os

if __name__ == '__main__':

    parser = argparse.ArgumentParser()
    parser.add_argument('--learning_rate', type=float, help='Learning Rate', default=0.025)
    parser.add_argument('--num_samples', type=int, help='Num Samples', default=100000)

    from datetime import datetime
    version = int(datetime.now().strftime("%s"))
    parser.add_argument('--rundir', type=str, help='Run Directory', default='runs/%s' % version)

    FLAGS, unparsed = parser.parse_known_args()
    print(FLAGS)
    print(unparsed)

    num_samples = FLAGS.num_samples
    learning_rate = FLAGS.learning_rate

    tf.reset_default_graph()

    config = tf.ConfigProto(
      allow_soft_placement = True,
      log_device_placement=True
    )
    print(config)

    sess = tf.Session(config=config)
    print(sess)

    x_train = np.random.rand(num_samples).astype(np.float32)
    print(x_train)

    noise = np.random.normal(scale=0.01, size=len(x_train))

    y_train = x_train * 0.1 + 0.3 + noise
    print(y_train)

    x_test = np.random.rand(len(x_train)).astype(np.float32)
    print(x_test)

    noise = np.random.normal(scale=.01, size=len(x_train))

    y_test = x_test * 0.1 + 0.3 + noise
    print(y_test)

    with tf.device("/cpu:0"):
        W = tf.get_variable(shape=[], name='weights')
        print(W)

        b = tf.get_variable(shape=[], name='bias')
        print(b)

        x_observed = tf.placeholder(shape=[None],
                                    dtype=tf.float32,
                                    name='x_observed')
        print(x_observed)

        y_pred = W * x_observed + b
        print(y_pred)

    with tf.device("/cpu:0"):
        y_observed = tf.placeholder(shape=[None], dtype=tf.float32, name='y_observed')
        print(y_observed)

        loss_op = tf.reduce_mean(tf.square(y_pred - y_observed))
        optimizer_op = tf.train.GradientDescentOptimizer(learning_rate)

        train_op = optimizer_op.minimize(loss_op)

        print("Loss Scalar: ", loss_op)
        print("Optimizer Op: ", optimizer_op)
        print("Train Op: ", train_op)

    with tf.device("/cpu:0"):
        init_op = tf.global_variables_initializer()
        print(init_op)

    sess.run(init_op)
    print("Initial random W: %f" % sess.run(W))
    print("Initial random b: %f" % sess.run(b))

    def test(x, y):
        return sess.run(loss_op, feed_dict={x_observed: x, y_observed: y})

    test(x_train, y_train)

    loss_summary_scalar_op = tf.summary.scalar('loss', loss_op)
    loss_summary_merge_all_op = tf.summary.merge_all()

    train_summary_writer = tf.summary.FileWriter(FLAGS.rundir + "/train",
                                                 graph=tf.get_default_graph())

    test_summary_writer = tf.summary.FileWriter(FLAGS.rundir + "/test",
                                                graph=tf.get_default_graph())

    with tf.device("/cpu:0"):
        run_metadata = tf.RunMetadata()
        max_steps = 401
        for step in range(max_steps):
            if (step < max_steps - 1):
                test_summary_log, _ = sess.run([loss_summary_merge_all_op, loss_op], feed_dict={x_observed: x_test, y_observed: y_test})
                train_summary_log, _ = sess.run([loss_summary_merge_all_op, train_op], feed_dict={x_observed: x_train, y_observed: y_train})
            else:
                test_summary_log, _ = sess.run([loss_summary_merge_all_op, loss_op], feed_dict={x_observed: x_test, y_observed: y_test})
                train_summary_log, _ = sess.run([loss_summary_merge_all_op, train_op], feed_dict={x_observed: x_train, y_observed: y_train},
                                                options=tf.RunOptions(trace_level=tf.RunOptions.FULL_TRACE),
                                                run_metadata=run_metadata)
            if step % 10 == 0:
                print(step, sess.run([W, b]))
                train_summary_writer.add_summary(train_summary_log, step)
                train_summary_writer.flush()
                test_summary_writer.add_summary(test_summary_log, step)
                test_summary_writer.flush()

    from tensorflow.python.saved_model import utils
    from tensorflow.python.saved_model import signature_constants
    from tensorflow.python.saved_model import signature_def_utils

    graph = tf.get_default_graph()

    x_observed = graph.get_tensor_by_name('x_observed:0')
    y_pred = graph.get_tensor_by_name('add:0')

    inputs_map = {'inputs': x_observed}
    outputs_map = {'outputs': y_pred}

    prediction_signature = signature_def_utils.predict_signature_def(inputs=inputs_map,
                                                                     outputs=outputs_map)

    from tensorflow.python.saved_model import builder as saved_model_builder
    from tensorflow.python.saved_model import tag_constants

    fully_optimized_saved_model_path = 'versions/%s' % version
    print(fully_optimized_saved_model_path)

    builder = saved_model_builder.SavedModelBuilder(fully_optimized_saved_model_path)
    builder.add_meta_graph_and_variables(sess,
                                         [tag_constants.SERVING],
                                         signature_def_map={'predict':prediction_signature,
    signature_constants.DEFAULT_SERVING_SIGNATURE_DEF_KEY:prediction_signature},
                                         clear_devices=True,
    )

    builder.save(as_text=False)
    print('')
    print('Model training completed and saved here:  ./%s' % version)
    print('')
