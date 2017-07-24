---
layout: docs
title: Quick start
description: A short tutorial to introduce Guild AI's basic features.
group: getting-started
---

After [installing Guild](/getting-started/setup/), follow these steps
to quickly become familiar with its features.

## Contents

* Will be replaced with the ToC
{:toc}

## Get Guild examples

If you haven't already cloned the Guild examples, run:

{% term %}
$ git clone https://github.com/guildai/guild-examples.git
{% endterm %}

All of the commands below assume you're running in the MNIST
example. Change to that directory now:

{% term %}
$ cd guild-examples/mnist
{% endterm %}

## Prepare to train MNIST

Most projects require data that's download once and reused across
multiple training operations. For this reason Guild supports a
`prepare` operation that can be used to perform one-time setup tasks
in preparation for subsequent trainings.

The MNIST project uses this convention. If you try to train a model
without first running `prepare` you get this error:

{% term %}
$ guild train
guild train: missing required './data'
Do you need to run 'guild prepare' first?
{% endterm %}

Run `prepare` to download the MNIST data:

{% term %}
$ guild prepare
{% endterm %}

{% insight %}

Your TensorFlow scripts perform the work when Guild runs an operation
--- Guild structures the operations. Here's a snippet from the MNIST
Guild project file that tells Guild how to prepare, train, and
evaluate the "intro" model:

{% code %}
[model "intro"]

train           = intro
prepare         = intro --prepare
train_requires  = ./data
evaluate        = intro --test
{% endcode %}

When you run `guild prepare`, this is what Guild runs in the
background:

{% term %}
/usr/bin/python \
    -um intro \
    --prepare \
    --datadir ./data \
    --rundir $RUNDIR \
    --batch-size 100 \
    --epochs 5
{% endterm %}

You can see what Guild is going to run running an operation with the
`--preview` option. For example:

{% term %}
$ guild prepare --preview
{% endterm %}
{% endinsight %}

## Train MNIST intro

Now that the MNIST project is prepared (i.e. the MNIST image data has
been downloaded) we can train the model:

{% term %}
$ guild train
{% endterm %}

The model we just trained is simple and will train in a matter of
seconds on most systems.

The MNIST project defines two models --- an *intro* and an *expert*
model. You can list the models defined for a project using the
`list-models` command:

{% term %}
$ guild list-models
intro
expert
{% endterm %}

{% insight %}

The `guild` program provides a number of query commands that print
information about a project (e.g. `list-models`). You may also simply
view the `Guild` project file, which is located in the project root
directory. The `Guild` file is a human readable text file that
describes all of Guild's interactions with the project. For more
information see [Project reference](/project-reference/guild-project-file/).

{% endinsight %}

We'll train the *expert* mode in a moment, but first let's use Guild view
to visualize the results of our first training.

## View training results

Open another terminal and change to the MNIST directory --- then run
the `view` command:

{% term %}
$ cd guild-examples/mnist
$ guild view
{% endterm %}

We run Guild view in a separate terminal because it runs alongside the
other Guild operations.

Open {% link http://localhost:6333 %}http://localhost:6333{% endlink
%} in your browser. You should see the results of the MNIST "intro"
model training.

Let's take a moment to understand the information Guild View provides
for the MNIST project.

<div class="bd-screen callout-container">

  <figure class="figure">
    <img class="figure-img screenshot border" src="/assets/img/view-5.jpg">
  </figure>

  <div class="popover popover-callout popover-bottom" data-top="0.06" data-left="0.4">
    Navigation bar
  </div>

  <div class="popover popover-callout popover-right" data-top="0.075" data-left="0.26">
    Active run
  </div>

  <div class="popover popover-callout popover-top" data-top="0.1" data-left="0.56">
    Current field values
  </div>

  <div class="popover popover-callout popover-left" data-top="0.067" data-left="0.795">
    Run status
  </div>

  <div class="popover popover-callout popover-bottom" data-top="0.31" data-left="0.86">
    Flags used for<br>train command
  </div>

  <div class="popover popover-callout popover-bottom" data-top="0.68" data-left="0.8">
    System attributes
  </div>

  <div class="popover popover-callout popover-bottom" data-top="0.5" data-left="0.27">
    Series charts
  </div>

  <div class="popover popover-callout popover-right" data-top="0.7" data-left="0.35">
    Run output with<br>timestamps
  </div>
</div>

