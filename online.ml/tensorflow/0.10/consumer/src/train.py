import tensorflow as tf
import time


def get_op(graph, name):
    """
    Returns the operation(s) in `graph` with `name`
    """
    return graph.get_operation_by_name(name)


def get_tensor(graph, name, output_idx=0):
    """
    Returns the output of the operation in `graph` with `name`
    """
    return get_op(graph, name).outputs[output_idx]


def op_in_types(op, types):
    """

    """
    if type(op) == tf.Tensor:
        op_type = op.op.type
    elif type(op) == tf.Operation:
        op_type = op.type
    else:
        raise TypeError('Object being checked must be of type `tf.Tensor` or '
                        '`tf.Operation`')
    return op in types, op_type


def check_op_type(op, types):
    is_valid, actual_type = op_in_types(op, types)
    if is_valid:
        return
    else:
        raise TypeError('Expected node \'{}\' to be in types {}'
                        ', got type {}'.format([op.name, types, actual_type]))


def create_tensor_dict(graph, names, types=None):
    tensor_dict = {}
    for name in names:
        tensor = get_tensor(graph, name)
        if types is not None:
            check_op_type(tensor, types)
        tensor_dict[name] = tensor
    return tensor_dict


def create_op_dict(graph, names, types=None):
    op_dict = {}
    for name in names:
        op = get_op(graph, name)
        if types is not None:
            check_op_type(op, types)
        op_dict[name] = op
    return op_dict


def create_placeholder_dict(graph, placeholder_pairs):
    """
    Creates a dictionary mapping string names to placeholder operations.

    Args:
        graph: `tf.Graph` object containing operations
        placeholder_pairs: list of tuples

    Returns:
        Dictionary mapping string:`tf.placeholder`
    """
    names = [
        p[1]
        for
        p
        in
        placeholder_pairs
        ]
    return create_tensor_dict(graph, names, types=['Placeholder'])


def get_init_op(graph):
    return get_op(graph, 'init')


def add_variable_summaries(name, op):
    """
    TODO: Add mean, standard deviation, min, max, histogram
    """
    pass


def add_summary_ops(graph, log_ops):
    """
    """
    with graph.as_default():
        for name in log_ops:
            tf.scalar_summary(name, log_ops[name])


def add_merged_summaries(graph):
    """
    """
    with graph.as_default():
        return tf.merge_all_summaries()


def train_model(queue_graph, model_graph, placeholder_list, train_list,
                log_list):
    """
    TODO: Documentation
    TODO: Clean up the all of the `get_operation_by_name()` lines
    TODO: TensorBoard
    """
    batch_data = get_tensor(queue_graph, 'batch_data')
    batch_labels = get_tensor(queue_graph, 'batch_labels')
    init = get_init_op(model_graph)
    placeholder_ops = create_tensor_dict(model_graph, placeholder_list)
    train_ops = create_op_dict(model_graph, train_list)
    train = train_ops['train']
    log_ops = create_tensor_dict(model_graph, log_list)
    add_summary_ops(model_graph, log_ops)
    summaries = add_merged_summaries(model_graph)

    queue_sess = tf.Session(graph=queue_graph)
    model_sess = tf.Session(graph=model_graph)
    model_sess.run(init)
    coord = tf.train.Coordinator()
    threads = tf.train.start_queue_runners(sess=queue_sess, coord=coord)
    try:
        time_list = []
        while not coord.should_stop():
            t_start = time.clock()
            data_input, labels = queue_sess.run([batch_data, batch_labels])
            labels = [
                1
                if b'dog' in s
                else 0
                for s in labels
                ]
            feed_dict = {placeholder_ops[placeholder_list[0]]: data_input,
                         placeholder_ops[placeholder_list[1]]: labels}
            summ, _ = model_sess.run([summaries, train],
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
    model_sess.close()