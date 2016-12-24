# encoding: UTF-8
# Copyright 2016 Google.com
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import tensorflow as tf
import tensorflowvisu
import math
from tensorflow.contrib.learn.python.learn.datasets.mnist import read_data_sets
tf.set_random_seed(0)

# neural network with 5 layers
#
# · · · · · · · · · ·       (input data, flattened pixels)       X [batch, 784]   # 784 = 28*28
# \x/x\x/x\x/x\x/x\x/    -- fully connected layer (relu+BN)      W1 [784, 200]      B1[200]
#  · · · · · · · · ·                                             Y1 [batch, 200]
#   \x/x\x/x\x/x\x/      -- fully connected layer (relu+BN)      W2 [200, 100]      B2[100]
#    · · · · · · ·                                               Y2 [batch, 100]
#    \x/x\x/x\x/         -- fully connected layer (relu+BN)      W3 [100, 60]       B3[60]
#     · · · · ·                                                  Y3 [batch, 60]
#     \x/x\x/            -- fully connected layer (relu+BN)      W4 [60, 30]        B4[30]
#      · · ·                                                     Y4 [batch, 30]
#      \x/               -- fully connected layer (softmax)      W5 [30, 10]        B5[10]
#       ·                                                        Y5 [batch, 10]

# Download images and labels into mnist.test (10K images+labels) and mnist.train (60K images+labels)
mnist = read_data_sets("data", one_hot=True, reshape=False, validation_size=0)

# input X: 28x28 grayscale images, the first dimension (None) will index the images in the mini-batch
X = tf.placeholder(tf.float32, [None, 28, 28, 1])
# correct answers will go here
Y_ = tf.placeholder(tf.float32, [None, 10])
# variable learning rate
lr = tf.placeholder(tf.float32)
# train/test selector for batch normalisation
tst = tf.placeholder(tf.bool)
# training iteration
iter = tf.placeholder(tf.int32)

# five layers and their number of neurons (tha last layer has 10 softmax neurons)
L = 200
M = 100
N = 60
P = 30
Q = 10

# Weights initialised with small random values between -0.2 and +0.2
# When using RELUs, make sure biases are initialised with small *positive* values for example 0.1 = tf.ones([K])/10
W1 = tf.Variable(tf.truncated_normal([784, L], stddev=0.1))  # 784 = 28 * 28
B1 = tf.Variable(tf.ones([L])/10)
W2 = tf.Variable(tf.truncated_normal([L, M], stddev=0.1))
B2 = tf.Variable(tf.ones([M])/10)
W3 = tf.Variable(tf.truncated_normal([M, N], stddev=0.1))
B3 = tf.Variable(tf.ones([N])/10)
W4 = tf.Variable(tf.truncated_normal([N, P], stddev=0.1))
B4 = tf.Variable(tf.ones([P])/10)
W5 = tf.Variable(tf.truncated_normal([P, Q], stddev=0.1))
B5 = tf.Variable(tf.ones([Q])/10)

## Batch normalisation conclusions:
# On RELUs, you have to display batch-max(activation) to see the nice effect on distribution but
# it is very visible.
# With RELUs, the scale and offset variables can be omitted. They do not seem to do anything.

# Steady 98.5% accuracy using these parameters:
# moving average decay: 0.998 (equivalent to averaging over two epochs)
# learning rate decay from 0.03 to 0.0001 speed 1000 => max 98.59 at 6500 iterations, 98.54 at 10K it,  98% at 1300it, 98.5% at 3200it

# relu, no batch-norm, lr(0.003, 0.0001, 2000) => 98.2%
# relu, batch-norm lr(0.03, 0.0001, 1000) => 98.5% - 98.55%
# relu, batch-norm, no offsets => 98.5% - 98.55% (no change)
# relu, batch-norm, no scales => 98.5% - 98.55% (no change)
# relu, batch-norm, no scales, no offsets => 98.5% - 98.55% (no change) - even peak at 98.59% :-)

# Correct usage of batch norm scale and offset parameters:
# According to BN paper, offsets should be kept and biases removed.
# In practice, it seems to work well with BN without offsets and traditional biases.
# "When the next layer is linear (also e.g. `nn.relu`), scaling can be
# disabled since the scaling can be done by the next layer."
# So apparently no need of scaling before a RELU.
# => Using neither scales not offsets with RELUs.

def batchnorm(Ylogits, is_test, iteration):
    exp_moving_avg = tf.train.ExponentialMovingAverage(0.998, iteration) # adding the iteration prevents from averaging across non-existing iterations
    bnepsilon = 1e-5
    mean, variance = tf.nn.moments(Ylogits, [0])
    update_moving_everages = exp_moving_avg.apply([mean, variance])
    m = tf.cond(is_test, lambda: exp_moving_avg.average(mean), lambda: mean)
    v = tf.cond(is_test, lambda: exp_moving_avg.average(variance)*100/101, lambda: variance)  # 100 = mini-batch size, to compute unbiased variance
    Ybn = tf.nn.batch_normalization(Ylogits, m, v, 0.0, 1.0, bnepsilon)
    return Ybn, update_moving_everages

def no_batchnorm(Ylogits, Offset, Scale, is_test, iteration):
    return Ylogits, tf.no_op()

# The model
XX = tf.reshape(X, [-1, 784])

Y1l = tf.matmul(XX, W1) + B1
Y1bn, update_ema1 = batchnorm(Y1l, tst, iter)
Y1 = tf.nn.relu(Y1bn)

Y2l = tf.matmul(Y1, W2) + B2
Y2bn, update_ema2 = batchnorm(Y2l, tst, iter)
Y2 = tf.nn.relu(Y2bn)

