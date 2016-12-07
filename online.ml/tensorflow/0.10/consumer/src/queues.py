import tensorflow as tf
import numpy as np
import imghdr


def create_queue_reader(files):
    """
    Given a string Tensor of filenames, creates a filename queue and
    `WholeFileReader`. Returns the key, value tensors returned from the
    `Reader.read()` method

    Args:
        files: String Tensor of file paths

    Returns:
        key, value tensors from the `tf.Reader.read()` method
    """
    filename_queue = tf.train.string_input_producer(files)
    reader = tf.WholeFileReader()
    return reader.read(filename_queue)


def clean_image_data(image, pixel_depth, height, width):
    """
    Cleans data from an image `Tensor`, and returns a new `Tensor`

    Args
    """
    norm = (tf.cast(image, tf.float32) - (pixel_depth / 2)) / pixel_depth
    resized = tf.image.resize_images([norm], height, width)
    return resized


def is_jpeg(filename, data):
    return imghdr.what(filename, data) == 'jpeg'


def is_png(filename, data):
    return imghdr.what(filename, data) == 'png'


def has_jpeg_ext(filename):
    return np.array(filename.endswith(b'.jpeg') or filename.endswith(b'.jpg'))


def has_png_ext(filename):
    return np.array(filename.endswith(b'.png'))


def create_image_queue_graph(files, pixel_depth, height, width, channels,
                             batch_size, capacity):
    """
    Creates and returns a TensorFlow graph containing a configured queue to
    pull in data
    """
    graph = tf.Graph()
    with graph.as_default():
        with graph.device('/cpu:0'):
            key, value = create_queue_reader(files)
            # image = tf.image.decode_jpeg(value, channels=channels)
            is_jpeg = tf.py_func(has_jpeg_ext, [key], [tf.bool])
            image = tf.cond(is_jpeg[0],
                            lambda: tf.image.decode_jpeg(value,
                                                         channels=channels),
                            lambda: tf.image.decode_png(value,
                                                        channels=channels)
                            )
            cleaned = clean_image_data(image, pixel_depth, height, width)
            image_batch, label_batch = tf.train.batch(
                [cleaned, key],
                batch_size=batch_size,
                capacity=capacity)
            image_squeezed = tf.squeeze(image_batch)
            batch_data = tf.identity(image_squeezed, name='batch_data')
            batch_labels = tf.identity(label_batch, name='batch_labels')
    return graph
