---
layout: docs
title: Integrating Guild with your project
description: How to use Guild to train, evaluate and serve an existing project
group: tutorials
---

{% note %}

As of May 4, 2017 some of the content in this tutorial is out-of-date
and will be updated shortly.

{% endnote %}

In this tutorial we'll add support for Guild to an existing TensorFlow
project with minimal modification.

## Contents

- Sample item
{:toc}

## Prerequisites

Ensure that you have Guild installed along with its
requirements. See [Setup](/getting-started/setup/) for details.

In this tutorial we'll work with the source files for
TensorFlow's
[MNIST tutorial](https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/tutorials/mnist). You
can clone the TensorFlow repository:

{% term %}
$ git clone https://github.com/tensorflow/tensorflow.git
{% endterm %}

Alternatively, download the source archive and unpack it:

{% term %}
$ wget https://github.com/tensorflow/tensorflow/archive/master.zip
$ unzip master.zip
{% endterm %}

Copy the MNIST tutorial code to a new location, where you'll modify
it:

{% term %}
$ cp -a tensorflow/tensorflow/examples/tutorials/mnist guild-mnist
{% endterm %}

This tutorial assumes your working directory will be the `guild-mnist`
directory created here.

## Review the MNIST project

The TensorFlow MNIST project in this tutorial is the implementation of
{% link https://www.tensorflow.org/tutorials/mnist/pros/ %}Deep MNIST
for Experts{% endlink %}. This project defines a convolutional neural
network with max pooling to achieve 99% accuracy on the MNIST test
data. We'll use the model as our starting point and incrementally add
Guild support to it.

You can apply the same steps to your TensorFlow project to integrate
Guild.

The script we'll be modifying is {% link
https://github.com/tensorflow/tensorflow/blob/master/tensorflow/examples/tutorials/mnist/mnist_with_summaries.py %}mnist_with_summaries.py{%
endlink %}.

Confirm that you can view the script help by running:

{% term %}
$ python mnist_with_summaries.py --help
{% endterm %}

You should see something like this:

{% code %}
usage: mnist_with_summaries.py [-h] [--fake_data [FAKE_DATA]]
                               [--max_steps MAX_STEPS]
                               [--learning_rate LEARNING_RATE]
                               [--dropout DROPOUT] [--data_dir DATA_DIR]
                               [--log_dir LOG_DIR]

optional arguments:
  -h, --help            show this help message and exit
  --fake_data [FAKE_DATA]
                        If true, uses fake data for unit testing.
  --max_steps MAX_STEPS
                        Number of steps to run trainer.
  --learning_rate LEARNING_RATE
                        Initial learning rate
  --dropout DROPOUT     Keep probability for training dropout.
  --data_dir DATA_DIR   Directory for storing input data
  --log_dir LOG_DIR     Summaries log directory
{% endcode %}

If you get errors when running this command, check the following:

- Is Python installed correctly on your system? Run `python --version`
  to make sure you're running a {% link
  https://www.tensorflow.org/get_started/os_setup#requirements
  %}version supported by TensorFlow{% endlink %}.

