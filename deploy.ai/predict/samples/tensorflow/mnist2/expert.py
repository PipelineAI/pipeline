import argparse
import json

import tensorflow as tf

from tensorflow.examples.tutorials.mnist import input_data

def init_flags():
    global FLAGS
    parser = argparse.ArgumentParser()
    parser.add_argument("--datadir", default="/tmp/MNIST_data",)
    parser.add_argument("--rundir", default="/tmp/MNIST_train")
    parser.add_argument("--batch_size", type=int, default=100)
    parser.add_argument("--epochs", type=int, default=10)
    parser.add_argument("--prepare", dest='just_data', action="store_true")
    parser.add_argument("--test", action="store_true")
    FLAGS, _ = parser.parse_known_args()

def init_data():
    global mnist
    mnist = input_data.read_data_sets(FLAGS.datadir, one_hot=True)

def init_train():
    init_model()
    init_train_op()
    init_eval_op()
    init_summaries()
    init_collections()
    init_session()

def init_model():
    global x, y

    # Input layer
    x = tf.placeholder(tf.float32, [None, 784])

    # First convolutional layer
    W_conv1 = weight_variable([5, 5, 1, 32])
    b_conv1 = bias_variable([32])
    x_image = tf.reshape(x, [-1, 28, 28, 1])
    h_conv1 = tf.nn.relu(conv2d(x_image, W_conv1) + b_conv1)
    h_pool1 = max_pool_2x2(h_conv1)

    # Second convolutional layer
    W_conv2 = weight_variable([5, 5, 32, 64])
    b_conv2 = bias_variable([64])
    h_conv2 = tf.nn.relu(conv2d(h_pool1, W_conv2) + b_conv2)
    h_pool2 = max_pool_2x2(h_conv2)

    # First fully connected layer
    W_fc1 = weight_variable([7 * 7 * 64, 1024])
    b_fc1 = bias_variable([1024])
    h_pool2_flat = tf.reshape(h_pool2, [-1, 7 * 7 * 64])
    h_fc1 = tf.nn.relu(tf.matmul(h_pool2_flat, W_fc1) + b_fc1)

    # Dropout
    keep_prob = tf.placeholder_with_default(1.0, [])
    h_fc1_drop = tf.nn.dropout(h_fc1, keep_prob)

    # Output layer
    W_fc2 = weight_variable([1024, 10])
    b_fc2 = bias_variable([10])
    y = tf.matmul(h_fc1_drop, W_fc2) + b_fc2

def weight_variable(shape):
    return tf.Variable(tf.truncated_normal(shape, stddev=0.1))

def bias_variable(shape):
    return tf.Variable(tf.constant(0.1, shape=shape))

def conv2d(x, W):
    return tf.nn.conv2d(x, W, strides=[1, 1, 1, 1], padding="SAME")

def max_pool_2x2(x):
    return tf.nn.max_pool(
        x, ksize=[1, 2, 2, 1], strides=[1, 2, 2, 1], padding="SAME")

def init_train_op():
    global y_, loss, train_op
    y_ = tf.placeholder(tf.float32, [None, 10])
    loss = tf.reduce_mean(
             tf.nn.softmax_cross_entropy_with_logits(
               logits=y, labels=y_))
    train_op = tf.train.AdamOptimizer(1e-4).minimize(loss)

def init_eval_op():
    global accuracy
    correct_prediction = tf.equal(tf.argmax(y, 1), tf.argmax(y_, 1))
    accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))

def init_summaries():
    init_inputs_summary()
    init_op_summaries()
    init_summary_writers()

def init_inputs_summary():
    tf.summary.image("inputs", tf.reshape(x, [-1, 28, 28, 1]), 10)

def init_op_summaries():
    tf.summary.scalar("loss", loss)
    tf.summary.scalar("accuracy", accuracy)

def init_summary_writers():
    global summaries, train_writer, validation_writer
    summaries = tf.summary.merge_all()
    train_writer = tf.summary.FileWriter(
        FLAGS.rundir + "/train",
        tf.get_default_graph())
    validation_writer = tf.summary.FileWriter(
        FLAGS.rundir + "/validation")

def init_collections():
    tf.add_to_collection("inputs", json.dumps({"image": x.name}))
    tf.add_to_collection("outputs", json.dumps({"prediction": y.name}))
    tf.add_to_collection("x", x.name)
    tf.add_to_collection("y_", y_.name)
    tf.add_to_collection("accuracy", accuracy.name)

def init_session():
    global sess
    sess = tf.Session()
    sess.run(tf.global_variables_initializer())

def train():
    steps = (mnist.train.num_examples // FLAGS.batch_size) * FLAGS.epochs
    for step in range(steps + 1):
        images, labels = mnist.train.next_batch(FLAGS.batch_size)
        batch = {x: images, y_: labels}
        sess.run(train_op, batch)
        maybe_log_accuracy(step, batch)
        maybe_save_model(step)

def maybe_log_accuracy(step, last_training_batch):
    if step % 20 == 0:
        evaluate(step, last_training_batch, train_writer, "training")
        validation_data = {
            x: mnist.validation.images,
            y_: mnist.validation.labels
        }
        evaluate(step, validation_data, validation_writer, "validation")

def evaluate(step, data, writer, name):
    accuracy_val, summary = sess.run([accuracy, summaries], data)
    writer.add_summary(summary, step)
    print "Step %i: %s=%f" % (step, name, accuracy_val)

def maybe_save_model(step):
    epoch_step = mnist.train.num_examples / FLAGS.batch_size
    if step != 0 and step % epoch_step == 0:
        save_model()

def save_model():
    print "Saving trained model"
    tf.gfile.MakeDirs(FLAGS.rundir + "/model")
    tf.train.Saver().save(sess, FLAGS.rundir + "/model/export")

def init_test():
    init_session()
    init_exported_collections()

def init_exported_collections():
    global x, y_, accuracy
    saver = tf.train.import_meta_graph(FLAGS.rundir + "/model/export.meta")
    saver.restore(sess, FLAGS.rundir + "/model/export")
    x = sess.graph.get_tensor_by_name(tf.get_collection("x")[0])
    y_ = sess.graph.get_tensor_by_name(tf.get_collection("y_")[0])
    accuracy = sess.graph.get_tensor_by_name(tf.get_collection("accuracy")[0])

def test():
    data = {x: mnist.test.images, y_: mnist.test.labels}
    test_accuracy = sess.run(accuracy, data)
    print "Test accuracy=%f" % test_accuracy

if __name__ == "__main__":
    init_flags()
    init_data()
    if FLAGS.just_data:
        pass
    elif FLAGS.test:
        init_test()
        test()
    else:
        init_train()
        train()
