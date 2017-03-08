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

# This file is a modification of the code here:
# https://github.com/dennybritz/cnn-text-classification-tf

from collections import Counter
import itertools
import json
import re

import numpy as np

import tensorflow as tf

vocabulary_mapping = None
vocabulary_inv = None


def clean_str(string):
    """
    Tokenization/string cleaning for all datasets except for SST.
    Original taken from
    https://github.com/yoonkim/CNN_sentence/blob/master/process_data.py
    """
    string = re.sub(r"[^A-Za-z0-9(),!?\'\`]", " ", string)
    string = re.sub(r"\'s", " \'s", string)
    string = re.sub(r"\'ve", " \'ve", string)
    string = re.sub(r"n\'t", " n\'t", string)
    string = re.sub(r"\'re", " \'re", string)
    string = re.sub(r"\'d", " \'d", string)
    string = re.sub(r"\'ll", " \'ll", string)
    string = re.sub(r",", " , ", string)
    string = re.sub(r"!", " ! ", string)
    string = re.sub(r"\(", " \( ", string)
    string = re.sub(r"\)", " \) ", string)
    string = re.sub(r"\?", " \? ", string)
    string = re.sub(r"\s{2,}", " ", string)
    return string.strip().lower()


def load_data_and_labels(
        cat1=None, cat2=None, x_text=None,
        positive_examples=None, negative_examples=None
        ):
    """
    Loads two-category data from files, splits the data into words and
    generates labels. Returns split sentences and labels.
    """
    if not x_text or not positive_examples or not negative_examples:
        # Load data from files
        print("Loading data from {} and {}".format(cat1, cat2))
        positive_examples = list(open(cat1, "r").readlines())
        positive_examples = [s.strip() for s in positive_examples]
        negative_examples = list(open(cat2, "r").readlines())
        negative_examples = [s.strip() for s in negative_examples]
        # Split by words
        x_text = positive_examples + negative_examples
        x_text = [clean_str(sent) for sent in x_text]
        x_text = [s.split(" ") for s in x_text]
    # Generate labels
    print("Generating labels...")
    positive_labels = [[0, 1] for _ in positive_examples]
    negative_labels = [[1, 0] for _ in negative_examples]
    y = np.concatenate([positive_labels, negative_labels], 0)
    positive_examples = None
    negative_examples = None
    return [x_text, y]


def build_vocab_mapping(run="", write_mapping=True,
                        cat1=None, cat2=None
                        ):
    """
    Generate vocabulary mapping info, write it to disk for later eval.
    This ensures that the mapping used for the eval is the same.
    """

    print("Building the vocabulary mapping. " +
          "This will take a while for large datasets.")
    # Load data from files
    positive_examples = list(open(cat1, "r").readlines())
    positive_examples = [s.strip() for s in positive_examples]
    negative_examples = list(open(cat2, "r").readlines())
    negative_examples = [s.strip() for s in negative_examples]
    # Split by words
    x_text = positive_examples + negative_examples
    print("cleaning...")
    x_text = [clean_str(sent) for sent in x_text]
    print("splitting...")
    x_text = [s.split(" ") for s in x_text]
    print("building indexes...")
    padded_sentences = pad_sentences(x_text)
    vocabulary_mapping, vocabulary_inv = build_vocab(
        padded_sentences)
    vocab_file = "vocab{}.json".format(run)
    if write_mapping:
        print("writing vocab file {}".format(vocab_file))
        with open(vocab_file, "w") as f:
            f.write(json.dumps(vocabulary_mapping))
    return [x_text, positive_examples, negative_examples, padded_sentences, vocabulary_mapping, vocabulary_inv]


def pad_sentences(sentences, padding_word="<PAD/>",
                  max_sent_length=60):
    """
    Pads all sentences to the same length. The length is defined by the min of
    the longest sentence and a given max sentence length.
    Returns padded sentences.
    """
    sequence_length = max(len(x) for x in sentences)
    # cap sentence length
    print("setting seq length to min of {} and {}".format(
        sequence_length, max_sent_length))
    sequence_length = min(sequence_length, max_sent_length)
    print("capped longest seq length: {}".format(sequence_length))
    padded_sentences = []
    for i in range(len(sentences)):
        # truncate as necessary
        sentence = sentences[i][:sequence_length]
        num_padding = sequence_length - len(sentence)
        new_sentence = sentence + [padding_word] * num_padding
        padded_sentences.append(new_sentence)
    return padded_sentences


