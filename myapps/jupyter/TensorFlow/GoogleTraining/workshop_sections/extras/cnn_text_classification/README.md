

# Using Convolutional Neural Networks for Text Classification

In this part of the workshop, we'll look at using a [convolutional NN ](http://arxiv.org/abs/1408.5882) [for text classification](http://arxiv.org/abs/1504.01255).
We'll also look more closely at TensorBoard's capabilities, and how to write the information used by TensorBoard during model training.

The code in this section is modified from this example: [https://github.com/dennybritz/cnn-text-classification-tf](https://github.com/dennybritz/cnn-text-classification-tf), used with permission.
See [this great accompanying tutorial](http://www.wildml.com/2015/12/implementing-a-cnn-for-text-classification-in-tensorflow/) (as well as [this related post](http://www.wildml.com/2015/11/understanding-convolutional-neural-networks-for-nlp/)) for more detail on this tensorflow model as well as the general approach of using CNNs for text classification.

There are two stages to this part of the workshop.

This model includes a word-vector layer (that is, word embeddings, just as we encountered in the word2vec sections of the tutorial earlier).
First, we'll train the `cnn-text-classification` model "from scratch".
That is, we'll just initialize that embedding layer with a random uniform distribution.

Next, we'll train the model by initializing that embedding layer with learned word embeddings.
[There is evidence](http://arxiv.org/abs/1408.5882) that this can improve training and model performance.
Specifically, we will use the word embeddings learned from running the [`word2vec_optimized`](word2vec_optimized) model.
(In an earlier section, you started a training run to learn these embeddings, but we did not have time to let that training run for very long.  So here we'll just use pre-generated embeddings from a longer such run.)

Then, we'll compare the training runs from the two variants on TensorBoard.

## Using convolutional NNs for text classification, and TensorBoard

We'll start the process of training the model, and then take look at the graph while that's running.

### Download the training data

Create a `data` subdirectory in this directory, and `cd` into it.
Download the training corpus from [here](https://storage.googleapis.com/oscon-tf-workshop-materials/processed_reddit_data/news_aww/prepared_data.tar.gz), and unzip it in the `data` directory. This data contains preprocessed numpy arrays generated from the data [here](https://storage.googleapis.com/oscon-tf-workshop-materials/processed_reddit_data/news_aww/reddit_data.zip).
This data contains post titles from (a few months of) two reddit (https://www.reddit.com/) subreddits: 'news' and 'aww', used with permission. It's processed using the script in `data_helpers2.py`.


### Start training the model

Start the training process like this:

```sh
$ python train.py --output-dir <Directory to write checkpoints and summaries> --data-file <.npz datafile with numpy arrays "sentences" and "labels"> --vocab-file <json file with vocabulary mapping>
```
If you are running inside a Docker container make sure these directories and files are located inside a volume, so that you can keep the data after your container exits.

### A look at the text-CNN code

This is the TensorBoard-generated graph of the model (click for larger version). In TensorBoard, many of these nodes expand.

<a href="https://storage.googleapis.com/oscon-tf-workshop-materials/images/text-cnn-graph.png" target="_blank"><img src="https://storage.googleapis.com/oscon-tf-workshop-materials/images/text-cnn-graph.png" width="500"/></a>

We'll walk through the code and look at how the graph is constructed, and point out some of the TensorFlow ops of interest. Note the sections for the embedding layer, the convolution and max-pooling layers, and dropout.
Note also the use of `tf.name_scope()`,  which allows hierarchical names for operations.

We'll also take a look at how the SummaryWriter is being used-- this lets us track progress using TensorBoard.

### Launch TensorBoard

Once the training script has gone through its first checkpoint save, we can look at its progress in TensorBoard.

In a separate terminal window, start up Tensorboard. (Make sure that you've activated your conda environment in this new window).

```sh
$ tensorboard --logdir=runs
```

Note: if you're starting TensorBoard within a docker container, and you're using `docker-machine`, run

```sh
$ docker-machine ip
```
(outside the container) to find the IP address to use in your browser.  Otherwise, you should be able to bring up TensorBoard on `http://localhost:6006/`.

We'll walk through the information TensorBoard is providing, and trace the logged events back to the `SummaryWriter` calls we looked at in the code.

### Loading and using a trained model

We won't have time to go into details during our workshop, but take a look at [`eval.py`](eval.py) for an example of how saved model graph and variable information can be loaded and restored.  In this case, `eval.py` uses the most recent checkpoint data from a given training run to restore that model and use it for prediction. As part of this process, it needs to also access some graph nodes by name.

## Using convolutional NNs for text classification, part II: using learned word embeddings

Go ahead and Ctl-C your running training session, but keep TensorBoard running in the other window.

Next, let's initialize the embedding layer of this model with learned embeddings from the [`word2vec_optimized` model](../word2vec_optimized), learned using post title words from the same dataset that we're using here.
Recall from that section that we modified the original `word2vec_optimized` example to let us retrieve the vector embedding for a given word.
We used that capability to write all word embeddings to a file at the end of a training run.

Download this [generated word embeddings file](https://storage.googleapis.com/oscon-tf-workshop-materials/learned_word_embeddings/reddit_embeds.zip) and unzip it.
These word embeddings were generated via the [`word2vec_optimized`](../word2vec_optimized) model.
The source data came from reddit (https://www.reddit.com/) post titles from the 'news' and 'aww' subreddits, used with permission.

Now, start up another training run that uses the learned embeddings to initialized the embedding vectors.

```sh
$ python train.py --embeds_file all_reddit_embeds.json
```

We'll take a quick look at the code that does this initialization.

Look at the results in TensorBoard after this second training run has been running for a little while. The initial benefits of initializing with the learned embeddings should be visible.
You might need to reload to pick up the new run info.

You should see something like the following, where the blue values are without use of the learned embeddings, and the green values are with the learned embeddings.
(Click to enlarge. This shows the values from the 'dev test' set -- a similar effect is observed on the training set.

<a href="https://storage.googleapis.com/oscon-tf-workshop-materials/images/summaries_dev_embeds3.png" target="_blank"><img src="https://storage.googleapis.com/oscon-tf-workshop-materials/images/summaries_dev_embeds3.png" width="500"/></a>

