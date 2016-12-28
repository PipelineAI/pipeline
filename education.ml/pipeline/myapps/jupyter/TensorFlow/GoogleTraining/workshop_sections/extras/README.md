
# 'Extra' examples

This directory contains some sections that aren't part of the primary set of workshop labs, but illustrate some interesting and fun things.

- [Introducing word2vec](intro_word2vec/README.md)
- [A less basic version of word2vec](workshop_sections/word2vec_optimized/README.md)
- [Using convolutional NNs for text classification, and TensorBoard](workshop_sections/cnn_text_classification/README.md#using-convolutional-nns-for-text-classification-and-tensorboard)
- [Using convolutional NNs for text classification, part II: using learned word embeddings](workshop_sections/cnn_text_classification/README.md#using-convolutional-nns-for-text-classification-part-ii-using-learned-word-embeddings)

If you would like to play around with them, they "should" still work, though they have not been tested with the current version of TensorFlow (version .11 as of this writing).

## Download data files for the workshop exercises

For some of these sections, we'll have you download some data files. For convenience, we list them here:

https://storage.googleapis.com/oscon-tf-workshop-materials/saved_word2vec_model.zip
https://storage.googleapis.com/oscon-tf-workshop-materials/processed_reddit_data/reddit_post_title_words.zip
https://storage.googleapis.com/oscon-tf-workshop-materials/processed_reddit_data/news_aww/reddit_data.zip
https://storage.googleapis.com/oscon-tf-workshop-materials/learned_word_embeddings/reddit_embeds.zip

(Thanks to [reddit](https://www.reddit.com/), for allowing us to use some post data for a training corpus.)

If you're running the Docker image, and have mounted a data directory via something like the following (as in the installation instructions):

```sh
$ docker run -v `pwd`/workshop-data:/workshop-data -it \
    -p 6006:6006 -p 8888:8888 gcr.io/google-samples/tf-workshop:v3
```

then copy the downloads into that directory so that they're accessible both inside and outside the container.

Thanks to Denny Britz for this code: [https://github.com/dennybritz/cnn-text-classification-tf](https://github.com/dennybritz/cnn-text-classification-tf), which we adapted for some of the workshop sections.

Thanks also to [reddit](https://www.reddit.com/), for allowing us to use some post data for a training corpus.
