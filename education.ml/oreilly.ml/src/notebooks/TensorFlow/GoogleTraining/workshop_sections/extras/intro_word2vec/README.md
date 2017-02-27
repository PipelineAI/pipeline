
# Introducing word2vec

In this section, we'll take a look at the ['basic' variant of word2vec](https://github.com/tensorflow/tensorflow/blob/master/tensorflow/examples/tutorials/word2vec/word2vec_basic.py) used for [this TensorFlow tutorial](https://www.tensorflow.org/versions/r0.8/tutorials/word2vec/index.html#vector-representations-of-words).

We'll launch the training process, then while that is running, we'll look at how this graph is constructed, and what it does. In the process we'll introduce [Tensorboard](https://www.tensorflow.org/versions/r0.8/how_tos/summaries_and_tensorboard/index.html).

## Start the process of training the model

First, confirm that you can import `matplotlib` and `sklearn`, as in the installation instructions.
Then, start training the model. You can do this in two different ways: from the command line, and from a [Jupyter](http://jupyter.org/) notebook.

### Training from the command line

Run the [`word2vec_basic_summaries.py`](word2vec_basic_summaries.py) script from this directory. (This file is the same as [`word2vec_basic`](https://github.com/tensorflow/tensorflow/blob/master/tensorflow/examples/tutorials/word2vec/word2vec_basic.py), but has a few additional lines of code that write some summary information to [TensorBoard](https://www.tensorflow.org/versions/r0.8/how_tos/summaries_and_tensorboard/index.html)).

```sh
$ python word2vec_basic_summaries.py
```


## (Alternately) train from a Jupyter notebook.

Instead of running the command-line script, you can use a Jupyter (iPython) notebook in the same directory.


```sh
$ jupyter notebook
```

Note: if you're starting Jupyter within a docker container, and you're using `docker-machine`, run

```sh
$ docker-machine ip
```
(outside the container) to find the IP address to use in your browser.  Otherwise, you should be able to bring up Jupyter on `http://localhost:8888/`.

Then select the `word2vec_basic_summaries.ipynb` notebook from the displayed list in the browser.
Step through its setup and graph definition steps until you reach the training step, and start it running.

## The word2vec_basic model graph

While the model is training, we'll walk through the code, and look at how its graph is constructed.

This example picks a random set of words from the top 100 most frequent, and periodically outputs the 'nearby' results for those words.  You can watch the set for each word becoming more accurate (mostly :) as the training continues. To get really impressive results, you'll need to run for more than the default number of steps.

### Take a look at TensorBoard

[Tensorboard](https://www.tensorflow.org/versions/r0.8/how_tos/summaries_and_tensorboard/index.html) is a suite of visualization tools that help make it easier to understand, debug, and optimize TensorFlow programs.

Start it up as follows while the training is running (or after it's done), pointing it to the directory in which the summaries were written.  If you're using the virtual environment, make sure you activate your conda environment in this shell window as well.

```sh
$ tensorboard --logdir=/tmp/word2vec_basic/summaries
```

Note: similarly to running Jupyter, if you're starting TensorBoard within a docker container, and you're using `docker-machine`, run

```sh
$ docker-machine ip
```
(outside the container) to find the IP address to use in your browser.  Otherwise, you should be able to bring up TensorBoard on `http://localhost:6006/`.

<a href="https://storage.googleapis.com/oscon-tf-workshop-materials/images/tensorboard_word2vec_basic.png" target="_blank"><img src="https://storage.googleapis.com/oscon-tf-workshop-materials/images/tensorboard_word2vec_basic.png" width="500"/></a>

We will look at TensorBoard again in a later section of the workshop.

## Look at the results

After the training is finished, the script will map the model's learned word vectors into a 2D space, and plot the results using `matplotlib` in conjunction with an `sklearn` library called
[TSNE](https://lvdmaaten.github.io/tsne/).
It will write the plot to an image file named `tsne.png` in the same directory.

<a href="https://amy-jo.storage.googleapis.com/images/tf-workshop/tsne.png" target="_blank"><img src="https://amy-jo.storage.googleapis.com/images/tf-workshop/tsne.png" width="500"/></a>

In your projection plot, you should see similar words clustered close to each other.

## Exercise: find the 'nearby' words for a specific given word

See if you can figure out how to modify the [`word2vec_basic_summaries.py`](word2vec_basic_summaries.py) code to evaluate and output the 'nearby' set for a specific word too.

E.g., picking "government" as the word may give a result like this (after about 500K training steps):

```
Nearest to b'government': b'governments', b'leadership', b'regime', b'crown', b'rule', b'leaders', b'parliament', b'elections',
```

After you've given it a try yourself, [`word2vec_basic_nearby.py`](word2vec_basic_nearby.py) shows one simple way to do this.


