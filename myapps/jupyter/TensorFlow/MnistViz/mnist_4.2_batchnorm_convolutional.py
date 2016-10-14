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
tf.set_random_seed(0.0)

# Download images and labels into mnist.test (10K images+labels) and mnist.train (60K images+labels)
mnist = read_data_sets("data", one_hot=True, reshape=False, validation_size=0)

# neural network structure for this sample:
#
# · · · · · · · · · ·      (input data, 1-deep)                    X [batch, 28, 28, 1]
# @ @ @ @ @ @ @ @ @ @   -- conv. layer +BN 6x6x1=>6 stride 1       W1 [5, 5, 1, 6]        B1 [6]
# ∶∶∶∶∶∶∶∶∶∶∶∶∶∶∶∶∶∶∶                                              Y1 [batch, 28, 28, 6]
#   @ @ @ @ @ @ @ @     -- conv. layer +BN 5x5x6=>12 stride 2      W2 [5, 5, 6, 12]        B2 [12]
#   ∶∶∶∶∶∶∶∶∶∶∶∶∶∶∶                                                Y2 [batch, 14, 14, 12]
#     @ @ @ @ @ @       -- conv. layer +BN 4x4x12=>24 stride 2     W3 [4, 4, 12, 24]       B3 [24]
#     ∶∶∶∶∶∶∶∶∶∶∶                                                  Y3 [batch, 7, 7, 24] => reshaped to YY [batch, 7*7*24]
#      \x/x\x\x/ ✞      -- fully connected layer (relu+dropout+BN) W4 [7*7*24, 200]       B4 [200]
#       · · · ·                                                    Y4 [batch, 200]
#       \x/x\x/         -- fully connected layer (softmax)         W5 [200, 10]           B5 [10]
#        · · ·                                                     Y [batch, 20]

# input X: 28x28 grayscale images, the first dimension (None) will index the images in the mini-batch
X = tf.placeholder(tf.float32, [None, 28, 28, 1])
# correct answers will go here
Y_ = tf.placeholder(tf.float32, [None, 10])
# variable learning rate
lr = tf.placeholder(tf.float32)
# test flag for batch norm
tst = tf.placeholder(tf.bool)
iter = tf.placeholder(tf.int32)
# dropout probability
pkeep = tf.placeholder(tf.float32)

def batchnorm(Ylogits, is_test, iteration, convolutional=False):
    exp_moving_avg = tf.train.ExponentialMovingAverage(0.9999, iteration) # adding the iteration prevents from averaging across non-existing iterations
    bnepsilon = 1e-5
    if convolutional:
        mean, variance = tf.nn.moments(Ylogits, [0, 1, 2])
    else:
        mean, variance = tf.nn.moments(Ylogits, [0])
    update_moving_everages = exp_moving_avg.apply([mean, variance])
    m = tf.cond(is_test, lambda: exp_moving_avg.average(mean), lambda: mean)
    v = tf.cond(is_test, lambda: exp_moving_avg.average(variance)*100/101, lambda: variance)  # 100 = mini-batch size, to compute unbiased variance
    Ybn = tf.nn.batch_normalization(Ylogits, m, v, 0.0, 1.0, bnepsilon)
    return Ybn, update_moving_everages

def no_batchnorm(Ylogits, is_test, iteration, convolutional=False):
    return Ylogits, tf.no_op()

# three convolutional layers with their channel counts, and a
# fully connected layer (tha last layer has 10 softmax neurons)
K = 6  # first convolutional layer output depth
L = 12  # second convolutional layer output depth
M = 24  # third convolutional layer
N = 200  # fully connected layer

W1 = tf.Variable(tf.truncated_normal([6, 6, 1, K], stddev=0.1))  # 6x6 patch, 1 input channel, K output channels
B1 = tf.Variable(tf.constant(0.1, tf.float32, [K]))
#S1 = tf.Variable(tf.constant(1.0, tf.float32, [K]))
W2 = tf.Variable(tf.truncated_normal([5, 5, K, L], stddev=0.1))
B2 = tf.Variable(tf.constant(0.1, tf.float32, [L]))
#S2 = tf.Variable(tf.constant(1.0, tf.float32, [L]))
W3 = tf.Variable(tf.truncated_normal([4, 4, L, M], stddev=0.1))
B3 = tf.Variable(tf.constant(0.1, tf.float32, [M]))
#S3 = tf.Variable(tf.constant(1.0, tf.float32, [M]))

W4 = tf.Variable(tf.truncated_normal([7 * 7 * M, N], stddev=0.1))
B4 = tf.Variable(tf.constant(0.1, tf.float32, [N]))
#S4 = tf.Variable(tf.constant(1.0, tf.float32, [N]))
W5 = tf.Variable(tf.truncated_normal([N, 10], stddev=0.1))
B5 = tf.Variable(tf.constant(0.1, tf.float32, [10]))

