import tensorflow as tf

filename_queue = tf.train.string_input_producer([
  "hdfs://127.0.0.1:39000/linear/training.csv",
  "hdfs://127.0.0.1:39000/linear/validation.csv",
])

reader = tf.TextLineReader()
key, value = reader.read(filename_queue)

x_observed, y_pred = tf.decode_csv(value, [[0.0],[0.0]])

with tf.Session() as sess:
  # Start populating the filename queue.
  coord = tf.train.Coordinator()
  threads = tf.train.start_queue_runners(coord=coord)
  try:
    for i in range(20):
      # Retrieve and print a single instance:
      example, label = sess.run([x_observed, y_pred])
      print(example, label)
  except tf.errors.OutOfRangeError:
    print("Done!")
  finally:
    coord.request_stop()
    coord.join(threads)
