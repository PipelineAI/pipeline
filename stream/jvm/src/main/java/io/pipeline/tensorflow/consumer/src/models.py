import tensorflow as tf


def create_model_graph(height, width, channels, num_labels):
    """
    """
    graph = tf.Graph()
    with graph.as_default():
        input_p = tf.placeholder(tf.float32, [None, height, width, channels],
                                 name='input')
        label_p = tf.placeholder(tf.int32, [None], name='label')
        one_hot_labels = tf.one_hot(label_p, num_labels)
        shape = input_p.get_shape().as_list()
        num_pixels = shape[1] * shape[2] * shape[3]
        reshaped = tf.reshape(input_p, [-1, num_pixels])
        weights = tf.Variable(tf.truncated_normal([num_pixels, num_labels],
                                                  stddev=0.1))
        bias = tf.Variable(tf.constant(0.0, shape=[num_labels]))
        logits = tf.matmul(reshaped, weights) + bias
        loss = tf.reduce_mean(
            tf.nn.softmax_cross_entropy_with_logits(logits, one_hot_labels,
                                                    name='loss'))
        train = tf.train.GradientDescentOptimizer(0.01).minimize(loss,
                                                                 name='train')
        init = tf.initialize_all_variables()

        model_prediction = tf.nn.softmax(logits)
        label_prediction = tf.argmax(model_prediction, 1,
                                     name='predicted_label')
        correct_prediction = tf.equal(label_prediction,
                                      tf.argmax(one_hot_labels, 1))
        model_accuracy = tf.reduce_mean(tf.cast(correct_prediction,
                                                tf.float32), name='accuracy')
    return graph


def generic_supervised_graph(input_type, input_shape, label_type, label_shape):
    """
    """
    graph = tf.Graph()
    with graph.as_default():
        input_p = tf.placeholder(input_type, input_shape, name='input')
        label_p = tf.placeholder(label_type, label_shape, name='label')

        loss = tf.identity(input_p, name='loss')
        train = tf.train.GradientDescentOptimizer(0.01).minimize(loss,
                                                                 name='train')
    return graph
