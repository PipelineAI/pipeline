---
layout: docs
title: Using Guild to serve models
description: How to use Guild to serve models as web services.
group: tutorials
---

In this tutorial we'll run the trained MNIST model
from [Quick start](/getting-started/quick-start/) as locally running
web service.

## Contents

* Will be replaced with the ToC
{:toc}

## Train MNIST expert model

This is a continuation of the quick start guide. If you haven't
already done so, complete these steps from the guide:

1. [Get Guild examples](/getting-started/quick-start/#get-guild-examples)
2. [Prepare to train MNIST](/getting-started/quick-start/#prepare-to-train-mnist)
3. [Train MNIST expert](/getting-started/quick-start/#train-mnist-expert)

## Serve model as web service

In this section we'll run your latest trained *expert* model as a web
service using [Guild Serve](/user-guide/guild-serve/).

Open a new terminal and change to your MNIST project directory. Run
this command:

{% term %}
$ guild list-runs --completed --with-export
{% endterm %}

You should see both the intro and expert runs listed. Select the
latest expert run (the path containing "expert" that's closest to the
top of the list) and serve it by running this command:

{% term %}
$ guild serve PATH_TO_RUN
{% endterm %}

Replace `PATH_TO_RUN` with the selected path.

{% note %}

You can alternatively run `guild serve --latest-run` if you know the
latest run is what you want to serve.

{% endnote %}

Your MNIST model is now available as an HTTP based web service! You
can test it by POSTing JSON input to `http://localhost:6444/run`.

Copy this JSON content to your clipboard:

<div class="with-copy">
{% code json %}
{% include mnist-image.json %}
{% endcode %}
</div>

{% include test-serve-with-curl.md %}

The result from the `curl` command is the JSON *outputs* from the
model. You application can use this interface to perform predictions
using your model.

To use this interface in your application, follow the conventions
outlined for your HTTP client for making POST requests to an HTTP
server.

For details on the application interface, see the documentation
for [Guild Serve](/user-guide/guild-serve/).

## Summary

In this tutorial we ran a trained model as a web service
using [Guild Serve](/user-guide/guild-serve/). We used the `curl`
application to test the model with sample JSON. You can use Guild
Serve's [REST interface](/user-guide/guild-serve/#rest-interface) to
similarly run your model from your application!
