---
layout: docs
title: Troubleshooting
title_class: hidden
description: Overview of troubleshooting Guild issues
group: troubleshooting
---

If you have an issue that is not listed below, submit an issue to {%
ref github-issues %}.

## Contents

* Will be replaced with the ToC
{:toc}

## Setup

### Unable to compile psutil

If you are compiling psutil for use with Guild, you need the
`python-dev` package for your system.

This problem manifests with compile errors like this:

{% term %}
build/temp.linux-x86_64-2.7/psutil/_psutil_linux.o
   psutil/_psutil_linux.c:12:20: fatal error: Python.h: No such file or directory
    #include <Python.h>
                        ^
   compilation terminated.
   error: command 'x86_64-linux-gnu-gcc' failed with exit status 1
{% endterm %}

## Training

### RUNDIR deleted by training script

Many of the TensorFlow examples recursively delete the log directory
before recreating it at the same location. This is problematic when
the the deleted directory is the Guild run directory (i.e. the
`$RUNDIR` reference used for flags or the `RUNDIR` environment
variable) because Guild writes several files to this location *before*
running the training script.

When the run directory is deleted by the script, Guild will print this
error message during training every time it tries to write to its logs:

{% term %}
Error logging series values: read_only (was RUNDIR deleted?)
{% endterm %}

You can fix this problem by removing this code pattern from your
scripts:

{% code python %}
if tf.gfile.Exists(rundir):
  tf.gfile.DeleteRecursively(rundir)
{% endcode %}

where `rundir` is a reference to the run directory
(e.g. `FLAGS.log_dir` in many TensorFlow examples).

{% note %}

Modifying the behavior of your script this way not likely cause
problems with TensorFlow's logging as TF event files are named with
timestamps. Additionally, Guild creates a new run directory for every
train operation, eliminating the need to clear the directory that way.

{% endnote %}
