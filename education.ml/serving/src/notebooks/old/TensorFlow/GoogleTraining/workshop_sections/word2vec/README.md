# Building a Custom Estimator for word2vec

The word2vec model produces vector embeddings of words, using proximity in a text corpus, and a sampling loss called "noise contrastive estimation". For more details check out [the tutorial on tensorflow.org](https://www.tensorflow.org/tutorials/word2vec/).

This walkthrough assumes that your working directory is that of this `README` file.

## Getting set up

First make sure you have installed TensorFlow in your python environment.

Previous workshop sections have instructions on how to install TensorFlow.

There are a number of options for how to train. Using preprocessed data from the cloud, locally, or preprocessing your own data from the corpus.


### Option 1. Preprocessed files

We have provided 3 preprocessed files, for your use directly in training. The preprocessing uses the [text8](http://mattmahoney.net/dc/textdata) which is a cleaned Wikipedia text dump. We then counted words in this corpus and created a "vocabulary file" containing the `2 ^ 15 - 1` most common words in the corpus in order of their occurance; `text8-index.tsv`. We then used this index to convert the corpus into integers, and split it 90-10 into a training data and test data corpus.

#### 1a. Training from files in the Cloud.

Since many TensorFlow ops use the `GFile` abstraction for reading from the filesystem, and `GFile` supports Google Cloud Storage (GCS), you can train without copying files locally. Simply train locally with the following command:

```
python word2vec/task.py \
    --output-path output/1 \
    --vocab-file gs://tf-ml-workshop/word2vec/data/text8-vocab.tsv \
    --train-data-file gs://tf-ml-workshop/word2vec/data/text8-train.pb2 \
    --eval-data-file gs://tf-ml-workshop/word2vec/data/text8-eval.pb2
```

#### 1b. Pulling files locally

If you want to pull files ahead of time and train locally, simply copy them from the public GCS bucket.

```
gsutil cp -r gs://tf-ml-workshop/word2vec/data/* ./
```

Then use the local paths in your command, as follows:

```
python word2vec/task.py \
    --output-path output/1 \
    --vocab-file text8-vocab.tsv \
    --train-data-file text8-train.pb2 \
    --eval-data-file text8-eval.pb2
```


### Option 2. Doing your own preprocessing

The original text8 corpus is available publicly, to download run:

```sh
curl http://mattmahoney.net/dc/text8.zip -o text8.zip
unzip text8.zip
```

Then install preprocessing dependencies and run the minmal preprocessing script (implemented in `numpy`)

```
pip install nltk
python -c "import nltk; nltk.download('punkt')"
python preprocess.py --text-file text8 --output-path text8
```

This will produce the three necessary files (A vocab, training data, and evaluation data) as in `1b` above. You can run with the same command as `1b`.

## Training the model

The model supports training locally, and in the cloud, both single worker, and distributed execution with no code changes. We have covered local single worker execution above.

### Local Distributed Execution with the Google Cloud ML Local Emulator

The `gcloud` CLI ships with a set of commands for interacting with the Google Cloud ML api `gcloud beta ml`. One of these commands is the local emulator, `gcloud beta ml local train` which runs your TensorFlow code locally in an environment which emulates that of Google Cloud ML workers. This can be an easy way to validate distributed code you plan to run in the Cloud.

You can run the following command *using either local or cloud data files* to train your model.

NOTE: The unnamed parameter `--` seperates user program args from `gcloud` args. Arguments after `--` are passed through to the user's program and ignored by `gcloud`.

```
gcloud beta ml local train \
    --package-path word2vec \
    --module-name word2vec.task \
    -- \
    --output-path output/1 \
    --vocab-file gs://tf-ml-workshop/word2vec/data/text8-vocab.tsv \
    --train-data-file gs://tf-ml-workshop/word2vec/data/text8-train.pb2 \
    --eval-data-file gs://tf-ml-workshop/word2vec/data/text8-eval.pb2
```

This will run your model in 5 processes (2 parameter servers and 2 workers). You will see output from all 5 in your terminal.

### Training in the Cloud with the Google Cloud Machine Learning API

To train in the Cloud, we use `gcloud` to submit a training job. It's important to note that here we must use a Google Cloud Storage output directory. Make sure you have set up a Google Cloud Storage bucket and authorized the Google Cloud ML API to access it. See instructions [here](../../INSTALL.md#project-and-cloud-ml-setup).

After you have completed this, set `BUCKET=gs://my-bucket` with your bucket's name, for the commands below.

Then submit a training job with:

```
gcloud beta ml jobs submit training myword2vectraining \
  --staging-bucket ${BUCKET} \
  --module-name word2vec.task \
  --package-path word2vec \
  -- \
  --output-path ${BUCKET}/word2vec/output/run1 \
  --vocab-file gs://tf-ml-workshop/word2vec/data/text8-vocab.tsv \
  --train-data-file gs://tf-ml-workshop/word2vec/data/text8-train.pb2 \
  --eval-data-file gs://tf-ml-workshop/word2vec/data/text8-eval.pb2
```

This will output TensorBoard summaries, and model checkpoints to `${BUCKET}/word2vec/output/run1`

NOTE: If you start multiple training jobs with the same `--output-path` they will load the trained model, and continue training. This can be useful for restarting canceled jobs, but if you wish to retrain a model from scratch, change the `--output-path`.

If you want to control the parallelism of the training job you can use the `--scale-tier` flag to `gcloud`. Run `gcloud beta ml jobs submit training --help` for more info.

Additional configuration can be added in a `config.yaml` file, which is passed with the `--config` flag. For the schema of this file check out the [Google Cloud ML Docs](https://cloud.google.com/ml/reference/configuration-data-structures#yaml_configuration_file)

## Viewing Embeddings With TensorBoard

To view embeddings (and other summaries) with TensorBoard, simply start TensorBoard pointing it at your output directory (in the Cloud or locally). For example:

```
tensorboard --logdir ${BUCKET}/word2vec/output/run1
```

If you want to play around in TensorBoard without training a model, you can use our pretrain model:

```
tensorboard --logdir gs://tf-ml-workshop/word2vec/example-output
```

NOTE: This will pull about 200 MB of data across the network, and may be initially slow.

## Loading Your Model For Prediction

This word2vec model predicts similarities between words using its trained embeddings.

When given a vector of words, the word2vec model outputs a generator of prediction tensors, and defers computation until `next()` is called on the generator.

For this model, the generator will output a dictionary of two tensors: 'predictions'` are the words most similar to the provided words, while `'values'` is a measure of their similarity to the provided words.

Since we defined our model with an `Estimator` we can load the trained model into a python intepreter fairly easy:

First run python and import the necessary files, and specify your output directory (from training).
```
python
import tensorflow as tf
import numpy as np
import model
my_output_dir = 'YOUR OUTPUT DIRECTORY HERE (CLOUD OR LOCAL)'
```

Alternatively if you want to try prediction without training your own model, you can use our pretrained model:

```
my_output_dir = 'gs://tf-ml-workshop/word2vec/example-output/'
```

Then initialize the trained estimator. You can make a new estimator object without retraining as long as you specify the same `model_dir` as your trained model. Trained parameters will be loaded from the most recent checkpoint!



```
word2vec_model = tf.contrib.learn.Estimator(
    model_fn=model.make_model_fn(
        vocab_file='gs://tf-ml-workshop/word2vec/data/text8-vocab.tsv',
        num_sim=8 # Number of similar words to print out for each input
    ),
    model_dir=my_output_dir
)
```

Now that we have an estimator object, to predict, simply input a pandas dataframe or numpy array into `word2vec_model.predict`.

For example,
```
results = word2vec_model.predict(x=np.array(['the', 'and']))
print(results.next())
```

## Hyperparameter tuning

To run hyperparameter tuning and search for the best `num_skips` and `skip_window` parameter you can use `hptuning_config.yaml` in the command.

NOTE: This will likely not work in the free trial due to quota restrictions on trial accounts.

```
gcloud beta ml jobs submit training myword2vechptuning4 \
  --staging-bucket ${BUCKET} \
  --module-name word2vec.task \
  --package-path word2vec \
  --config hptuning_config.yaml \
  -- \
  --output-path ${BUCKET}/word2vec/output/hptuning2 \
  --vocab-file gs://tf-ml-workshop/word2vec/data/text8-vocab.tsv \
  --train-data-file gs://tf-ml-workshop/word2vec/data/text8-train.pb2 \
  --eval-data-file gs://tf-ml-workshop/word2vec/data/text8-eval.pb2
```
