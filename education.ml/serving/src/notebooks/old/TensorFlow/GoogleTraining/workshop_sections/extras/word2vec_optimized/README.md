
# A less basic version of word2vec

In this section of the workshop, we're going to look at a more optimized version of the word2vec code.

This directory contains a slightly modified version of the `word2vec_optimized.py` file in the `models/embedding` [directory](https://github.com/tensorflow/tensorflow/blob/master/tensorflow/models/embedding/word2vec_optimized.py) of the TensorFlow repo.
See that directory's README for further detail.

The graph for this model (click for larger version):

<a href="https://storage.googleapis.com/oscon-tf-workshop-materials/images/word2vec_optimized.png" target="_blank"><img src="https://storage.googleapis.com/oscon-tf-workshop-materials/images/word2vec_optimized.png" width="500"/></a>

The script in this directory has been modified from the original to allow a mode that doesn't run the model training, but instead just restores the model from saved checkpoint info, then starts up an interactive shell to let you explore the results.
(We'll do that below).

First, download and unzip [this file](https://storage.googleapis.com/oscon-tf-workshop-materials/processed_reddit_data/reddit_post_title_words.zip).

This data comes from [reddit](https://www.reddit.com/), and contains words from post titles from the
'news' and 'aww' subreddits over several months. Used with permission.

Then, create a `/tmp/word2vec_optimized` directory in which to store the saved model info.

Start the model training with the following command. In the flags, specify the location of the unzipped
`reddit_post_title_words.txt` file, and the directory you just created in which you're going to save your model info.


```sh
$ python word2vec_optimized.py --train_data=reddit_post_title_words.txt \
    --eval_data=questions-words.txt \
    --save_path=/tmp/word2vec_optimized \
    --train=true --epochs_to_train=1
```

Due to the workshop time constraints, we're just training for 1 epoch here. (This won't properly train the model).
If we have time, we'll take a quick look at the code while it's running.

Note: `questions-words.txt` is tailored towards the original example corpus, not the reddit one. In this case it doesn't matter, as the eval results won't impact the number of epochs we run.

## Load and use the saved model results

We can save a graph's structure and values to disk -- both while it is training, to checkpoint, and when it is done training, to later use the final result.

`word2vec_optimized` shows how to restore graph variables from disk: we'll restore the model with checkpointed learned variables. (The [`../cnn_text_classification`] example will show how to load the graph structure from disk as well).

Because you won't have time to fully train your model during this workshop, download pregenerated model checkpoint data from an already-trained model [here](https://storage.googleapis.com/oscon-tf-workshop-materials/saved_word2vec_model.zip).
Unzip it, which should create a `saved_word2vec_model` directory.
In the directory, you should see some `model.ckpt*` files as well as a file named `checkpoint`.

Then, run the following command, this time pointing to the directory that contains the saved model info (instead of `/tmp` like you did above). Don't include the `--train=true` flag this time.

```sh
$ python word2vec_optimized.py --save_path=saved_word2vec_model \
    --train_data=reddit_post_title_words.txt \
    --eval_data=questions-words.txt
```

Without the `--train` flag, the script builds the model graph, and then restores the saved model variables to it, then starts an interactive shell.

Note: because of the way we're co-opting an existing example to do this, it's still necessary to pass in the training corpus, but this time it won't be used.

### Use the trained model to do word relationship analyses

In the interactive shell, with the model loaded, play with the `model.analog()` and `model.nearby()` methods.

For example, try:

```
model.analogy(b'cat', b'kitten', b'dog')
```
and

```
model.nearby([b'cat', b'octopus', b'president'])
```

We are evaluating a subpart of the learned model graph to emit these relationships.

### Get the embedding vector for a given word

We've modified the original version of this example to add code that evaluates the learned word vector for a given word.

Still in the interactive shell, try something like:

```
model.get_embed(b'cat')
```

If there is time, we'll take a quick look at the `build_get_embed_graph()` and `get_embed()` methods.

## Coming up soon: using the learned embeddings

In the [`../cnn_text_classification`] section, we'll use the word vectors (the embeddings) learned by this model, to improved the performance of a different model -- a convolutional NN for text classification.
