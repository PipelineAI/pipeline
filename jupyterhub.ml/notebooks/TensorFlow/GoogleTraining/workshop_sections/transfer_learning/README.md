
# Transfer learning

  - [Introduction](#introduction)
  - [1. Take a look at the the Inception v3 model](#1-take-a-look-at-the-the-inception-v3-model)
  - [Data sets](#data-sets)
    - [The "hugs/no-hugs" data set](#the-hugsno-hugs-data-set)
    - [(Or, you can use the Flowers data set if you want)](#or-you-can-use-the-flowers-data-set-if-you-want)
    - [Pre-generated 'bottleneck' values for both example datasets](#pre-generated-bottleneck-values-for-both-example-datasets)
  - [2. Run a training session and use the model for prediction](#2-run-a-training-session-and-use-the-model-for-prediction)
    - [Train the model](#train-the-model)
    - [Do prediction using your learned model in an ipython notebook](#do-prediction-using-your-learned-model-in-an-ipython-notebook)
  - [3. A Custom Esimator for the transfer learning model](#3-a-custom-esimator-for-the-transfer-learning-model)
  - [Named Scopes and TensorBoard Summary information](#named-scopes-and-tensorboard-summary-information)
  - [4. Exercise: Building the Custom Estimator's model graph](#4-exercise-building-the-custom-estimators-model-graph)


## Introduction

This lab shows how we can use an existing model to do *transfer learning* -- effectively bootstrapping an existing model to reduce the effort needed to learn something new.

Specifically, we will take an 'Inception' v3 architecture model trained on ImageNet images, and using its penultimate "bottleneck" layer, train a new top layer that can recognize other classes of images.
We'll see that our new top layer does not need to be very complex, and that we don't need to do much training of this new model, to get good results for our new image classifications.

The core of the approach is the same as that used in [this TensorFlow example](https://github.com/tensorflow/tensorflow/blob/master/tensorflow/examples/image_retraining), but here we will use a custom [Estimator](https://www.tensorflow.org/versions/r0.11/api_docs/python/contrib.learn.html#estimators) (and train on a different set of photos).

## 1. Take a look at the the Inception v3 model

We can use the `view_inception_model.ipynb` Jupyter notebook to take a look at the structure of the Inception model before we start working with it.

First, download the inception model from:
http://download.tensorflow.org/models/image/imagenet/inception-2015-12-05.tgz , extract it,
and copy the model file `classify_image_graph_def.pb` into `/tmp/imagenet` (you may need to first create the directory).  This is where our python scripts will look for it, so we're saving a later download by putting it in the same place.

Then, start a jupyter server in this directory.  For convenience, run it in a new terminal window. (Don't forget to activate your virtual environment first as necessary).

```sh
$ jupyter notebook
```

Load and run the `view_inception_model.ipynb` notebook.  Poke around the model graph a bit.

<a href="https://storage.googleapis.com/oscon-tf-workshop-materials/images/incpv3.png" target="_blank"><img src="https://storage.googleapis.com/oscon-tf-workshop-materials/images/incpv3.png" width="500"/></a>

See if you can find the 'DecodeJpeg/contents:0' and 'pool_3/_reshape:0' nodes-- these will be our input and 'bottleneck' nodes, respectively, for the transfer learning.

<a href="https://storage.googleapis.com/oscon-tf-workshop-materials/images/incpv3_pool_3_reshape.png" target="_blank"><img src="https://storage.googleapis.com/oscon-tf-workshop-materials/images/incpv3_pool_3_reshape.png" width="500"/></a>

Note: If you should want to write the model graph to a text file to browse it that way, you can use
the `tf.train.write_graph()` method. See [`mnist_hidden.py`](../mnist_series/the_hard_way/mnist_hidden.py) for
a (commented-out) example of how to call it.

## Data sets

We've provided training images for you, but if you want to play around further, you can use any image datasets you like.  The training script simply assumes you have a top-level directory containing class-named subdirectories, each containing images for that class.  It then infers the classes to be learned from the directory structure.

### The "hugs/no-hugs" data set

For this exercise, we'll use a training set of images that have been sorted into two categories -- whether or not one would want to hug the object in the photo.
(Thanks to Julia Ferraioli for this dataset).

This dataset does not have a large number of images, but as we will see, prediction on new images still works surprisingly well.  This shows the power of 'bootstrapping' the pre-trained Inception model.


```sh
$ curl -O https://storage.googleapis.com/oscon-tf-workshop-materials/transfer_learning/hugs_photos.zip
$ unzip hugs_photos.zip
```

### (Or, you can use the Flowers data set if you want)

If you want to do flower classification instead, as with the original tutorial, you can find the data here:

```sh
$ curl -O http://download.tensorflow.org/example_images/flower_photos.tgz
$ tar xzf flower_photos.tgz
```


### Pre-generated 'bottleneck' values for both example datasets

When you run the transfer learning training, you'll first need to generate "bottleneck values" for the images, using the Inception v3 model. (We'll take a look at how that works).
If this process is too time-consuming for the workshop context, you can download the pre-calculated bottleneck files for both the data sets above:

- https://storage.googleapis.com/oscon-tf-workshop-materials/transfer_learning/bottlenecks_hugs.zip
- https://storage.googleapis.com/oscon-tf-workshop-materials/transfer_learning/bottlenecks_flowers.zip

## 2. Run a training session and use the model for prediction

Let's start by training our new model and using the results to make predictions.

### Train the model

```sh
$ python transfer_learning.py --image_dir=hugs_photos --bottleneck_dir=bottlenecks_hugs
```

**Note the name of the model directory** that is created by the script.

### Do prediction using your learned model in an ipython notebook

Start up a jupyter server in this directory as necessary.  Select the `transfer_learning_prediction.ipynb` notebook in the listing that comes up.

Find this line:
```
MODEL_DIR = '/tmp/tfmodels/img_classify/your-model-dir'
```

and edit it to point to the model directory used for your training run.

Then, run the notebook.  
You should see some predictions made for the images in the `prediction_images` directory!

If you like, you can try adding additional images to that directory, and rerunning the last part of the notebook to find out whether they're more huggable than not.

## 3. A Custom Esimator for the transfer learning model

Before we jump into the coding part of the lab, we'll take a look at `transfer_learning_skeleton.py`.
It has the scaffolding in place for building a custom Estimator to do the transfer learning.  
We'll look at how the `fit()`, `evaluate()`, and `predict()` methods are being used.

We'll also take a look at how the Inception model is being loaded and accessed.

## Named Scopes and TensorBoard Summary information

Note that this code includes some examples of use of `tf.name_scope()` when defining nodes, particularly
in the `add_final_training_ops()` function. You'll be able to spot these scope names when you look at the model graph in TensorBoard.
We saw use of `tf.name_scope` earlier in ['mnist_hidden.py'](../mnist_series/the_hard_way/mnist_hidden.py) as well.

The code in `add_final_training_ops()` also includes some examples of defining summary information for TensorBoard (we saw a simple example of doing this in ['mnist_hidden.py'](../mnist_series/the_hard_way/mnist_hidden.py) also).

However, here, as we're wrapping things in an Estimator, we don't need to an an explicit `tf.merge_summary` op-- it will do that for us.


## 4. Exercise: Building the Custom Estimator's model graph

Start with [`transfer_learning_skeleton.py`](transfer_learning.py), and complete the `_make_model`
function definition. This function builds the model graph for the custom estimator.

As noted above, the Inception model graph is doing the heavy lifting here. We will just train a new
top layer to identify our new classes: that is, we will just add a new softmax and fully-connected
layer.  The input to this layer is the generated "bottleneck" values. The `add_final_training_ops`
function defines this layer, then defines the loss function and the training op.

Then, the `add_evaluation_step` function adds an op to evaluate the accuracy of the results. Add
'loss' and 'accuracy' metrics to the prediction_dict, as per the `METRICS` dict below
`make_model_fn` in the code, which we will then pass to the Estimator's `evaluate()` method.

Then, add support for generating prediction value(s).
See if you can figure out how to derive the index of the highest-value the entry in the result
vector, and store that value at the `"index"` key in the `prediction_dict`. As a hint, take a look
at the ops used in `add_evaluation_step()`.

As shown in the skeleton of `_make_model`, be sure to return the prediction dict, the loss, and the
training op.  This info sets up the Estimator to handle calls to its `fit()`, `evaluate()`, and
`predict()` methods.


If you get stuck, you can take a peek at `transfer_learning.py`, but try not to do that too soon.
