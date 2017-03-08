
# MNIST labs

This directory contains a series of labs/examples using the ['MNIST'](http://yann.lecun.com/exdb/mnist/) database.
Each builds conceptually on the previous ones.  Start at the README numbered '01', and work upwards.

- [01_README_mnist_simple](./01_README_mnist_simple.md): A simple version of MNIST with no hidden layers.

- [02_README_mnist_tflearn](./02_README_mnist_tflearn.md): Use the high-level TensorFlow API in `contrib.tflearn` to easily build DNNs with hidden layers. Introducing TensorBoard.

- ["The hard way"](./the_hard_way/README.md): Next, we'll drop down into using the 'low-level' TensorFlow APIs, and see how we'd build the same model that way.

- [CNN Estimators for MNIST](./mnist_cnn/README.md): Here, we'll pop back up to `tflearn`.  We'll build a custom `Estimator` for a version of MNIST that uses CNNs.  We'll do this in two stages.  First, we'll create an `Estimator` that directly uses the graph described in [this tutorial](https://www.tensorflow.org/versions/r0.11/tutorials/mnist/pros/index.html#deep-mnist-for-experts).  Then, we'll convert that to a version which uses `tf.layers`.

- [Running distributed MNIST using Cloud ML](./cloudml/README.md).


