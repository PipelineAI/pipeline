---
layout: docs
title: Guild Serve
description: Guide for using Guild serve
group: user-guide
---

Guild Serve is a light weight HTTP server that hosts your trained
TensorFlow model as web service.

## Contents

* Will be replaced with the ToC
{:toc}

## Starting Guild Serve

To serve a trained model run:

{% term %}
$ guild serve RUN
{% endterm %}

`RUN` may be:

- The path to a run directory
- The command line flag `--latest-run` to indicate the latest run
  should be used

For more information, refer to the `serve` {% ref cmd:serve %}.

The specified run must contain an exported model. Exported models must
conform to the Google Cloud Machine Learning specification outlined in
*{% link https://cloud.google.com/ml/docs/how-tos/preparing-models
%}Preparing Your TensorFlow Application for Training in the Cloud{%
endlink %}*.

{% insight %}

Each of the [Guild examples](/examples/) exports a trained model ---
refer to any of these for working code to use for your own models.

{% endinsight %}

## REST interface

Guild Serve is an HTTP server that uses a REST interface for running a
model and accessing model stats.

You may use any HTTP client to interface with Guild Serve. Examples
below use the `curl` command line utility.

Guild Serve supports the following operations:

| [POST `/run`](#post-run) | Run a model by submitting JSON input |
| [GET `/info`](#get-info) | Get model info |
| [GET `/stats`](#get-stats) | Get model stats |

### POST `/run`

To run your model --- i.e. perform inference using inputs --- POST
JSON inputs to the `/run` path.

The body of the request must be a well formed JSON list of input
objects. Each input object represents an element that will be fed to
the model when evaluating its output tensors and must define
attributes for each of the required input tensors.

#### Parameters

| `withstats` | if present, indicates that the model should be run with tracing to capture stats |

{% note %}

Tracing a model incurs a performance penalty --- the `withstats`
parameter should be used to occasionally test model performance
rather than used for every request.

{% endnote %}

#### Result

The result of a successful run is the JSON encoded outputs from the
model.

To view the input and output tensors for a model,
use [GET `/info`](#get-info).

#### Example

To illustrate inference in Guild View, we'll use the samples generated
by the {% ref mnist-example %}. To generate the samples, train either
intro or expert models and run:

{% term %}
$ guild prepare samples
{% endterm %}

This generates a number of samples from the MNIST training set. Each
sample is represented by an image (PNG file) and a JSON encoding of
the image that can be used for inference.

In another terminal, serve the latest model:

{% term %}
$ guild serve --latest-model
{% endterm %}

Select an image you want to submit to the model in the `samples`
directory. In this example we'll use the image `00033.png`, which is
classified as the number 4.

<div class="m-3">
<img src="/assets/img/00033.png">
</div>

We'll use curl to submit the JSON representation of the image, which
is `00033.json`.

Confirm that the MNIST model is running on port 6444 and from the
MNIST example project directory run:

{% term %}
$ echo [`cat samples/00033.json`] | curl -d @- localhost:6444/run | python -m json.tool
{% endterm %}

This command line operation has three parts:

- Create the JSON request body by surrounding the image JSON in square
  brackets to create a list
- Submit the JSON request body to Guild Serve (`-d` indicates that the
  following argument should be submitted as the request body and the
  notation `@-` indicates that the value should be read from standard
  input)
- Pretty print the output from curl using Python's `json.tool` module

You should see something similar to this (the values you see will be
different):

{% code %}
[
    {
        "prediction": [
            -9.565634727478027,
            2.1398215293884277,
            1.6076571941375732,
            -3.6334381103515625,
            18.267333984375,
            -4.2377238273620605,
            -0.4351389408111572,
            -5.422787666320801,
            1.177714228630066,
            3.202186107635498
        ]
    }
]
{% endcode %}

This result shows the prediction values for each digit --- each digit
corresponding to its zero-based position in the `prediction`
array. The highest value is the digit predicted by the model. In the
example here, which uses the MNIST expert model (i.e. a convolutional
neural network), correctly predicts 4. The intro model should also
predict 4.

### GET `/info`

To get information about a model being hosted by Guild Serve, make a
GET request to `/info`.

#### Parameters

*This operation takes no parameters.*

#### Result

The result is a JSON object containing the following keys:

| `inputs` | map of input tensors required as inputs to the model |
| `outputs` | map of output tensors returned for a successful run |
| `path` | path of the model being served |

Tensors returned for `inputs` and `outputs` have the following keys:

| `dtype` | type of values stored in the tensor |
| `shape` | tensor shape |
| `tensor` | name of the tensor in the TensorFlow graph |

#### Example

With an MNIST model running in another terminal, use curl to get
information about the model:

{% term %}
$ curl localhost:6444/info | python -m json.tool
{% endterm %}

As with the `/run` example above, we use Python to pretty print the
response.

You should see this result for the intro model (the value for `path`
will be different and the expert model has different tensor names but
is otherwise the same):

{% code %}
{
    "inputs": {
        "image": {
            "dtype": "float32",
            "shape": "(?, 784)",
            "tensor": "Placeholder:0"
        }
    },
    "outputs": {
        "prediction": {
            "dtype": "float32",
            "shape": "(?, 10)",
            "tensor": "Softmax:0"
        }
    },
    "path": "./runs/20161221T224843Z-intro/model/export"
}
{% endcode %}

{% insight %}

This information is useful when formulating the inputs for a run and
handling the results.

{% endinsight %}

### GET `/stats`

Guild Serve can optionally maintain statistics about model runs that
are submitted with the `withstats` parameter.

#### Parameters

*This operation has no parameters.*

#### Result

The result is a JSON object containing the following keys:

| `average_batch_time_ms`  | rolling average of run times in milliseconds |
| `last_batch_time_ms`     | time of the last run in milliseconds |
| `last_memory_bytes`      | memory used by the last run in bytes |
| `predictions_per_second` | estimated predictions per second based on average batch time |

#### Example

Submit one or more requests to a hosted model using the `withstats`
parameter --- for example, using the URL:

    http://localhost:6444/run?withstats

Read the current stats by running:

{% term %}
$ curl localhost:6444/stats | python -m json.tool
{% endterm %}

You should see output similar to this (with different values):

{% code %}
{
    "average_batch_time_ms": 3.373,
    "last_batch_time_ms": 3.373,
    "last_memory_bytes": 542990.336,
    "predictions_per_second": 3261.191817373258
}
{% endcode %}

{% insight %}

Stats are useful for estimating throughput and latency of inference
operations in an application, as well as memory requirements.

{% endinsight %}

## Next Steps

{% next /tutorials/integrating-guild-with-your-project/ %}Integrate Guild with your project{% endnext %}
{% next /examples/ %}Review Guild examples{% endnext %}
