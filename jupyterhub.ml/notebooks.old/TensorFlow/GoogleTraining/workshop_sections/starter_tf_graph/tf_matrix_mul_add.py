#!/usr/bin/env python
# Copyright 2016 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ==============================================================================

import numpy as np

import tensorflow as tf


m1 = np.array([[1., 2.], [3., 4.], [5., 6.], [7., 8.]], dtype=np.float32)

# Input data.
m1_input = tf.placeholder(tf.float32, shape=[4, 2])

m2 = tf.Variable(tf.random_uniform([2, 3], -1.0, 1.0))

m3 = tf.matmul(m1_input, m2)

# This is an identity op with the side effect of printing data when
# evaluating.
m3 = tf.Print(m3, [m3], message="m3 is: ")

m4 = tf.add(m3, m3)
# m4 = m3 + m3

# Add variable initializer.
init = tf.initialize_all_variables()

with tf.Session() as session:
    # We must initialize all variables before we use them.
    init.run()
    print("Initialized")

    print("m2: {}".format(m2))
    print("eval m2: {}".format(m2.eval()))
    # The following will error, because it depends on m1_input, which won't
    # be initialized.
    # print("eval m3: {}".format(m3.eval()))

    feed_dict = {m1_input: m1}

    result1, result2 = session.run([m4, m3], feed_dict=feed_dict)

    print("\nresult1: {}, result2 {}".format(result1, result2))
