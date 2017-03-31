import tensorflow as tf
from tensorflow.python.client import timeline
import numpy as np

tf.reset_default_graph()

num_samples=100000

x_train = np.random.rand(num_samples).astype(np.float32)
print(x_train)

noise = np.random.normal(scale=0.01, size=len(x_train))

y_train = x_train * 0.1 + 0.3 + noise
print(y_train)

x_test = np.random.rand(len(x_train)).astype(np.float32)
print(x_test)

noise = np.random.normal(scale=0.01, size=len(x_train))

y_test = x_test * 0.1 + 0.3 + noise
print(y_test)

with tf.device("/cpu:0"):
  W = tf.get_variable(shape=[], name='weights')
  print(W)

  b = tf.get_variable(shape=[], name='bias')
  print(b)

  x_observed = tf.placeholder(shape=[None], dtype=tf.float32, name='x_observed')
  print(x_observed)

with tf.device("/job:localhost/replica:0/task:0/device:XLA_GPU:0"):
  y_pred = W * x_observed + b
  print(y_pred)

with tf.device("/job:localhost/replica:0/task:0/device:XLA_GPU:0"):
  y_observed = tf.placeholder(shape=[None], dtype=tf.float32, name='y_observed')
  print(y_observed)

  loss_op = tf.reduce_mean(tf.square(y_pred - y_observed))

  # Create an optimizer.
  optimizer_op = tf.train.GradientDescentOptimizer(0.025)  

  # Create an operation that minimizes loss.
  train_op = optimizer_op.minimize(loss_op)  

  # 'loss', 'optimizer' and 'train' are.
  print("loss:", loss_op)
  print("optimizer:", optimizer_op)
  print("train:", train_op)

with tf.device("/cpu:0"):
  init_op = tf.global_variables_initializer()
  print(init_op)

config = tf.ConfigProto(
  log_device_placement=True,
)

config.gpu_options.allow_growth=True
config.graph_options.optimizer_options.global_jit_level = tf.OptimizerOptions.ON_1

print(config)

from datetime import datetime
version = int(datetime.now().strftime("%s"))
print(version)

train_summary_writer = tf.summary.FileWriter('/root/tensorboard/linear/xla/%s/train' % version, graph=tf.get_default_graph())

test_summary_writer = tf.summary.FileWriter('/root/tensorboard/linear/xla/%s/test' % version, graph=tf.get_default_graph())

sess = tf.Session(config=config)
sess.run(init_op)

print(sess.run(W))
print(sess.run(b))

def test(x, y):
  return sess.run(loss_op, feed_dict={x_observed: x, y_observed: y})

test(x=x_test, y=y_test)

loss_summary_scalar_op = tf.summary.scalar('loss', loss_op)
loss_summary_merge_all_op = tf.summary.merge_all()

max_steps = 350 

for step in range(max_steps):
  # Run the training op; feed the training data into the graph
  if (step < max_steps):
    train_summary_log, _ = sess.run([loss_summary_merge_all_op, train_op], feed_dict={x_observed: x_train, y_observed: y_train})
  else:  
    train_summary_log, _ = sess.run([loss_summary_merge_all_op, train_op], feed_dict={x_observed: x_train, y_observed: y_train})

# The following take a relatively long time, so do them at periodic intervals
  if step % 5 == 0:
    print(step, sess.run([W, b]))
    train_summary_writer.add_summary(train_summary_log, step)
    train_summary_writer.flush()

test(x=x_test, y=y_test)
