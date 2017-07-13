---
layout: docs
title: Guild View
description: Guide for using Guild view
group: user-guide
---

Guild View is a web application used to visualize and interact with a
Guild project.

## Contents

* Will be replaced with the ToC
{:toc}


## Starting Guild View

To start Guild View, change to your project directory and run the
`view` command:

{% term %}
$ guild view
{% endterm %}

Next, open your browser to:

<div class="m-3">
<a href="http://localhost:6333" target="_blank">http://localhost:6333</a>
</div>

To run Guild View on a different port, use the `--port` option. For
example, to use port 8888, run the command:

{% term %}
$ guild view --port 8888
{% endterm %}

For more information, refer to the `view` {% ref cmd:view %}.

Guild View provides a default view for your project that is configured
by the {% ref guild-project-file %}.

The default view provides three pages:

* Train
* Compare
* Serve

## Train page

The train page displays a summary of a selected run. Runs are training
operations, either completed or in progress. Guild View updates
in-progress runs in real time --- you don't need to refresh the view.

The train page provides a variety of information about a run:

- Run selector
- Run status
- Field values (single value summaries)
- Flags used for the run
- System attributes at the time of the run
- Time series
- Run output

### Run selector

The run selector is used to select the current run. A *run* is a model
training operation and contains a database of flags, time series,
system attributes, and training output. The **Train** page displays
the current values for the selected run.

{% screen view-13.jpg %}

### Run status

The run status displays the status of the run. It's updated
automatically when in the *Running* state.

{% screen view-14.jpg %}

Status values may be one of:

| **Running**    | Run is in progress |
| **Completed**  | Run completed without errors |
| **Terminated** | Run was terminated early by the user |
| **Error**      | Run exited with an error (refer to Output for specific error messages) |

### Field values

Fields display single value summaries for the run. Fields are defined
in the {% ref guild-project-file %}. For details on defining fields
for your model,
see [Fields](/project-reference/fields/).

Field values are updated dynamically every 5 seconds. You may modify
this interval using the `--interval` option when running `guild view`.

### Flags

Flags are parameters that are passed to your training scripts for a
run. Guild captures these because they often play an important role in
the training and are useful for comparing results.

{% screen view-15.jpg %}

For more information on flags and how they're used in operations,
see [Flags](/project-reference/flags/).

### Attributes

Guild captures and logs various system attributes for each
run. Attributes include GPU and CPU characteristics that can effect
training performance and can be helpful when comparing runs across
different system configurations.

{% screen view-16.jpg %}

### Time series

Guild logs time series scalar values are generated during a train
operation including scalar summaries written to TensorFlow event
logs. Guild also captures system metrics as time series.

{% screen view-17.jpg %}

Time series chart widgets are used to display these values. You must
specify the series to display for a model in the Guild project
file. For more information about specifying series for a model,
see [Series](/project-reference/series/).

### Output

Log captures all output generated during a training operation. Output
is displayed at the bottom of the default view.

{% screen view-18.jpg %}

To search for text occurring in the output, use the **Filter** field.

## Compare page

The compare page displays a table of project runs with various fields
that can be helpful in assessing relative changes in training performance.

{% screen view-19.jpg %}

Compare fields must be specified in the Guild project file. For detail
on specifying compare fields, see the reference for
the [view compare attribute](/project-reference/views/#compare).

To filter the table content to runs containing specific text or number
values, use the **Filter** field.

Runs may be sorted according to field values, either in ascending or
descending order. For example, this is useful for finding runs with
the highest validation accuracy.

Compare components can be added by clicking the blue button at the
bottom of the page and selecting something to compare:

{% screen view-20.jpg %}

To compare runs, select two or more runs.

## TensorBoard page

Guild seamlessly integrates TensorBoard into View. TensorBoard is run
behind the scenes and serves data from the project directory (via the
`--logdir` option).

For details on TensorBoard, see {% link
https://www.tensorflow.org/get_started/summaries_and_tensorboard
%}TensorBoard: Visualizing Learning{% endlink %}.

## Next Steps

{% next /user-guide/guild-serve/ %}Read about Guild Serve{% endnext %}
