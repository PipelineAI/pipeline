---
layout: docs
title: Guild AI feature overview
description: Overview of the Guild AI features
group: user-guide
---

Guild AI augments your TensorFlow development with a set of command
line based utilities.

<div class="row mt-4">
  <div class="col-10 offset-1 col-lg-11 offset-lg-1">
    <div class="row">
      <div class="col-8 offset-2 col-lg-3 offset-lg-0 flow-step"><a href="#train"><i class="fa fa-retweet"></i> Train</a></div>
      <div class="col-8 offset-2 col-lg-1 offset-lg-0 flow-arrow"></div>
      <div class="col-8 offset-2 col-lg-3 offset-lg-0 flow-step"><a href="#evaluate"><i class="fa fa-table"></i> Evaluate</a></div>
      <div class="col-8 offset-2 col-lg-1 offset-lg-0 flow-arrow"></div>
      <div class="col-8 offset-2 col-lg-3 offset-lg-0 flow-step"><a href="#serve"><i class="fa fa-cloud-upload"></i> Serve</a></div>
    </div>
  </div>
</div>

## Train

Guild AI lets you train your model by running a simple command:

{% term %}
$ guild train
{% endterm %}

All of the complexity of training your model is encapsulated in a
`Guild` file, located in your project directory. For example, this
sample project file tells Guild to use the `train_mnist.py` script
(Python module) when training the *mnist* model -- it also defines
several *flags*, including hyperparameters, that are provided to the
training script as command line arguments:

{% code %}
[model "mnist"]

train           = train_mnist

[flags "mnist"]

rundir          = $RUNDIR
datadir         = ./data
epochs          = 1000
learning_rate   = 0.01
{% endcode %}

Think of the `Guild` project file as a `Makefile` for your deep
learning projects.

When Guild trains a model, it creates a unique *run directory* that
your script can use to log events, save training checkpoints, and
export models. Guild also collects system metrics as your model
trains, consolidating them along with your TensorFlow summaries
(e.g. training loss, accuracy, etc.) to give you a more complete view
of your model performance.

**Benefits of using Guild AI to train**

* Track each run result as a separate experiment, letting you review
  and compare historical results at any time
* Capture additional statistics along with your TensorFlow summaries
  to better understand your model's performance characteristics
* Simplify the training operation to a single command
* Formalize default script inputs, including hyperparameters, as a
  part of your project source

{% next /user-guide/model-operations/ %}More about Guild operations{% endnext %}

## Evaluate

Use Guild `view` to start a web based application for viewing your
training results. Guild supports real time updates as your model
trains to give you early feedback on results.

Simply start Guild view:

{% term %}
$ guild view
{% endterm %}

and open [http://localhost:6333](http://localhost:6333) in your
browser.

Guild provides three views to your models:

* Train
* Compare
* Serve

**Train** is a dashboard for a single training run. Use it to view a
training in progress or prior experiments. Guild
supports [simple view definitions](/project-reference/views/).

**Compare** lets you evaluate all of your training results in once
place. Use this view to find the model that performs the best.

**Serve** lets you perform ad hoc predictions using a trained model
and capture inference performance stats such as run times, predictions
per second, and memory consumed.

**Benefits of using Guild View**

* Create a dashboard for your model that you and others can use to
  view training results
* Compare historical results at a glance to select the best model
* Run your model to test input and output
* Capture inference statistics

{% next /user-guide/guild-view/ %}More about Guild View{% endnext %}

## Serve

Guild provides a light weight HTTP server that can be used to run your
trained models. Guild uses a simple JSON API that's compatible with
Google's Cloud ML for predictions.

To serve your latest trained model over HTTP, run this command from
the project directory:

{% term %}
$ guild serve --latest-run
{% endterm %}

This will start a server on port 6444 that will accept HTTP `POST`
requests with JSON inputs to your model and return the JSON
outputs. In this way you can quickly create a web service for your
model.

{% note %}
Guild Serve is currently stable for local development and testing but
is not yet optimized for production use.
{% endnote %}

{% next /user-guide/guild-serve/ %}More about Guild Serve{% endnext %}

## Next Steps

{% next /user-guide/projects/ %}Read about Guild projects{% endnext %}
