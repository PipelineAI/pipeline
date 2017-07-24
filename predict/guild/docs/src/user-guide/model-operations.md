---
layout: docs
title: Model operations
description: Guide for performing model operations in Guild
group: user-guide
---

Operations play a central role in Guild AI --- you perform operations
using the `guild` command line utility.

## Contents

* Will be replaced with the ToC
{:toc}

## Overview

Guild supports the development and serving of trained models in
TensorFlow. The general workflow looks like this:

1. Run [`prepare`](#prepare) as needed to prepare a model for training.
2. Run [`train`](#train) to train a model and calculate its validation
   accuracy.
3. Modify your model architecture and hyperparameters as needed to
   improve its validation accuracy. Retrain as needed (step 2).
4. Select the best trained model and run [`evaluate`](#evaluate) to
   calculate it's final accuracy.
5. If model performance is acceptable, use [`serve`](#serve) to run
   your model as a web service.

For a complete list of Guild commands,
see [Command reference](/command-reference/overview/).

## Operation commands

Each Guild operation must be defined in
the {% ref guild-project-file %}
as a *command*. Commands tell Guild how to perform operations.

Here are the operation definitions for the "intro" model defined in
the {% ref mnist-example-guild %}:

{% code %}
[model "intro"]

train           = intro
prepare         = intro --prepare
evaluate        = intro --evaluate
{% endcode %}

The `train` command in this case runs the `intro` Python module
(i.e. the
[`intro.py`](https://github.com/guildai/guild-examples/blob/master/mnist/intro.py) script
located in the project root) along with any project defined flags.

For more information on the format of these commands,
see
[Command specs](/project-reference/models/#command-specs).

{% insight %}
Rather than dictate how your code should be written, Guild interfaces
with your TensorFlow scripts as command line operations. This lets you
write canonical TensorFlow code without requiring additional software
libraries or otherwise changing your development workflow.
{% endinsight %}

To see the command Guild uses for a model and operation, use the
`--preview` command line option. For example, running this command in
the MNIST example:

{% term %}
$ guild train intro --preview
{% endterm %}

shows that the command Guild uses to train the "intro" model is:

{% term %}
  /usr/bin/python \
    -um intro \
    --datadir ./data \
    --rundir $RUNDIR \
    --batch_size 100 \
    --epochs 10
{% endterm %}

The additional command line options used when running the `intro`
module are defined in the `Guild` project file under the `flags`
section:

{% code %}
[flags]

datadir         = ./data
rundir          = $RUNDIR
batch_size      = 100
epochs          = 10
{% endcode %}

Alternative flag sections may be applied using the `--profile`
option. Named flags sections are applied over the unnamed flags
section (i.e. may be added or redefined). Additionally, individual
flag values may be specified using the `-F, --flag` option.

For example, to train the MNIST intro model over 20 epochs instead of
10, run:

{% term %}
$ guild train -F epochs=20
{% endterm %}

The *evaluate* and *serve* operations apply to trained models and so
require a run directory or `--latest-run` to indicate that the latest
run results should be used. Runs are implicitly associated with the
model specified in the training operation.

## Operation variables

Guild provides special variables that may be specified in commands and
flag values. Variables are referenced using the syntax:

    '$' + VARIABLE_NAME

For example, this project snippet illustrates how the `RUNDIR` can be
used for flag values.

{% code %}
[flags]

train_dir = $RUNDIR/train
eval_dir  = $RUNDIR/eval
{% endcode %}

Guild supports the following operation variables:

| `RUNDIR` | run directory created for train or specified by the user for run-specific operations |
| `GPU_COUNT` | number of GPUs found on the system |

All operation variables are defined in the process environment for
operations. For example, you may use the `RUNDIR` environment variable
in your training scripts.

## Flag profiles

Model operations support a `--profile` option, which may be used to
apply named flags to the operation.

For example, consider a "quick-train" flags section:

{% code %}
[flags "quick-train"]

max_steps        = 100
learning_rate    = 0.01
{% endcode %}

These flags can be applied to a train operation as follows:

{% term %}
$ guild train --profile quick-train
{% endterm %}

Note that Guild will also include unnamed flags in the operation. When
in doubt about the flags used for an operation, use the `--preview`
option to print the operation command without running it.

## Prepare

The `prepare` operation is used to perform a one-time preparation for
model training. This is commonly used to download and pre-process
large datasets used for training and validation.

You're free to define the prepare logic in a separate script
(i.e. Python module) or implement prepare as conditional behavior
based on a command line option flag. For example, the MNIST example
implements *prepare* only when the `--prepare` option is
specified. You can see that in the `prepare` attribute for the "intro"
model:

{% code %}
[model "intro"]

prepare         = intro --prepare
{% endcode %}

All of the flags defined for the model are used for the prepare
operation, so flags that specify where data should be stored are
provided in the same way for *prepare* as they are for *train*.

Models may specify files or directories that must be present for a
train operation using the `train_requires` attribute. The MNIST
example relies on the `data` subdirectory to run and so specifies this
directory as follows:

{% code %}
[model "intro"]

train_requires  = ./data
{% endcode %}

## Train

The `train` operation is used to train a model using training data. A
typical TensorFlow script will perform several operations during the
training procedure:

* Feed training data, with optional augmentation (random
  transformations) into the model
* Calculate training loss
* Update model weights to minimize training loss
* Log model statistics as summary events
* Calculate and log training and validation accuracies to track
  progress
* Occasionally snapshot a model either for retraining or serving

Your TensorFlow code is responsible for performing all of these steps
--- Guild `train` will run your script, but will not perform any of
these directly.

Guild supplements the training procedure by performing some additional
steps:

* Create a unique run directory, which should be made available to the
  training script as a location for logs and exported models
* Call the training script with the appropriate flags for the
  specified model and profile
* Collect and log system statistics such as GPU, CPU, and IO
  performance

This approach leverages standard TensorFlow coding practices and adds
enough additional functionality to improve developer workflow.

## Evaluate

To evaluate the latest trained model, run:

{% term %}
$ guild evaluate RUN
{% endterm %}

`RUN` may be a run directory or the flag `--latest-run`. To list
available runs, use `guild list-runs` --- paths printed by this
command may be used as values for `RUN`.

If the project does not define an `evaluate` command for a model,
Guild will print an error message when this operation is run.

{% insight %}

The `evaluate` command does not have special meaning for Guild, other
than that Guild knows not create a new run directory and instead
provides the run directory specified by the user for flag values. The
evaluate operation must be implemented by the user. By convention,
evaluate operations use exported models to calculate and print
accuracy for test data.

{% endinsight %}

## Serve

The `serve` operation may be used to host a trained model as a web
service, running in a light weight HTTP server.

For details on serving a trained model,
see [Guild Serve](/user-guide/guild-serve/).

## Next Steps

{% next /user-guide/guild-view/ %}Read about Guild View{% endnext %}
