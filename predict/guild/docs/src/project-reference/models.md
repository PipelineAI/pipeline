---
layout: docs
title: Models
description: Guild project models
group: project-reference
---

## Contents

* Will be replaced with the ToC
{:toc}

A project may contain one or more `model` sections. If a project
contains more than one model section, subsequent sections must be
named, though it's good practice to name each of the sections for
clarity.

The following Guild operations apply to models:

- [prepare](/user-guide/model-operations/#prepare)
- [train](/user-guide/model-operations/#train)
- [evaluate](/user-guide/model-operations/#evaluate)

If a model is not named explicitly in an operation, Guild assumes the
operation applies to the first model defined in the project.

The primary operation applied to models is `train`. In some cases a
model may define a `train_requires` attribute, which specifies one or
more files that must exist before the model can be trained. Required
files are typically created during a `prepare` operation if they don't
already exist.

## Command specs

Attributes that define operations (e.g. *prepare*, *train*,
*evaluate*) do so using *command specs*.

Guild expects command specs to consist of a Python
module with zero or more command options. For example, to execute a
locally defined `train.py` module with the arguments `--datadir
./data`, the command spec would be:

{% code %}
train --datadir ./data
{% endcode %}

{% note %}
Most commands are single module names without options. Options are
typically provided via flags, [described below](#flags).
{% endnote %}

## Model attributes

Model attributes are typically defined in this order:

| [description](#description) | optional, used for documentation only |
| [prepare](#prepare) | optional, if model supports a prepare operation |
| [train](#train)     | typically defined |
| [train_requires](#train_requires) | optional, used as a check before training, typically used with prepare |
| [evaluate](#evaluate) | optional, if model supports an evaluate operation |

Refer to the applicable section below for details.

### description

Guild AI currently does not use this attribute, but it is useful
document a model with a short description, particularly if a project
has more than one model.

{% note %}

Future releases of Guild AI will make use of this attribute.

{% endnote %}

### prepare

The `prepare` attribute is a command spec used to prepare a model for
training. Prepare operations are typically run once, whereas train
operations are run repeatedly.

Prepare is typically used to download and pre-process data used in
training and evaluating a model.

### train

The `train` attribute is a command spec used to train a model. While
`train` is optional, most models will provide a value.

### train_requires

`train_requires` is a UNIX style file pattern that is used to check
for files prior to training. This is a safe guard to prevent train
operations on unprepared models.

For example, if `prepare` is not first run on the {% ref mnist-example
%} the user will see this message when she runs `train`:

{% term %}
$ guild train
guild train: missing required './data'
Do you need to run 'guild prepare' first?
{% endterm %}

Here's the model configuration for this example:

{% code %}
[model]

prepare         = intro --prepare
train           = intro
train_requires  = ./data
{% endcode %}

### evaluate

The `evaluate` attribute is a command spec used to evaluate a trained
model. Because evaluating a model requires a run it's common to
include the `$RUNDIR` argument to the command. This may be provided
as a [flag](#flags) or specified directly in the command spec.

{% next /project-reference/flags/ %}Next: Project flags{% endnext %}