Y3l = tf.matmul(Y2, W3) + B3
Y3bn, update_ema3 = batchnorm(Y3l, tst, iter)
Y3 = tf.nn.relu(Y3bn)

Y4l = tf.matmul(Y3, W4) + B4
Y4bn, update_ema4 = batchnorm(Y4l, tst, iter)
Y4 = tf.nn.relu(Y4bn)

Ylogits = tf.matmul(Y4, W5) + B5
Y = tf.nn.softmax(Ylogits)

update_ema = tf.group(update_ema1, update_ema2, update_ema3, update_ema4)

# cross-entropy loss function (= -sum(Y_i * log(Yi)) ), normalised for batches of 100  images
# TensorFlow provides the softmax_cross_entropy_with_logits function to avoid numerical stability
# problems with log(0) which is NaN
cross_entropy = tf.nn.softmax_cross_entropy_with_logits(Ylogits, Y_)
cross_entropy = tf.reduce_mean(cross_entropy)*100

# accuracy of the trained model, between 0 (worst) and 1 (best)
correct_prediction = tf.equal(tf.argmax(Y, 1), tf.argmax(Y_, 1))
accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))

# matplotlib visualisation
allweights = tf.concat(0, [tf.reshape(W1, [-1]), tf.reshape(W2, [-1]), tf.reshape(W3, [-1])])
allbiases  = tf.concat(0, [tf.reshape(B1, [-1]), tf.reshape(B2, [-1]), tf.reshape(B3, [-1])])
# to use for sigmoid
#allactivations = tf.concat(0, [tf.reshape(Y1, [-1]), tf.reshape(Y2, [-1]), tf.reshape(Y3, [-1]), tf.reshape(Y4, [-1])])
# to use for RELU
allactivations = tf.concat(0, [tf.reduce_max(Y1, [0]), tf.reduce_max(Y2, [0]), tf.reduce_max(Y3, [0]), tf.reduce_max(Y4, [0])])
alllogits = tf.concat(0, [tf.reshape(Y1l, [-1]), tf.reshape(Y2l, [-1]), tf.reshape(Y3l, [-1]), tf.reshape(Y4l, [-1])])
I = tensorflowvisu.tf_format_mnist_images(X, Y, Y_)
It = tensorflowvisu.tf_format_mnist_images(X, Y, Y_, 1000, lines=25)
datavis = tensorflowvisu.MnistDataVis(title4="Logits", title5="Max activations across batch", histogram4colornum=2, histogram5colornum=2)


# training step, the learning rate is a placeholder
train_step = tf.train.AdamOptimizer(lr).minimize(cross_entropy)

# init
init = tf.initialize_all_variables()
sess = tf.Session()
sess.run(init)


# You can call this function in a loop to train the model, 100 images at a time
def training_step(i, update_test_data, update_train_data):

    # training on batches of 100 images with 100 labels
    batch_X, batch_Y = mnist.train.next_batch(100)

    # learning rate decay (without batch norm)
    #max_learning_rate = 0.003
    #min_learning_rate = 0.0001
    #decay_speed = 2000
    # learning rate decay (with batch norm)
    max_learning_rate = 0.03
    min_learning_rate = 0.0001
    decay_speed = 1000.0
    learning_rate = min_learning_rate + (max_learning_rate - min_learning_rate) * math.exp(-i/decay_speed)

    # compute training values for visualisation
    if update_train_data:
        a, c, im, al, ac = sess.run([accuracy, cross_entropy, I, alllogits, allactivations], {X: batch_X, Y_: batch_Y, tst: False})
        print(str(i) + ": accuracy:" + str(a) + " loss: " + str(c) + " (lr:" + str(learning_rate) + ")")
        datavis.append_training_curves_data(i, a, c)
        datavis.update_image1(im)
        datavis.append_data_histograms(i, al, ac)

    # compute test values for visualisation
    if update_test_data:
        a, c, im = sess.run([accuracy, cross_entropy, It], {X: mnist.test.images, Y_: mnist.test.labels, tst: True})
        print(str(i) + ": ********* epoch " + str(i*100//mnist.train.images.shape[0]+1) + " ********* test accuracy:" + str(a) + " test loss: " + str(c))
        datavis.append_test_curves_data(i, a, c)
        datavis.update_image2(im)

    # the backpropagation training step
    sess.run(train_step, {X: batch_X, Y_: batch_Y, lr: learning_rate, tst: False})
    sess.run(update_ema, {X: batch_X, Y_: batch_Y, tst: False, iter: i})

datavis.animate(training_step, iterations=10000+1, train_data_update_freq=20, test_data_update_freq=100, more_tests_at_start=True)

# to save the animation as a movie, add save_movie=True as an argument to datavis.animate
# to disable the visualisation use the following line instead of the datavis.animate line
# for i in range(10000+1): training_step(i, i % 100 == 0, i % 20 == 0)

print("max test accuracy: " + str(datavis.get_max_test_accuracy()))

# Some results to expect:
# (In all runs, if sigmoids are used, all biases are initialised at 0, if RELUs are used,
# all biases are initialised at 0.1 apart from the last one which is initialised at 0.)

## learning rate = 0.003, 10K iterations
# final test accuracy = 0.9788 (sigmoid - slow start, training cross-entropy not stabilised in the end)
# final test accuracy = 0.9825 (relu - above 0.97 in the first 1500 iterations but noisy curves)

## now with learning rate = 0.0001, 10K iterations
# final test accuracy = 0.9722 (relu - slow but smooth curve, would have gone higher in 20K iterations)

## decaying learning rate from 0.003 to 0.0001 decay_speed 2000, 10K iterations
# final test accuracy = 0.9746 (sigmoid - training cross-entropy not stabilised)
# final test accuracy = 0.9824 (relu - training set fully learned, test accuracy stable)
