# Guild AI

Guild AI supplements
your [TensorFlow&trade;](https://www.tensorflow.org/) operations by
collecting a wide range of information about your model's performance,
including GPU usage, CPU usage, memory consumption and disk IO. You
can view all of this information along with your TensorFlow summary
output in realtime using Guild AI view.

Guild is used to measure model performance when running on specific
systems. The data you collect can be used to optimize your model for
specific applications. For example, you can collect metrics such as:

- GPU memory usage for batch training, single inference, batch inference
- Inference latency and throughput
- Impact of hyper parameter tuning on model accuracy and training time

When your model is trained you can run it using
Guild's [Google Cloud Machine Learning](https://cloud.google.com/ml/)
compatible inference server. This can be used as a local dev/test
environment in preparation for cloud deployment, or be run in
production within your own environment.

## Project status

Guild is in a pre-release "alpha" state. All command interfaces,
programming interfaces, and data structures may be changed without
prior notice. We'll do our best to communicate potentially disruptive
changes.

## Build dependencies

Guild requires the following software for compilation:

- make (available via Linux system package or Command Line Tools via Xcode on OSX)
- [Erlang](http://www.erlang.org/) (18 or later)

## Runtime dependencies

Guild requires the following software for runtime (i.e. performing
model related operations prepare, train, and evaluate).

- [Python](https://www.python.org/) (2.7 recommended)
- [TensorFlow](https://www.tensorflow.org/versions/r0.11/get_started/os_setup.html#download-and-setup)
- [NVIDIA System Management Interface](https://developer.nvidia.com/cuda-downloads) (optional, for GPU stats)
- [psutil](https://github.com/giampaolo/psutil)

## Compiling Guild

Before building Guild, confirm that you have the required build
dependencies installed (see above).

Clone the Guild repository:

    $ git clone git@github.com:guildai/guild.git

Change to the Guild directory and run make:

    $ cd guild
    $ make

Please report any compile errors to
the
[Guild issues list on GitHub](https://github.com/guildai/guild/issues).

Create a symlink named `guild` to `guild/scripts/guild-dev` that's in
your `PATH` environment. The most convenient location would be
`/usr/local/bin` (requires root access):

    $ sudo ln -s GUILD_REPO/scripts/guild-dev /usr/local/bin/guild

where `GUILD_REPO` is the local Guild repo you cloned above.

Alternatively, create a symlink in a directory in your home directory
(e.g. `~/Bin`) and include that directory in your `PATH` environment
variable.

    $ sudo ln -s GUILD_REPO/scripts/guild-dev ~/Bin/guild

Future releases of Guild will provide precompiled packages for Linux
and OSX to simplify the process of installing Guild.

Verify that Guild is available by running:

    $ guild --help

If you get an error message, verify that you've completed the steps
above. If you can't resolve the issue,
please [open an issue](https://github.com/guildai/guild/issues).

## Using Guild

The easiest way to start using Guild is to run some of the
examples. Clone the example repository:

    $ git clone git@github.com:guildai/guild-examples.git

Change to the MNIST example and train the intro model. This model
downloads MNIST images and so requires an initial `prepare` operation
before any of the models can be trained.

    $ cd guild-examples/mnist
    $ guild prepare

This operation will take some time to download the MNIST images. When
it finished, train the intro model:

    $ guild train intro

The intro example corresponds to
TensorFlow's
[MNIST for ML Beginners](https://www.tensorflow.org/versions/r0.11/tutorials/mnist/beginners/index.html). It's
a very simple model and should train in a few seconds even on a CPU.

Next, run Guild View from the same directory:

    $ guild view

Open [http://localhost:6333](http://localhost:6333) to view the
training result. You should see the results of the intro training,
including the model validation accuracy, training accuracy, steps, and
time. The view also includes time series charts that plot training
loss, accuracy, and CPU/GPU information during the operation. Note
the training may not have run long enough in this simple case to
collect system stats.

Next, train the expert version of MNIST. You can keep running View
during any Guild operation -- in that case, open another terminal,
change to `guild-examples/mnist` and run:

    $ guild train expert

This model correspond to
TensorFlow's
[Deep MNIST for Experts](https://www.tensorflow.org/versions/r0.11/tutorials/mnist/pros/index.html) example. As
it trains a multi-layer convolutional neural network it takes longer
to train.

You can view the training progress in real time in Guild View --
select the latest training operation from the dropdown selector in the
top left of the View page.

You can compare the performance of multiple runs in Guild View by
clicking the *Compare* tab. When the expert model finishes training,
you can compare its validation accuracy to the intro model -- it's
significantly more accurate, at the cost of a longer and more
computationally expensive training run.

You can train either model using more epochs (rounds of training using
the entire MNIST training set) -- this will improve validation
accuracy up to a point:

    $ guild train expert -F epochs=5

The `-F` sets a model *flag* that is used by the operation. In this
case we're asking the model to train over 5 epochs. You should see a
slight improvement in validation accuracy -- again, at the cost of
more training.

Finally, evaluate the model performance using the MNIST test data:

    $ guild evaluate --latest-run

This will evaluate the model trained on the latest and print the test
accuracy.

For background on why *test* is different from *validation*,
see
[this section](https://www.tensorflow.org/versions/r0.9/how_tos/image_retraining/index.html#training-validation-and-testing-sets) in
TensorFlow's documentation on network retraining.

## Next Steps

Documentation for Guild is in process but not yet available. While
lacking in detail, you may benefit from:

- Reading Guild examples source code
- Using `guild --help` and `guild COMMAND --help`
- Guild-enable an existing project by running `guild init` and editing
  the generated `Guild` project file
