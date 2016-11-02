# Scalable Tensorflow

## Set Up

This section assumes you completed the [Optional Cloud ML Setup](https://github.com/amygdala/tensorflow-workshop/blob/master/INSTALL.md#optional-get-started-with-google-cloud-machine-learning)


## Define the Model - Exercise 1

We have provided a skeleton model file at `trainer/model_skeleton.py`: This skeleton demonstrates how to conditionally construct a model based on a `mode` parameter. Fill this in with the TensorFlow code from previous sections on MNIST. 

Additionally, the `args` namespace provides three hyperparameters `args.learning_rate`, `args.hidden1`, and `args.hidden2` which you should use to define your model.

To validate that you have correctly implemented `make_model_fn`

Open a python interpreter with `python` and run:

```
import tensorflow as tf
import task
from model_skeleton import make_model_fn, METRICS
import argparse
parser = argparse.ArgumentParser()
task.model_arguments(parser)
args = parser.parse_args(args=[])
mnist_estimator = tf.contrib.learn.Estimator(model_fn=make_model_fn(args), model_dir='output/')
from tensorflow.examples.tutorials.mnist import input_data
mnist_data = input_data.read_data_sets("data",
                                       one_hot=False,
                                       validation_size=0)
mnist_estimator.fit(x=mnist_data.train.images,
                    y=mnist_data.train.labels,
                    batch_size=256,
                    max_steps=10000)
```

If this produces loss logs you have successfully implemented `make_model_fn`:

You can also now run:

```
mnist_estimator.evaluate(x=mnist_data.test.images,
                         y=mnist_data.test.labels,
                         metrics=METRICS)
```

Which will give you the trained accuracy


After you are finished, replace the provided solution with your own implementation:

```
mv trainer/model.py trainer/model_solution.py
mv trainer/model_skeleton.py trainer/model.py
```


## Data I/O

TFRecords are a scalable way to provide data to TensorFlow training processes. They allow you to read data from files (local or Google Cloud Storage) as part of TensorFlow graph execution.

### Writing TFRecords - Exercise 2

```
from tensorflow.examples.tutorials.mnist import input_data
mnist = input_data.read_data_sets("data",
                                  one_hot=False,
                                  validation_size=0)
```

Gives you a `Dataset` object with two members `mnist.train` and `mnist.test`, each of these datasets has `images` A numpy array where each row is an images pixels (flattened), and `labels` A numpy vector`.

Write a Python CLI which writes each of `mnist.train` and `mnist.test` to a TFRecords file, containing `tf.train.Examples` with two features:

* `'labels'`: An `tf.train.Int64List` containing the single label value for the example
* `'images'`: A `tf.train.FloatList` containing the flattened array of pixels for the example

The solution can be found in `prepare_data.py` and run with:

```
python prepare_data.py data/
```

After you have successfully completed this optionally copy to Google Cloud Storage, run

```
gsutil -m cp -z pb2 -r data/* gs://$BUCKET_NAME/
```

Set the `DATA_LOCATION` environment variable to follow along with the rest of the tutorial

```
DATA_LOCATION=data
```
for local or
```
DATA_LOCATION=gs://$BUCKET_NAME
```
for remote

### Reading TFRecords - Exercise 3

Next we need to provide an `input_fn` to our model. When called `input_fn` will add TFRecord reading operations to our graph, and return two Tensors: `features` and `targets` which represent the training data and labels of our model.

For readability we break this down into two functions you must implement:

* `parse_examples` should take a string Tensor of TFRecord Examples, and return two tensors, representing `'images'` and `'labels'` from part one

* `make_input_fn` should have the signature:

```
def make_input_fn(files, example_parser, batch_size, num_epochs=None):
```

Which takes a list of file names, our `parse_examples` function, an integer batch size, and an optional number of times to read through the data, and returns a valid `input_fn`, which when called with no arguments, returns two tensors representing a *batch* of `'images'` and `'labels'` respectively.

To validate that your functions are working, you can `mv trainer/util.py trainer/util_solution.py` and in your own `trainer/util.py` first implement `parse_examples`, and `from util_solution import * `

Then run:

```
gcloud beta ml local train --package-path trainer/ --module-name trainer.task \
    -- \
    --max-steps 50000 \
    --train-data-paths $DATA_LOCATION/train.pb2 \
    --eval-data-paths $DATA_LOCATION/eval.pb2 \
    --output-path output/
```

If you start seeing losses logged you have successfully implemented parse_examples.

You can then implement `make_input_fn` and remove `from  util_solution import * ` and validate the same way.

## Putting it all Together: The Experiment function - Exercise 4

To run, we simply need to write `make_experiment_fn` found in `trainer/task_skeleton.py`. We pass an `experiment_fn` to `learn_runner.run` which uses environment variables from `gcloud ml local run` to move between distributed and local training seamlessly.

Once you have written your `make_experiment_fn` move the skeleton file to the original task files location:

```
mv trainer/task.py trainer/task_solution.py
mv trainer/task_skeleton.py trainer/task.py
```


 and run `gcloud beta ml local train` as before. Also try running it with `--distributed` ! 

## Hyperparameter Tuning - Exercise 5

If you run:

```
git diff trainer/task.py
```

You'll notice that we have omitted some environment variable processing from `trainer/task_skeleton.py`. This is the sole code change required to enable hyperparameter tuning so apply it to your code now (you can copy it from `trainer/task_solution.py`.

The Google Cloud Machine Learning API provides tuning of hyper parameters through command line flags, and tensorboard summaries. The tuning system adjusts the values of flags specified in your [Job Configuration](https://cloud.google.com/ml/reference/rest/v1beta1/projects.jobs#TrainingInput), and watches the value of a special Tensorboard metric:
`training/hptuning/metric` which we defined in `trainer/model.py` to be the accuracy.


Use the reference documentation linked above to define a Job Configuration for our model in `my_config.yaml`. It should provide tuning of the `hidden1`, `hidden2`, and `learning-rate` flags.

Run it on the cloud (if `$DATA_LOCATION` is a GCS bucket only) with:

```
gcloud beta ml jobs submit training mnist_hptuning \
    --staging-bucket $DATA_LOCATION \
    --package-path trainer/ \
    --module-name trainer.task \
    --config config.yaml \
    -- \
    --max-steps 50000 \
    --train-data-paths $DATA_LOCATION/train.pb2 \
    --eval-data-paths $DATA_LOCATION/eval.pb2 \
    --output-path $DATA_LOCATION/output
gcloud beta ml jobs stream-logs mnist_hptuning
```
