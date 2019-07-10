# Copyright 2016 The TensorFlow Authors. All Rights Reserved.
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


# Be sure to clone https://github.com/openai/improved-gan
# and add improved-gan/mnist_svhn_cifar10 to your PATH variable

# Download labels used to train the student
wget https://github.com/npapernot/multiple-teachers-for-privacy/blob/master/mnist_250_student_labels_lap_20.npy

# Train the student using improved-gan 
THEANO_FLAGS='floatX=float32,device=gpu,lib.cnmem=1' train_mnist_fm_custom_labels.py --labels mnist_250_student_labels_lap_20.npy --count 50 --epochs 600