# The model
stride = 1  # output is 28x28
Y1l = tf.nn.conv2d(X, W1, strides=[1, stride, stride, 1], padding='SAME') + B1
Y1bn, update_ema1 = batchnorm(Y1l, tst, iter, convolutional=True)
Y1 = tf.nn.relu(Y1bn)
stride = 2  # output is 14x14
Y2l = tf.nn.conv2d(Y1, W2, strides=[1, stride, stride, 1], padding='SAME') + B2
Y2bn, update_ema2 = batchnorm(Y2l, tst, iter, convolutional=True)
Y2 = tf.nn.relu(Y2bn)
stride = 2  # output is 7x7
Y3l = tf.nn.conv2d(Y2, W3, strides=[1, stride, stride, 1], padding='SAME') +B3
Y3bn, update_ema3 = batchnorm(Y3l, tst, iter, convolutional=True)
Y3 = tf.nn.relu(Y3bn)

# reshape the output from the third convolution for the fully connected layer
YY = tf.reshape(Y3, shape=[-1, 7 * 7 * M])

Y4l = tf.matmul(YY, W4) +B4
Y4bn, update_ema4 = batchnorm(Y4l, tst, iter)
Y4 = tf.nn.relu(Y4bn)
Y4d = tf.nn.dropout(Y4, pkeep)
Ylogits = tf.matmul(Y4d, W5) + B5
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
allweights = tf.concat(0, [tf.reshape(W1, [-1]), tf.reshape(W2, [-1]), tf.reshape(W3, [-1]), tf.reshape(W4, [-1]), tf.reshape(W5, [-1])])
allbiases  = tf.concat(0, [tf.reshape(B1, [-1]), tf.reshape(B2, [-1]), tf.reshape(B3, [-1]), tf.reshape(B4, [-1]), tf.reshape(B5, [-1])])
conv_activations = tf.concat(0, [tf.reshape(tf.reduce_max(Y1, [0]), [-1]), tf.reshape(tf.reduce_max(Y2, [0]), [-1]), tf.reshape(tf.reduce_max(Y3, [0]), [-1])])
dense_activations = tf.reduce_max(Y4, [0])
I = tensorflowvisu.tf_format_mnist_images(X, Y, Y_)
It = tensorflowvisu.tf_format_mnist_images(X, Y, Y_, 1000, lines=25)
datavis = tensorflowvisu.MnistDataVis(title4="batch-max conv activations", title5="batch-max dense activations", histogram4colornum=2, histogram5colornum=2)

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

    # learning rate decay
    #max_learning_rate = 0.003
    #min_learning_rate = 0.0001
    #decay_speed = 2000
    max_learning_rate = 0.02
    min_learning_rate = 0.00015
    decay_speed = 1000.0
    learning_rate = min_learning_rate + (max_learning_rate - min_learning_rate) * math.exp(-i/decay_speed)

    # compute training values for visualisation
    if update_train_data:
        a, c, im, ca, da = sess.run([accuracy, cross_entropy, I, conv_activations, dense_activations], {X: batch_X, Y_: batch_Y, tst: False, pkeep: 1.0})
        print(str(i) + ": accuracy:" + str(a) + " loss: " + str(c) + " (lr:" + str(learning_rate) + ")")
        datavis.append_training_curves_data(i, a, c)
        datavis.update_image1(im)
        datavis.append_data_histograms(i, ca, da)

    # compute test values for visualisation
    if update_test_data:
        a, c, im = sess.run([accuracy, cross_entropy, It], {X: mnist.test.images, Y_: mnist.test.labels, tst: True, pkeep: 1.0})
        print(str(i) + ": ********* epoch " + str(i*100//mnist.train.images.shape[0]+1) + " ********* test accuracy:" + str(a) + " test loss: " + str(c))
        datavis.append_test_curves_data(i, a, c)
        datavis.update_image2(im)

    # the backpropagation training step
    sess.run(train_step, {X: batch_X, Y_: batch_Y, lr: learning_rate, tst: False, pkeep: 0.75})
    sess.run(update_ema, {X: batch_X, Y_: batch_Y, tst: False, iter: i})

datavis.animate(training_step, 20001, train_data_update_freq=20, test_data_update_freq=100)

# to save the animation as a movie, add save_movie=True as an argument to datavis.animate
# to disable the visualisation use the following line instead of the datavis.animate line
# for i in range(10000+1): training_step(i, i % 100 == 0, i % 20 == 0)

print("max test accuracy: " + str(datavis.get_max_test_accuracy()))

## All runs 10K iterations:
# batch norm 0.998 lr 0.03-0.0001-1000 no BN offset or scale: best 0.9933 but most of the way under 0.993 and lots of variation. test loss under 2.2 though
# batch norm 0.998 lr 0.03-0.0001-500 no BN offset or scale: best 0.9933 but really clean curves
# batch norm 0.998 lr 0.03-0.0001-500 no BN offset or scale, dropout 0.8 on fully connected layer: max 0.9926
# same as above but batch norm on fully connected layer only: max 0.9904
# batch norm 0.998 lr 0.03-0.0001-500, withouts biases or BN offsets or scales at all: above 0.99 at 1200 iterations (!) but then max 0.9928 (record test loss though: 2.08193)
# batch norm 0.998 lr 0.03-0.0001-500 dropout à.75, with biases replaced with BN offsets as per the book: above 0.99 at 900 iterations (!), max 0.9931 at 10K iterations, maybe could have gone higher still (record test loss though: below 2.05)
# batch norm 0.998 lr 0.03-0.0001-500 no dropout, with biases replaced with BN offsets as per the book: above 0.99 at 900 iterations (!), max 0.993 (best loss at 2.0879 at 2100 it and went up after that)
# batch norm 0.998 lr 0.03-0.0001-500 no dropout, offets and scales for BN, no biases: max 0.9935 at 2400 it but going down from there... also dense activations not so regular...
# batch norm 0.999 + same as above: 0.9935 at 2400 iterations but downhill from there...
# batch norm 0.999 lr 0.02-0.0002-2000 dropout 0.75, normal biases, no BN scales or offsets: max 0.9949 at 17K it (min test loss 1.64665 but cruising around 1.8) 0.994 at 3100 it, 0.9942 at 20K it, 0.99427 average on last 10K it
# batch norm 0.999 lr 0.02-0.0001-1000 dropout 0.75, normal biases, no BN scales or offsets: max 0.9944 but oscillating in 0.9935-0.9940 region (test loss stable betwen 1.7 and 1.8 though)
# batch norm 0.999 lr 0.02-0.0002-1000 dropout 0.75, normal biases, no BN scales or offsets: max 0.995, min test loss 1.49787 cruising below 1.6, then at 8K it something happens and cruise just above 1.6, 0.99436 average on last 10K it
# => see which setting removes the weird event at 8K ?:
# => in everything below batch norm 0.999 lr 0.02-0.0002-1000 dropout 0.75, normal biases, no MB scales or offsets, unless stated otherwise
# remove n/n+1 in variation calculation: no good, m ax 0.994 buit cruising around 0.993
# bn 0.9955 for cutoff at 2K it: still something happens at 8K. Max 0.995 but cruising at 0.9942-0.9943 only and downward trend above 15K. Test loss: nice cruise below 1.6
# bn epsilon e-10 => max 0.9947 cruise around 0.9939, test loss never went below 1.6, barely below 1.7,
# bn epsilon e-10 run 2=> max 0.9945 cruise around 0.9937, test loss never went below 1.6, barely below 1.7,
# baseline run 2: max 0.995 cruising around 0.9946 0.9947, test loss cruising between 1.6 and 1.7 (baseline confirmed)
# bn 0.998 for cutoff at 5K it: max 0.9948, test loss cruising btw 1.6 and 1.8, last 10K avg 0.99421
# lr 0.015-0.0001-1500: max 0.9938, cruise between 0.993 and 0.994, test loss above 2.0 most of the time (not good)
# bn 0.9999: max 0.9952, cruise between 0.994 and 0.995 with upward trend, fall in last 2K it. test loss cruise just above 1.6. Avg on last 10K it 0.99441. Could be stopped at 7000 it. Quite noisy overall.
# bn 0.99955 for cutoff at 20K it: max 0.9948, cruise around 0.9942, test loss cruise around 1.7. Avg on last 10K it 0.99415
# batch norm 0.999 lr 0.015-0.00015-1500 dropout 0.75, normal biases, no MB scales or offsets: cruise around 0.9937-00994, test loss cruise around 1.95-2.0 (not good)
# batch norm 0.999 lr 0.03-0.0001-2000 dropout 0.75, normal biases, no MB scales or offsets: stable cruise around 0.9940, test loss cruise around 2.2, good stability in last 10K, bumpy slow start
# batch norm 0.9999 lr 0.02-0.0001-1500 dropout 0.75, normal biases, no MB scales or offsets: max 0.995, stable btw 0.0040-0.9945, test loss stable around 1.7, good stability in last 4K, avg on last 10K: 0.99414, avg on last 4K
# *batch norm 0.9999 lr 0.02-0.00015-1000 dropout 0.75, normal biases, no MB scales or offsets: max 0.9956 stable above 0.995!!! test loss stable around 1.6. Avg last 10K 0.99502. Avg 10K-13K 0.99526. Avg 8K-10K: 0.99514. Best example to run in 10K
# same as above with different rnd seed: max 0.9938 only in 10K it, test loss in 1.9 region (very bad)
# same as above with dropout 0.8: max 0.9937 only (bad)
# same as above with dropout 0.66: max 0.9942 only, test loss between 1.7-1.8 (not good)
# same as above with lr 0.015-0.0001-1200: max 0.9946 at 6500 it but something happens after that it it goes down (not good)
# best * run 2 (lbl 5.1): max 0.9953, cruising around 0.995 until 12K it, went down a bit after that (still ok) avg 8-10K 0.99484
# best * run 3 (lbl 5.2 video): max 0.9951, cruising just below 0.995, test loss cruising around 1.6
# best * run 3-8: not good, usually in the 0.994 range
# best * run 9: (lbl 5.3 video): max 0.9956, cruising above 0.995, test loss cruising around 1.6, avg 7K-10K it: 0.99518
