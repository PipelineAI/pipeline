---
layout: docs
title: Introduction
description: Getting started with Guild AI
group: getting-started
---

This is a short overview of Guild and its features. If you're ready to
install Guild, go to [Setup](/getting-started/setup).

Guild AI is a set of tools to enhance TensorFlow&trade;
development. Start by adding a simple `Guild` file to the root of your
project:

{% code %}
[project]

name      = My Deep Learning Project

[model]

train     = train

[flags]

data_dir  = data
train_dir = $RUNDIR
{% endcode %}

Guild uses this information to execute the script `train.py` when you
run this command:

{% term %}
$ guild train
{% endterm %}

Just before executing your script, Guild creates a unique run
directory that contains training logs, checkpoints, exported models,
and additional data that Guild collects. This directory can be
referenced using the `RUNDIR` environment variable, or provided as a
command line option to your script.

Here's what Guild provides:

- Each training operation is tracked in a separate directory, managed
  by Guild
- Guild logs your script's output and results, along with any
  TensorFlow event logs
- Guild also logs system stats such as CPU and GPU utilization

You can view your training operation in progress by running the `view`
command in a separate terminal:

{% term %}
$ guild view
{% endterm %}

Because each model training is unique, Guild lets you customize your
view for each model. Here's an example that shows the MNIST training
in progress --- it reads the validation and training accuracy from the
TensorFlow event logs:

{% screen view-4.jpg %}

When it's time to run your model as a part of your application, use
the `serve` command:

{% term %}
$ guild serve --latest-run
{% endterm %}

This runs your model in an light weight HTTP server, creating a web
service for making predictions. You can even use `curl` (or any HTTP
client) to test your model:

{% term %}
$ curl -sd @my-request.json localhost:6444/run
{% endterm %}

That's Guild AI in a nutshell! Use it to augment your TensorFlow
development to train, evaluate, and finally run your
models.  [Read more about Guild features](/features/) or
click below to get started using Guild on your system.

{% next /getting-started/setup/ %}Next: Setup Guild on you system{% endnext %}
