
# Run and modify a version of MNIST built using TensorFlow's 'low-level' APIs

  - [The "one-hidden-layer" version of the MNIST model](#the-one-hidden-layer-version-of-the-mnist-model)
  - [Running the example as a script](#running-the-example-as-a-script)
  - [Running the example in a Jupyter notebook](#running-the-example-in-a-jupyter-notebook)
  - [TensorBoard](#tensorboard)
  - [Exercise: The "two-hidden-layer" version](#exercise-the-two-hidden-layer-version)

In this lab, now that you've seen how easy it is to use the `DNNClassifier`, we'll look at something a bit more complex: how to build a similar NN using TensorFlow's "low-level" APIs.

This requires you to pay attention to a lot more things than you had to do in the previous lab, including: define a model graph and training op; manage the batched training loop; checkpointing the model and generating TensorBoard summaries; and supporting making predictions. 

It is useful to work through and understand this code, as there won't be a pre-baked Estimator (like `DNNClassifier`) available for every kind of model you want. 

(However, as a middle ground, you can also consider a custom [Estimator](https://www.tensorflow.org/versions/r0.11/api_docs/python/contrib.learn.html#Estimator). This can simplify many aspects of training, eval, and prediction, but lets you define your own model.  See [04_README_mnist_estimator](./04_README_mnist_estimator) and the [word2vec](../word2vec/README.md) lab for more on building custom Estimators.)

## The "one-hidden-layer" version of the MNIST model

We'll start with a model graph that has only one hidden layer, then as part of the lab, you'll add another hidden layer.

Start by looking at [`mnist_onehlayer.py`](./mnist_onehlayer.py).
See if you can find where the 'hidden layer' part of the model graph is defined.

## Running the example as a script

Run this script like this:
```sh
$ python mnist_onehlayer.py
```

## Running the example in a Jupyter notebook

You can also run the example as a Jupyter (ipython) notebook.
Start up the Jupyter server from this directory like this, if it's not already running:

```sh
$ jupyter notebook
```

When the server starts up, it may bring up a tab automatically in your browser. If not, visit
`http://localhost:8888/`.  Then select `mnist_onehlayer.ipynb`.

## TensorBoard

View the result in TensorBoard (refer to the previous lab if you forget how to start it up). You'll find the generated summary info under `/tmp/tfmodels/mnist_onehlayer`.
If you have a TensorBoard server already running, kill the old one first. 

While it's running, we'll take a closer look at the code.  In addition to defining an interesting model graph, this code shows (at least) a couple more interesting new things.  It demonstrates how to checkpoint models and then how to load and reuse a model from a checkpoint file. (`DNNClassifier`, in the previous lab, did this for you.)
It also shows how to generate summary info for TensorBoard.  (`DNNClassifier` did this for you as well.)

## Exercise: The "two-hidden-layer" version

Now that we've explored the [`mnist_onehlayer.py`](./mnist_onehlayer.py) code, see if you can figure out how to make two changes to it.

First, **add a second hidden layer** to the model graph. For this second layer, use 32 hidden units.

Next, see if you can figure how how to add *accuracy* information (in addition to *loss*) to the summaries being written for TensorBoard.

If you get stuck, the changes are here: [`mnist_hidden.py`](./mnist_hidden.py).