def build_vocab(sentences, max_vocab=30000):
    """
    Builds a vocabulary mapping from word to index based on the sentences.
    Returns vocabulary mapping and inverse vocabulary mapping.
    """

    # Build vocabulary. Cap to a max.
    word_counts = Counter(itertools.chain(*sentences))
    # Mapping from index to word. Use the 'max_vocab' most common.
    vocabulary_inv = [x[0] for x in word_counts.most_common(max_vocab)]
    vocabulary_inv = list(sorted(vocabulary_inv))
    # Mapping from word to index
    vocabulary = {x: i for i, x in enumerate(vocabulary_inv)}
    return [vocabulary, vocabulary_inv]


def get_embeddings(vocab_size, embedding_size, emb_file):  # expected sizes
    """..."""
    # create a matrix of the right size
    embeddings = np.random.uniform(
        -1.0, 1.0, size=(vocab_size, embedding_size)).astype('float32')
    # get the vocabulary mapping info
    if not vocabulary_mapping:
        # should have already generated the vocab mapping
        print("Don't have vocabulary mapping.")
        return None
    vocabulary = vocabulary_mapping
    if len(vocabulary) != vocab_size:
        print('vocab size mismatch: %s vs %s' % (vocab_size, len(vocabulary)))
        return None
    # read and parse the generated embeddings file
    try:
        with open(emb_file, "r") as f:
            for line in f:
                edict = json.loads(line)
                key = list(edict.keys())[0]
                # see if key is in the vocab
                if key in vocabulary:
                    # then add the embedding vector
                    emb = edict[key][0]
                    if len(emb) != embedding_size:
                        print(
                            "embedding size mismatch for word {}: " +
                            "{} vs {}".format(
                                key, embedding_size, len(emb)))
                        return None
                    vocab_idx = vocabulary[key]
                    embeddings[vocab_idx] = emb
        return tf.convert_to_tensor(embeddings)
    except Exception as e:
        print(e)
        return None


def build_input_data(sentences, labels, vocabulary):
    """
    Maps sentencs and labels to vectors based on a vocabulary.
    """

    # With capped vocab, need to account for word not present in
    # vocab. Using the padding word.
    # TODO -- pass padding word in as an arg
    padding_word = "<PAD/>"
    pad_idx = vocabulary[padding_word]
    x = np.array(
        [[vocabulary.get(word, pad_idx) for word in sentence] for sentence in sentences])
    y = np.array(labels)
    return [x, y]


def load_data(run="", cat1=None, cat2=None,
              eval=False, vocab_file=None):
    """
    Loads and preprocessed data for the MR dataset.
    Returns input vectors, labels, vocabulary, and inverse vocabulary.
    """
    x_text = None
    positive_examples = None
    negative_examples = None
    padded_sentences = None

    print("eval mode: {}".format(eval))
    print("vocab file: {}".format(vocab_file))
    # Load and preprocess data
    if eval:  # in eval mode, use the generated vocab mapping.
        print("loading generated vocab mapping")
        with open(vocab_file, "r") as f:
            mapping_line = f.readline()
            vocabulary_mapping = json.loads(mapping_line)
    else:
        x_text, positive_examples, negative_examples, padded_sentences, vocabulary_mapping, vocabulary_inv = build_vocab_mapping(
            run=run, cat1=cat1, cat2=cat2)
    print("building training data structures")
    sentences, labels = load_data_and_labels(
        cat1=cat1, cat2=cat2,
        x_text=x_text, positive_examples=positive_examples,
        negative_examples=negative_examples)
    if not padded_sentences:
        padded_sentences = pad_sentences(sentences)
    print("Building input data...")
    x, y = build_input_data(padded_sentences, labels, vocabulary_mapping)
    return [x, y, vocabulary_mapping, vocabulary_inv]


def batch_iter(data, batch_size, num_epochs, shuffle=True):
    """
    Generates a batch iterator for a dataset.
    """
    data = np.array(data)
    data_size = len(data)
    num_batches_per_epoch = int(len(data)/batch_size) + 1
    for epoch in range(num_epochs):
        # Shuffle the data at each epoch
        if shuffle:
            shuffle_indices = np.random.permutation(np.arange(data_size))
            shuffled_data = data[shuffle_indices]
        else:
            shuffled_data = data
        for batch_num in range(num_batches_per_epoch):
            start_index = batch_num * batch_size
            end_index = min((batch_num + 1) * batch_size, data_size)
            yield shuffled_data[start_index:end_index]