- Is TensorFlow installed correctly? Refer to TensorFlow {% link
  https://www.tensorflow.org/get_started/os_setup %}Download and
  Setup{% endlink %} for help installing TensorFlow on your system.

- Are you running the command from the directory containing
  `mnist_with_summaries.py`? Verify using `pwd` and `ls`.

If you continue to face problems viewing the help for this script,
open an issue at {% ref github-issues %} and we'll try to help.

## Initial training

Let's test the MNIST model by training it with a small number of
steps. This will accomplish a few things:

- Download the MNIST dataset used by the model
- Verify that we can train using TensorFlow
- Introduce the use of flags when running TensorFlow scripts

In the project directory, run:

{% term %}
$ python mnist_with_summaries.py \
    --data_dir data \
    --log_dir runs/initial \
    --max_steps 100
{% endterm %}

This should download the MNIST dataset, unpack it in a local `data`
subdirectory, and run a short training session (100 steps). It should
take less than a minute to train the model once the MNIST data has
been downloaded.

You should see output similar to this:

{% code %}
Successfully downloaded train-images-idx3-ubyte.gz 9912422 bytes.
Extracting data/train-images-idx3-ubyte.gz
Successfully downloaded train-labels-idx1-ubyte.gz 28881 bytes.
Extracting data/train-labels-idx1-ubyte.gz
Successfully downloaded t10k-images-idx3-ubyte.gz 1648877 bytes.
Extracting data/t10k-images-idx3-ubyte.gz
Successfully downloaded t10k-labels-idx1-ubyte.gz 4542 bytes.
Extracting data/t10k-labels-idx1-ubyte.gz
...
<Information about TensorFlow and GPUs used, if present>
...
Accuracy at step 0: 0.1116
Accuracy at step 10: 0.7011
Accuracy at step 20: 0.8197
Accuracy at step 30: 0.8562
Accuracy at step 40: 0.8757
Accuracy at step 50: 0.8809
Accuracy at step 60: 0.8945
Accuracy at step 70: 0.8841
Accuracy at step 80: 0.8913
Accuracy at step 90: 0.8925
Adding run metadata for 99
{% endcode %}

Let's look at the command we just ran:

- Python is used to run the training script `mnist_with_summaries.py`
- `--data_dir` specifies where the MNIST data is stored
- `--log_dir` specifies where event logs should be written
- `--max_steps` specifies how many steps we should run for our training

Let's take a look at what the script created:

{% term %}
$ find data
{% endterm %}

You should see:

{% code %}
data
data/t10k-images-idx3-ubyte.gz
data/train-images-idx3-ubyte.gz
data/t10k-labels-idx1-ubyte.gz
data/train-labels-idx1-ubyte.gz
{% endcode %}

These are the compressed image source files for the MNIST images. The
TensorFlow script reads from these files to get images for training
and validation.

Next, run:

{% term %}
$ find runs
{% endterm %}

You should see something similar to this:

{% code %}
runs
runs/initial
runs/initial/test
runs/initial/test/events.out.tfevents.1484063066.pueblo
runs/initial/train
runs/initial/train/events.out.tfevents.1484063065.pueblo
{% endcode %}

These are TensorFlow event logs, which are created by the training
script. They contain a variety of information about the model and its
training.

You can view the logs using {% link
https://www.tensorflow.org/how_tos/summaries_and_tensorboard/
%}TensorBoard{% endlink %} --- a visualization tool that comes with
TensorFlow. To launch TensorBoard, run:

{% term %}
$ tensorboard --logdir runs/initial
{% endterm %}

Open {% link http://127.0.1.1:6006 %}http://127.0.1.1:6006{% endlink
%} in your browser and click on **accuracy_1** and **cross_entropy_1**
--- these will expand to reveal time series charts for the two scalars.

{% screen tensorboard-screen-1.png %}

Explore the other features of TensorBoard, in particular the
**Graphs** tab, which provides a detailed visual interface to the
graph created by the training script.

Use TensorBoard in your own project to view TensorFlow event logs and
explore lower level details of your models.

{% insight %}

While the Python command above is relatively simple, it still requires
detailed knowledge of what the command does to use correctly. In the
next section we'll see how Guild simplifies the command, allowing
users to more effectively and consistently work with the model.

{% endinsight %}

## Create a Guild project

In this section we'll create a new Guild project file, which will let
us run Guild commands that simplify various model operations.

First confirm that Guild is available on your system:

{% term %}
$ guild --version
{% endterm %}

You should see the installed version of Guild. If you get an error
when running this command, confirm that you have followed the step
outlined in [Setup](/getting-started/setup/). If you still have
problems running Guild, open an issue in {% ref github-issues %}.

Next, create a Guild project file by running this command in the MNIST
project directory:

{% term %}
$ guild init \
    --name "MNIST Expert" \
    --train-cmd mnist_with_summaries
{% endterm %}

This should create a `Guild` file in the current directory.

- `--name` specifies the project name, which will be used whenever
  Guild refers to the project
- `--train-cmd` specifies the Python module used to train the mode ---
  in this case we specify the module used in the previous section

Verify that the project was initialized:

{% term %}
$ guild status
This directory is a Guild project.
{% endterm %}

You can also list the models defined for the project:

{% term %}
$ guild list-models
mnist_with_summaries
{% endterm %}

{% note %}

The `init` command created a `Guild` file in the project
directory. We'll explain its various parts through the remainder of
this tutorial but feel free to view it and read its annotations.

{% endnote %}

{% insight %}

The `Guild` file should be treated as project source code, along with
scripts and other supporting files. It is similar to the configuration
files for other support tools --- for example a `Makefile`.

{% endinsight %}

## Train using Guild

Even with the default configuration, we can now use Guild to train the
MNIST model. In the project directory, run:

{% term %}
$ guild train
{% endterm %}

You should see output similar to that generated in the first
training.

{% note %}

This operation uses the default `max_steps` setting, which is 10 times
more than our initial training --- be prepared to wait a few minutes
for this operation to finish. In the meantime, continue below with the
tutorial.

{% endnote %}

This command calls the `mnist_with_summaries` module, resulting in the
same training operation we ran earlier, however with a few
differences:

- Guild automatically adds values defined in the `flags` section as
  command line options to the training module.

- Guild creates a unique *run directory* for each training run and
  makes that value available to flags and as an environment variable.

- Guild logs information about the operation including the command,
  flags, and system attributes like CPU and GPU information.

- While the training operation runs, Guild collects additional system
  statistics such as CPU and GPU utilization in the background and
  logs them.

You'll learn how these additional steps benefit your development
workflow as we progress through this tutorial.

## Start Guild View

In a separate terminate, change to the MNIST example project directory
and run:

{% term %}
$ guild view
{% endterm %}

This will start *Guild View* --- a web application for viewing and
interacting with your Guild project. By default Guild View runs on
port `6333`. If you'd like to run it on a different port, use the
`--port` option.

In your browser, open {% link http://localhost:6333
%}http://localhost:6333{% endlink %}.

You should see a view that looks similar to this:

{% screen guild-view-screen-22.png %}

For an explanation of the various components here, refer
to [Guild View](/user-guide/guild-view/) in the user guide.

A few points are worth mentioning about the initial view:

- The project name **MNIST Expert** is used in the upper left to
  identify the project.

- The latest run is initially selected when you open the page --- you
  can select a different view using the dropdown selector next to the
  project name.

- The fields at the top of the page (**Validation Accuracy**,
  **Training Accuracy**, etc.) are empty --- we'll fix this in the
  sections below.

- The run **Status** is updated until either the run completes, an
  error occurs, or the run is stopped by the user.

- All of the flags applicable to the operation are displayed in the
  **Flags** table.

- System information including CPU and GPU information is displayed in
  the **Attributes** table (the values displayed for your system will
  be different).

- The CPU percent and memory used by the process are displayed in time
  series charts.

- The process **Output** is captured and displayed at the bottom of
  the page.

In the following sections we'll modify the Guild project and the
training script to complete our integration.

## Guild run directory integration

The integration between Guild and TensorFlow is designed to minimize
the impact on your TensorFlow code. There is however one important
consideration when using Guild, which is the proper use of the Guild
*run directory*.

The run directory --- commonly referenced as `RUNDIR` --- is a unique
directory that Guild creates for each training operation. Guild writes
its logs and other run specific data to this location.

To integrate Guild into your TensorFlow projects, log all TensorFlow
events to somewhere within the run directory hierarchy. Additionally,
model snapshots and exports should be saved within the hierarchy.

{% note %}

You are free to log events and save models to subdirectories under the
log directory. For example, it's common to store train logs in a
`train` subdirectory and validation or test logs to `validate` or
`test` respectively.

{% endnote %}

The run directory may be referenced one of two ways:

- Using `$RUNDIR` in a flag value
  (or [command spec](/project-reference/#command-specs))
- The `RUNDIR` environment variable, from the script

In our MNIST example, the train script writes TF event logs to
subdirectories under `FLAGS.log_dir`. To confirm this, scan
`mnist_with_summaries.py` for references to `FLAGS.log_dir` --- you'll
see it's used initialize the two event writers.

To integrate Guild with the training script, we need to define a flag
`log_dir` that will be set to the run directory. We'll do that in the
next section, along with defining the other flags for our project.

## Define project flags

Let's review the flags that our training script accepts. Here again is
the output when we run `python mnist_with_summaries.py --help`:

{% code %}
usage: mnist_with_summaries.py [-h] [--fake_data [FAKE_DATA]]
                               [--max_steps MAX_STEPS]
                               [--learning_rate LEARNING_RATE]
                               [--dropout DROPOUT] [--data_dir DATA_DIR]
                               [--log_dir LOG_DIR]

optional arguments:
  -h, --help            show this help message and exit
  --fake_data [FAKE_DATA]
                        If true, uses fake data for unit testing.
  --max_steps MAX_STEPS
                        Number of steps to run trainer.
  --learning_rate LEARNING_RATE
                        Initial learning rate
  --dropout DROPOUT     Keep probability for training dropout.
  --data_dir DATA_DIR   Directory for storing input data
  --log_dir LOG_DIR     Summaries log directory
{% endcode %}

We want to define the command line options defined as *flags* in our
`Guild` project.

{% insight %}

*Flags* in Guild represent input parameters to a model operation ---
these are often model hyperparameters but may be other flags as
well. Guild provides a number of features for using flags, including
model specific flags
and
[flag profiles](/user-guide/model-operations/#flag-profiles). Flags
may also be referenced in field values in Guild View. This is useful
when comparing runs that use different hyperparameters.

For more information, see [Flags](/project-reference/#flags) in the
project reference.

{% endinsight %}

To define the flags for your project, open the `Guild` project file in
a text editor and scroll to the `[flags]` section. It will look like
this (annotations have been removed below for clarity):

{% code %}
[flags]

data_dir                = data
train_dir               = $RUNDIR/train
eval_dir                = $RUNDIR/eval
max_steps               = 1000
batch_size              = 128
learning_rate           = 0.01
{% endcode %}

These are examples generated when we initialized the project --- they
should be changed according to your project scripts. We want to keep
some of the flags, delete others, and add those that are missing.

Modify the section to look like this:

{% code %}
[flags]

data_dir                = data
log_dir                 = $RUNDIR
max_steps               = 1000
dropout                 = 0.9
learning_rate           = 0.001
{% endcode %}

Note that we changed the following:

- Deleted `train_dir`, `eval_dir` and `batch_size` since these are not
  applicable to our script
- Added `log_dir` as `$RUNDIR` which will be replaced with the path to
  the run directory (either a new directory created for training or an
  existing run directory for evaluation, serving, etc.)
- Added `dropout` which is an option accepted by the script
- Modified the default value of `learning_rate` to match the default
  value in mnist_with_summaries.py (see the argument definition in the
  source code to confirm)

Save your changes to the `Guild` file.

## Preview train operation

After making changes to project flags, it's a good idea to confirm the
command options used for a given operation. Let's check the `train`
operation by running the command with `--preview`:

{% term %}
$ guild train --preview
{% endterm %}

If you specified the flags above the **Command** printed should be:

{% code %}
  /usr/bin/python \
    -um mnist_with_summaries \
    --data_dir data \
    --log_dir $RUNDIR \
    --max_steps 1000 \
    --dropout 0.9 \
    --learning_rate 0.001
{% endcode %}

{% insight %}

You can always run this command directly to test your script without
using Guild. This is useful in cases where your script is in
development mode and you don't want the overhead of creating new run
directories for every training operation.

Note also that if you use Python `pdb` module for interactive
debugging, you *must* run your script directly from Python as the
Guild training operation does not yet support tty interactions over
standard input.

{% endinsight %}

## Customize Guild View fields

In this section we'll modify the `Guild` project so that we can view
stats from the TensorFlow logs. These include:

- Training and test loss
- Training and test accuracy

<div id="validate-vs-test"></div>
{% note %}

The TensorFlow MNIST tutorial uses the term *test* to refer to the
process of validating model performance using data that has not been
used to train model weights. Guild uses the term *validate* for this
process, reserving the term *test* for the final measurement of model
performance. Strictly speaking, test results should not be used to
further adjust a model --- if they are, the become validation results
by definition and the developer must retest with new unseen test data
to measure final performance.

However, for the purposes of this tutorial, *test* and *validate* may
be considered interchangeable.

{% endnote %}

In the previous section, we configured the flag `log_dir` to be the
run directory. This lets us capture TensorFlow event logs for each
training run.

{% insight %}

Guild watches TensorFlow event logs and writes scalar values to its
own database in the run directory. As long as the event logs are
written within the run directory hierarchy Guild will be able to
capture TensorFlow summaries during an operation.

{% endinsight %}

Let's run the `train` command again, now that our flags are configured
so that TensorFlow event logs are written to the run directory.

In the project directory, run:

{% term %}
$ guild train
{% endterm %}

As the model trains, Guild writes various values to its own
logs. These include logged TensorFlow events and also system
information such as GPU and CPU utilization. Guild refers to these
values as *series* (short for time series) because each observation is
associated with a timestamp.

You can inspect the series captured by Guild using the `list-series`
command.

In the project directory, run:

{% term %}
$ guild list-series --latest-run
{% endterm %}

This will print a list of series keys that exist for the latest
run. You can run this command at any time for a run --- you don't have
to wait for the run to finish.

You should see a list similar to this:

{% code %}
op/cpu/util
op/mem/rss
op/mem/vms
sys/cpu/util
...<more sys/* keys>...
tf/test/accuracy_1
tf/test/cross_entropy_1
tf/test/dropout/dropout_keep_probability
...<more tf/test/* keys>...
tf/train/accuracy_1
tf/train/cross_entropy_1
tf/train/dropout/dropout_keep_probability
...<more tf/train/* keys>...
{% endcode %}

Look for series starting with `tf/` --- these are TensorFlow scalar
summaries. If you don't see any in the list, wait a few seconds and
try again --- it can take a few seconds for logs to be flushed and
updated.

If after a few seconds you still don't see series starting with `tf/`,
verify that your `Guild` [project flags](#define-project-flags) are
correct and that the preview of the `train` command is as expected
(again, see the example above). If after confirming that the flags
being used for the train command are correct you still don't see
`tf/*` series when you run `list-series` open an issue on {% ref
github-issues %}.

Our goal here is to define our fields in the `Guild` project so that
this view:

{% screen guild-view-screen-24.png %}

looks something like this instead:

{% screen guild-view-screen-25.png %}

To accomplish this we need to tell Guild the correct sources for each
of these fields.

In your text editor, open the `Guild` project file and scroll to the
`field` sections, which are toward the bottom of the file.

The sections we'll modify look like this:

{% code %}
[field "validation-accuracy"]

source          = series/tf/test/accuracy

[field "train-accuracy"]

source          = series/tf/train/accuracy

[field "steps"]

source          = series/tf/train/loss

[field "time"]

source          = series/tf/train/loss
{% endcode %}

The fields in the Guild View (i.e. the colored panels at the top of
the **Train** page) are initially empty because these source values
don't correspond to any of the `tf/*` series logged by
`mnist_with_summaries.py`. To display the expected values, we need to
modify these values.

Reviewing the output from the `list-series` command, we see that
*accuracy* and *loss* are named differently:

- *accuracy* is named `accuracy_1`
- *loss* is named `cross_entropy_1`

This slightly odd names are simply because `mnist_with_summaries.py`
does not explicitly name the summaries and default values are
used. Rather than modify the TensorFlow source code, in this case,
we'll simply change the values for `source` to match the values
written to the logs.

Modify the four `field` sections to be the following:

{% code %}
[field "validation-accuracy"]

source          = series/tf/test/accuracy_1

[field "train-accuracy"]

source          = series/tf/train/accuracy_1

[field "steps"]

source          = series/tf/train/cross_entropy_1

[field "time"]

source          = series/tf/train/cross_entropy_1
{% endcode %}

{% todo %}
Horrible user experience! While it's great to support full definitions
of fields and series, having to create all these sections just to
remap source names --- which is something that users will need to do
all the time --- is pure silly business.

We need a simple map somewhere. This could even go right in the view
section.

    [view]

    source-map   = validation-accuracy=series/tf/test/accuracy_1 \
                   train-accuracy=series/tf/train/accuracy_1

Ugh, not much better!

Well, let's see how this goes --- maybe something more obvious will come up.
{% endtodo %}

Note here that Guild uses the term "validation" to refer to the values
stored in the *test* logs. See the [note above](#validate-vs-test) for
an explanation of why these terms are different in this example.

The fields `steps` and `time` display the number of steps (i.e. batch
training iterations) performed during the run and the time of the run
respectively. They both use the source
`series/tf/train/cross_entropy_1` which is not obviously associated
with either steps or time! This is a by product of how series values
are stored, both in TensorFlow and similarly in Guild logs --- *any*
series contains step and time information, which can be used to
calculate the total steps and time for an operation. In this case we
use *training loss* because fundamental to most training
operations. We could however have used *accuracy* or another other
summary scalar with the same result.

Save you changes to the `Guild` file and restart Guild View by typing
`CTRL-C` in your Guild View terminate (you should see `^C` printed and
the server will terminate):

{% term %}
$ guild view
View server running on port 6333
^C
{% endterm %}

Then simply start Guild View again:

{% term %}
$ guild view
{% endterm %}

When you refresh your browser (or click the **Train** tab to cause the
page to reload) you should now see values for the four fields!

{% insight %}

Guild View can be customized for a project --- everything you see can
be modified. While this tutorial is not focused on building custom
views, you can find more information in
the [Project reference](/project-reference/)
and [Developers guide](/developers-guide/).

{% endinsight %}

## Customize Guild View series

Our next task will be to fix our series references so we can change this:

{% screen guild-view-screen-26.png %}

to this:

{% screen guild-view-screen-27.png %}

We have the same problem with our *series* sources as we did for our
field sources.

In your text editor, open the `Guild` file and scroll to the `series`
sections at the bottom of the file.

The sections we'll modify look like this:

{% code %}
[series "loss"]

source          = series/tf/.+/loss

[series "accuracy"]

source          = series/tf/.+/accuracy
{% endcode %}

Again, these are default values generated by Guild `init` --- we need
to modify them to match the series contained in the Guild logs.

To review, we used the `list-series` command to list the series in the
Guild logs. The keys we're interested in are:

{% code %}
tf/test/accuracy_1
tf/test/cross_entropy_1
tf/train/accuracy_1
tf/train/cross_entropy_1
{% endcode %}

These are the accuracy and loss values for test and train. In our
'source' attributes for the series, we can use a regular expression to
match the series we're interested in. This lets us display multiple
series on a single chart.

Note again that we need to prefix our source values with `series/` to
indicate that they are series.

Modify the two series section in the `Guild` file to be:

{% code %}
[series "loss"]

source          = series/tf/.+/cross_entropy_1

[series "accuracy"]

source          = series/tf/.+/accuracy_1
{% endcode %}

This follows the same convention as renaming the fields to match the
stored series keys --- though in this case we use a regular expression
that matches both train and test sources. These appear as separate
series on the charts.

Save your changes and restart Guild View (again by typing `CTRL-C` and
rerunning `guild view`).

Refresh your browser to see the changes!

## Create custom profile

In this section we'll modify the `Guild` project file to include a
custom `flags` section that can be used as a profile when training.

In your text editor, open the `Guild` file and add the following
section immediately following the `flags` section (i.e. on the line
after `learning_rate = 0.001`):

{% code %}
[flags "high-accuracy"]

max_steps = 5000
{% endcode %}

Save your changes.

Let's preview the command line options that are used for training when
the `high-accuracy` profile is specified:

{% term %}
$ train --preview --profile high-accuracy
{% endterm %}

You should see this command:

{% code %}
  /usr/bin/python \
    -um mnist_with_summaries \
    --data_dir data \
    --log_dir $RUNDIR \
    --max_steps 5000 \
    --dropout 0.9 \
    --learning_rate 0.001
{% endcode %}

Confirm that `max_steps` is 5000 and then use train the model with the profile:

{% term %}
$ guild train --profile high-accuracy
{% endterm %}

While the model trains, click the **Compare** tab in Guild View. You
should see something like this:

{% screen guild-view-screen-28.png %}

You can watch the progress of your training as it progresses --- Guild
updates the field values automatically.

The longer training should improve validation accuracy to around 98%!
You can of course experiment with longer periods to improve accuracy
further.

## Fix overfitting

On the **Compare** page, note that the training accuracy of the last
run is higher than the validation accuracy. This is an indication of
overfitting. Overfitting occurs when the training procedure fits to
variations in the patterns that are specific to the training data and
not part of the underlying features. By reserving images that are not
used for training, we can detect when this occurs.

Overfitting will typically not improve with more training --- so while
our training accuracy may be very high (as high as 100%) our
validation accuracy will remain stuck at a lower plateau.

One of the counter measures to over fitting is *dropout*. Dropout is
the process of temporarily removing neurons during training. For more
information in dropout, see {% link
https://www.cs.toronto.edu/~hinton/absps/JMLRdropout.pdf %} Dropout: A
Simple Way to Prevent Neural Networks from Overfitting {% endlink %}.

In your text editor, open the `Guild` file and the modify the `[flags
"high-accuracy"]` to be:

{% code %}
[flags "high-accuracy"]

max_steps = 5000
dropout = 0.5
{% endcode %}

The `dropout` attribute is the probability of keeping a neuron in the
network. In this case we're specifying that 50% of the neurons (and
their associated connections) should be dropped for each training
iteration!

Save your changes and rerun the training with the "high-accuracy"
profile:

{% term %}
$ guild train --profile high-accuracy
{% endterm %}

As the model trains, you can watch the progress of the two accuracy
rates on the **Train** page. The decreased value for `dropout` should
have a noticeable effect on the two accuracies. If you compare the
**Accuracy** chart to the previous run, you'll see that the validation
accuracy for the more aggressive dropout (value of 0.5) stays roughly
within the training accuracy, whereas the validation for the previous
run (dropout of 0.5) is almost always below the training accuracy.

## Add dropout to compare table

Click on the **Compare** tab and look at the last two runs. The latest
run (the one on top) has lower validation and test accuracies than the
previous run (the one below it). This is of course because we lowered
the `dropout` from 0.9 to 0.5! However there's no indication of this
from looking at the table --- we would need to manually compare the
hyperparameters of the two runs to find the differences.

Because the `dropout` flag plays such an important role in our model's
performance, we'd like to include a **Dropout** column in the compare
table.

We can do this easily by defining a `compare` attribute for the view.

In your text editor, open the `Guild` file and add the following lines
at the end of the `[view]` section:

{% code %}
compare = validation-accuracy train-accuracy dropout-flag steps time
{% endcode %}

Restart Guild View and refresh your browser (or simply click the
**Compare** tab again to reload the page). You should now see the
**Dropout** column with the flag value used for each run!

{% todo %}

The table obviously needs to support ad hoc display of columns ---
having to manually edit the list of compare fields is pretty
terrible. It works in a pinch but we can only live this way so long!

{% endtodo %}

{% insight %}

It might seem magical that by simply adding a value `dropout-flag` to
a list of fields a fully formed field definition appears! There are
several attributes that seem to have come out of nowhere:

- Field label ("Dropout")
- Field source (a particular *flag* value rather than a series source)
- Field formatting

Guild does not hard-code the names of fields. Instead it uses aliases
that correspond to a set of field defaults, which apply automatically
when the alias is used. These defaults can be overridden in named
`field` and `series` sections. This is how we modified the field and
series sources earlier. Sections may also be used to modify labels,
colors, icons, formatting, and a number of other settings.

For more information on customizing fields and series,
see [Fields](/project-reference/#fields-1)
and [Series](/project-reference/#series) in the project reference.

{% endinsight %}

## Export trained model

This section covers the more advanced step of exporting a trained
model for use in Guild Service.

In many cases it's sufficient to build a network architecture, train
the model and public the results if they're compelling or you've
otherwise learning something novel. However, if you need to run your
model in production --- as an inference service for an application for
example --- you need to take a few additional steps.

The `mnist_with_summaries.py` script does not provide support for
exporting models. Fortunately, it's pretty easy to add. There are
three modifications to `mnist_with_summaries.py`:

- Describe the inputs needed by the model to perform inference and the
  outputs generated
- Specify a default value for dropout of 1.0, which will be used when
  the model is not being trained
- Save the model to the run directory

Our first modification will be to store information about the model
inputs and outputs in a collection. Collections are named lists of
string values. Guild, as well as Google Cloud ML, expect the following
collections in exported models:

| `inputs` | JSON map of input names to tensors |
| `outputs` | JSON map of output names to tensors |

Let's modify `mnist_with_summaries.py` to save this information in the
model prior to exporting.

The last two lines of the `train` function are:

{% code python %}
  ...
  train_writer.close()
  test_writer.close()
{% endcode %}

Add the three lines below after those two using the same two space
indentation --- i.e. we're adding more lines to the `train` function,
which will be executed after training completes. The final lines of
the `train` functions should be:

{% code python %}
  ...
  train_writer.close()
  test_writer.close()

  import json
  tf.add_to_collection("inputs", json.dumps({"image": x.name}))
  tf.add_to_collection("outputs", json.dumps({"prediction": y.name}))
{% endcode %}

These lines modify the default graph by adding the required inputs and
outputs collections. The names `image` and `prediction` are arbitrary
--- they are used when reading inputs as JSON (in the case of `image`)
and when generating JSON output (in the case of `prediction`). We'll
see later in this section how these two values are used.

Our next step is to adjust the *dropout* definition in
`mnist_with_summaries.py` to not require a value during inference. The
model is designed to work with both training and validation (aka test
in this case) data and therefore *dropout* is defined as a
placeholder. Placeholders in TensorFlow are required inputs --- if
values are not provided when an operation reads a placeholder tensor,
TensorFlow generates an error. Because we're not feeding dropout
values at inference time, we need to soften this requirement.

TensorFlow provides an alternative definition of placeholder --- one
that accepts a default value, which will be used whenever the
placeholder values is not provided.

In `mnist_with_summaries.py` find the line that looks like this:

{% code python %}
    keep_prob = tf.placeholder(tf.float32)
{% endcode %}

and modify it to be:

{% code python %}
    keep_prob = tf.placeholder_with_default(1.0, [])
{% endcode %}

This specifies that the value 1.0 should be used whenever we don't
provide a value for `keep_prob`. This simply means that we never
perform dropout when running the model for inference!

Out final change is to save the model. Returning to the `train`
function, add the last three lines below to the end of the
function. The final lines of the `train` function should be:

{% code python %}
  ...
  tf.add_to_collection("inputs", json.dumps({"image": x.name}))
  tf.add_to_collection("outputs", json.dumps({"prediction": y.name}))

  saver = tf.train.Saver()
  tf.gfile.MakeDirs(FLAGS.log_dir + "/model")
  saver.save(sess, FLAGS.log_dir + "/model/export")
{% endcode %}

That's it! We're ready to retrain our model, which will export the
trained version.

Let's run a trial first using a small number of steps to verify that
our new code is working:

{% term %}
$ guild train --flag max_steps=10
{% endterm %}

In this command we've used Guild's `--flag` option (we also could have
used the short version `-F`) to set `max_steps` to 10. If the code
change was successful, the operation will finish in a few seconds
without any errors.

Let's now train the model using the full number of steps:

{% term %}
$ guild train
{% endterm %}

Of course if you want to use the "high accuracy" profile as we did
earlier, by all means!

## Run model in Guild View

In Guild View click the **Serve** tab. Select the latest run from the
dropdown. If your model was exported as expected, you should see this:

{% screen guild-view-screen-28.png %}

In particular, **Input Values** and **Output Values** should contain
information about the model's inputs and outputs. This is the
information from the collections we created just before saving the
model.

If the model wasn't successfully exported --- or you happen to be
viewing an old run --- you'll see this message in **Input Values**:

{% screen guild-view-screen-29.png %}

If you get this message, return to the previous section and confirm
that you're modifying `mnist_with_summaries.py` as described.

When Guild recognizes your exported model you can run it by submitting
JSON inputs. We've provided a sample MNIST image encoded as JSON here:

{% code %}
[{"image": [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.02352941408753395, 0.7490196228027344, 0.5176470875740051, 0.5882353186607361, 0.9921569228172302, 0.7568628191947937, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.4745098352432251, 0.9921569228172302, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.7058823704719543, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.4745098352432251, 0.9921569228172302, 0.988235354423523, 0.988235354423523, 0.8705883026123047, 0.10588236153125763, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5607843399047852, 0.9921569228172302, 0.988235354423523, 0.960784375667572, 0.4235294461250305, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.41960787773132324, 0.9764706492424011, 0.9921569228172302, 0.9647059440612793, 0.41960787773132324, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1098039299249649, 0.9254902601242065, 0.988235354423523, 0.9803922176361084, 0.41568630933761597, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.11372549831867218, 0.8078432083129883, 0.988235354423523, 0.988235354423523, 0.7882353663444519, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5098039507865906, 0.988235354423523, 0.988235354423523, 0.8078432083129883, 0.06666667014360428, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.46274513006210327, 0.9725490808486938, 0.988235354423523, 0.988235354423523, 0.3137255012989044, 0.0, 0.04313725605607033, 0.49803924560546875, 0.8980392813682556, 0.7843137979507446, 0.4274510145187378, 0.08235294371843338, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.22745099663734436, 0.9803922176361084, 0.988235354423523, 0.988235354423523, 0.35686275362968445, 0.003921568859368563, 0.501960813999176, 0.9490196704864502, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.6352941393852234, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.10196079313755035, 0.8588235974311829, 0.9921569228172302, 0.9764706492424011, 0.7647059559822083, 0.06666667014360428, 0.7254902124404907, 1.0, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.1411764770746231, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.2352941334247589, 0.988235354423523, 0.988235354423523, 0.6549019813537598, 0.0, 0.6980392336845398, 0.988235354423523, 0.9921569228172302, 0.988235354423523, 0.8627451658248901, 0.4235294461250305, 0.5568627715110779, 0.988235354423523, 0.988235354423523, 0.4117647409439087, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6627451181411743, 0.988235354423523, 0.988235354423523, 0.364705890417099, 0.30588236451148987, 0.9529412388801575, 0.988235354423523, 0.9647059440612793, 0.3764706254005432, 0.14509804546833038, 0.027450982481241226, 0.5686274766921997, 0.988235354423523, 0.988235354423523, 0.3176470696926117, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.027450982481241226, 0.7254902124404907, 0.988235354423523, 0.8000000715255737, 0.2862745225429535, 0.9450981020927429, 0.988235354423523, 0.988235354423523, 0.5372549295425415, 0.0, 0.12156863510608673, 0.5686274766921997, 0.988235354423523, 0.988235354423523, 0.7647059559822083, 0.0470588281750679, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.14509804546833038, 0.988235354423523, 0.988235354423523, 0.545098066329956, 0.4901961088180542, 0.988235354423523, 0.988235354423523, 0.760784387588501, 0.2392157018184662, 0.2823529541492462, 0.8823530077934265, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.545098066329956, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.14509804546833038, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.9921569228172302, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.6352941393852234, 0.0470588281750679, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.37254902720451355, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.9921569228172302, 0.988235354423523, 0.988235354423523, 0.9254902601242065, 0.545098066329956, 0.0470588281750679, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.09019608050584793, 0.8627451658248901, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.9921569228172302, 0.9529412388801575, 0.6078431606292725, 0.2705882489681244, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.15294118225574493, 0.6627451181411743, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.988235354423523, 0.7843137979507446, 0.5411764979362488, 0.08235294371843338, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.125490203499794, 0.8784314393997192, 0.988235354423523, 0.8078432083129883, 0.3137255012989044, 0.02352941408753395, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0], "label": 6}]
{% endcode %}

Copy the JSON above and paste it without modification into the
**Input** field. Click **Run model**.

The **Output** field should contain formatted JSON that is similar to this:

{% code json %}
[
    {
        "prediction": [
            -1.7864482402801514,
            -4.660470485687256,
            1.341475009918213,
            -3.295342445373535,
            -0.08682294189929962,
            -0.8310251832008362,
            10.286107063293457,
            -7.066933631896973,
            -1.5645456314086914,
            -4.694084167480469
        ]
    }
]
{% endcode %}

The array of numbers corresponds to the prediction score for each
respective digit. The highest value in the array indicates the
predicted digit. In this case the highest value is in the position
corresponding to the number 6, which is correct!

{% note %}

The Guild {% ref mnist-example %} provides a
`samples` [resource](/project-reference/#resources) that can be used
to generate encoded JSON MNIST images in the same format expected by
the model now exported by `mnist_with_examples.py`. Feel free to
experiment with different JSON inputs, using the generated samples.

{% endnote %}

Now that we've seen the model work in Guild View --- let's move to the
final section of the tutorial to see how we can run the model as a
server.

## Run model in Guild Serve

As our last section of this tutorial, we'll run the model as a
standalone HTTP server.

In the project directory, run:

{% term %}
$ guild serve --latest-run
{% endterm %}

This will start Guild Serve on port 6444 and load your most recently
trained model.

Refer to [Guild Serve](/user-guide/guild-serve/) for details on the
HTTP based API implemented by server.

{% include test-serve-with-curl.md %}

## Summary

In this tutorial we added Guild support to {% link
https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/tutorials/mnist
%}TensorFlow's MNIST example{% endlink %}. You can follow the same
steps to integrate Guild into your own project.

Guild is designed to minimize changes your TensorFlow code. Guild does
not provide Python libraries or other frameworks that you need to use
in model development, training, or serving. Instead, Guild uses
patterns and conventions that are easy to implement in your TensorFlow
projects.

**Integration points**

- Guild uses project *flags* to define sets of parameters that are
  passed to your TensorFlow scripts as command line options.

- Guild executes your scripts as *operation system processes* without
  introducing additional software libraries, frameworks, or runtime
  environments that could change their behavior.

- Project operations (*prepare*, *train*, *evaluate*, etc.) are
  configured using a single `Guild` project source file, which is
  human readable and editable.

- The primary integration point between Guild and your project is the
  use of the *run directory* (i.e. `RUNDIR`) for writing logs and
  exporting models.

- Guild automatically reads standard TensorFlow event logs for a run
  as long as the logs are written within the run directory hierarchy.

- Guild View requires correct *source* values to display TensorFlow
  scalar information, either as fields or time series charts.

**Benefits to integrating Guild**

While integrating Guild requires some work, there are a number of
benefits to enabling Guild for your projects.

- Others can reliably use your models by running a single command
  without knowing the details of the project. This is similar to the
  benefits provided by adding a `Makefile` (or similar tool
  configuration) to your project.

- Each experiment is automatically tracked by Guild in its own run
  directory. The run directory includes your TensorFlow logs (which
  your script writes) but also a record of:

  - Flags used in the operation
  - System attributes and time series stats such as CPU and GPU
    information
  - Output generated by your scripts during the operation
  - Copies of the `Guild` file and project sources for comparison
  - Exported models
  <p></p>

- Guild View can be used to stud and compare experiments.

- Guild Serve can be used to run trained models (provided they're
  exported) as web services

- Guild will continue to be enhanced with new capabilities
  (e.g. experiment publishing and importing) that the project will
  benefit from.
