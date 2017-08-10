import argparse
import sys
import numpy as np
import tensorflow as tf

def main(_):
  tf.logging.set_verbosity(tf.logging.INFO)

  ps_hosts = FLAGS.ps_hosts.split(",")
  worker_hosts = FLAGS.worker_hosts.split(",")


  # Create a cluster from the parameter server and worker hosts.
  cluster = tf.train.ClusterSpec({"ps": ps_hosts, "worker": worker_hosts})

  # Create and start a server for the local task.
  server = tf.train.Server(cluster,
                           job_name=FLAGS.job_name,
                           task_index=FLAGS.task_index)

  config = tf.ConfigProto(allow_soft_placement=True,
                          log_device_placement=True,
                          intra_op_parallelism_threads=10,
                          inter_op_parallelism_threads=10,
                          device_filters=["/job:ps", "/job:worker/task:%d" % FLAGS.task_index])
  # GPU Config
  # config.gpu_options.allow_growth=True
  # config.gpu_options.per_process_gpu_memory_fraction = 0.5

  if FLAGS.job_name == "ps":
    server.join()
  elif FLAGS.job_name == "worker":

    # Assigns ops to the local worker by default.
    with tf.device(tf.train.replica_device_setter(
        ps_tasks=len(ps_hosts),
        ps_strategy=tf.contrib.training.GreedyLoadBalancingStrategy(load_fn=lambda _: 1, num_tasks=len(ps_hosts)),
        worker_device="/job:worker/task:%d" % FLAGS.task_index,
        cluster=cluster)):

      # Build model...
      W = tf.Variable(0.0, name="weights")
      b = tf.Variable(0.0, name="bias")

      x_observed = tf.placeholder(shape=[None], 
                                  dtype=tf.float32, 
                                  name='x_observed')

      y_pred = W * x_observed + b

      y_observed = tf.placeholder(shape=[None], dtype=tf.float32, name='y_observed')

      global_step = tf.contrib.framework.get_or_create_global_step()

      loss_op = tf.reduce_mean(tf.square(y_pred - y_observed))
      optimizer_op = tf.train.GradientDescentOptimizer(FLAGS.learning_rate)
      train_op = optimizer_op.minimize(loss_op, global_step)  
      init_op = tf.global_variables_initializer()

      print('Loss Scalar: ', loss_op)
      print('Optimizer Op: ', optimizer_op)
      print('Train Op: ', train_op)
      print('Init Op: ', init_op)

    # The StopAtStepHook handles stopping after running given steps.
    stop_hook = tf.train.StopAtStepHook(num_steps=FLAGS.num_steps)
    logging_hook = tf.train.LoggingTensorHook([W,b], every_n_iter=1000, at_end=True)

    hooks=[logging_hook, stop_hook]

    version = 0

    # The MonitoredTrainingSession takes care of session initialization,
    # restoring from a checkpoint, saving to a checkpoint, and closing when done
    # or an error occurs.
    with tf.train.MonitoredTrainingSession(master=server.target,
                                           config=config,
                                           is_chief=(FLAGS.task_index == 0),
                                           checkpoint_dir="./model/%s" % version,
                                           hooks=hooks) as mon_sess:
      # Generate random samples
      x_train = np.random.rand(FLAGS.num_samples).astype(np.float32)
      noise = np.random.normal(scale=0.01, size=len(x_train))
      y_train = x_train * 0.1 + 0.3 + noise
      mon_sess.run(init_op)

      while not mon_sess.should_stop():
        # Run a training step asynchronously.
        # See `tf.train.SyncReplicasOptimizer` for additional details on how to
        # perform *synchronous* training.
        # mon_sess.run handles AbortedError in case of preempted PS.
        mon_sess.run(train_op, feed_dict={x_observed: x_train, y_observed: y_train})


if __name__ == "__main__":
  parser = argparse.ArgumentParser()
  parser.register("type", "bool", lambda v: v.lower() == "true")

  # Flags for training params
  parser.add_argument(
      "--num_steps",
      type=int,
      default=10000,
      help="Num steps"
  )

  parser.add_argument(
      "--num_samples",
      type=int,
      default=100000,
      help="Num samples"
  )

  parser.add_argument(
      "--learning_rate",
      type=float,
      default=0.025,
      help="Learning rate"
  )

  # Flags for defining the tf.train.ClusterSpec
  parser.add_argument(
      "--ps_hosts",
      type=str,
      default="",
      help="Comma-separated list of hostname:port pairs"
  )
  parser.add_argument(
      "--worker_hosts",
      type=str,
      default="",
      help="Comma-separated list of hostname:port pairs"
  )
  parser.add_argument(
      "--job_name",
      type=str,
      default="",
      help="One of 'ps', 'worker'"
  )
  # Flags for defining the tf.train.Server
  parser.add_argument(
      "--task_index",
      type=int,
      default=0,
      help="Index of task within the job"
  )
  FLAGS, unparsed = parser.parse_known_args()
  tf.app.run(main=main, argv=[sys.argv[0]] + unparsed)