Select any of the project runs using the **Active run** dropdown at
the top of the page.

The **Overview** page displays the following details for the active
run:

- **Field** values
- **Flags** and **Attributes** associated with the run
- **Series** charts
- **Output** generated during the run

{% insight %}

Every training operation is tracked at this level of detail when you
run `guild train`.

{% endinsight %}

## Evaluate your trained model

Looking at the results from Guild View we can see the validation
accuracy is around 92%. This is what we'd expect from this model ---
it's the logistic regression used as TensorFlow's introduction.

This value is generated by measuring the model's predictive
performance on *validation data* --- i.e. data that the trained model
has not seen, but that we might use to make improvements to the model.

If we want to measure the final performance of the model, we need to
evaluate it using *test data* --- i.e. data that neither the trained
model has seen nor have we used to modify the model.

Our MNIST project supports Guild's `evaluate` operation, which uses
test data. To evaluate the intro model run:

{% term %}
$ guild evaluate --latest-run
{% endterm %}

We need to specify the run for the model we're evaluating. We use
`--latest-run` to indicate we're evaluating the model generated during
the latest run.

The test accuracy should be roughly equal to our validation accuracy
--- i.e. approximately 92%. If the test accuracy was substantially
lower we might conclude that our model doesn't generalize well.

## Train MNIST expert

Our MNIST project supports two different models: an *intro* and an
*expert* model. These are both taken directly from TensorFlow's MNIST
examples (the famous red and blue pills --- of course we're taking
*both* in our getting started guide!)

Let's train the expert model by running:

{% term %}
$ guild train expert
{% endterm %}

Note that in this case we are specifying the `expert` model --- as we
saw earlier, the `intro` model is used by default if not otherwise
specified.

The expert model is a convolutional neural network and so takes much
longer to train. If your system has a GPU you'll be able to train the
expert model over 10 epochs in a few minutes. If you're training on a
CPU, the expert model will take 20 times longer, or more!

If training is progressing slowly, terminate the training by pressing
**CTRL-C** and try again with a smaller number of epochs:

{% term %}
$ guild train expert --flag epochs=1
{% endterm %}

This modification to the training command sets an `epochs` flag, which
is passed to the training script. This is not a realistic training
scenario as the model will not have seen enough data to learn
properly, but it will serve our purposes.

There's no need to wait for the expert model to finish training --- we
can compare our runs at any time, even when one is in progress.

## Compare results

In your browser, click the **Compare** tab in Guild View. You'll see
the results from the two runs (or three if you terminated the expert
run early).

<div class="bd-screen callout-container">

  <figure class="figure">
    <img class="figure-img screenshot border" src="/assets/img/view-6.jpg">
  </figure>

  <div class="popover popover-callout popover-bottom" data-top="0.14z" data-left="0.46">
    Navigation bar
  </div>

  <div class="popover popover-callout popover-right" data-top="0.18" data-left="0.15">
    Project title
  </div>

  <div class="popover popover-callout popover-top" data-top="0.35" data-left="0.4">
    Results across multiple runs
  </div>

  <div class="popover popover-callout popover-right" data-top="0.88" data-left="0.07">
    Add components to compare run details
  </div>
</div>

This page lets you sort and filter results to gain insight from your
experiments and ultimately to select the optimal model for your
application.

{% insight %}

If you were able to train *expert* for more than a few epochs, you
should see a dramatic improvement in accuracy over *intro*. The expert
model uses a state of the art convolutional neural network while the
intro model uses a simple logistic regression. While *expert* is
considerably more accuracy, it takes far longer to train. Such trade
offs are common when evaluating models --- performance should be
measured across various axis, not merely accuracy or error rates.

{% endinsight %}

## Summary

By now you're off and running using Guild! Here's what we covered in
this quick start guide:

- Downloaded Guild examples
- Prepare and train the *intro* and *expert* MNIST models
- Use Guild View to visualize training operations and compare results
- Evaluate models by calculating their test accuracies
- Perform ad hoc inference on both models with sample image data

## Next steps

{% next /features/ %}Read more about Guild features{% endnext %}
{% next /examples/ %}Browse Guild examples{% endnext %}
{% next /tutorials/integrating-guild-with-your-project/ %}Integrate Guild with your project{% endnext %}
{% next /tutorials/using-guild-to-serve-models/ %}Run the expert MNIST model as a web service{% endnext %}
