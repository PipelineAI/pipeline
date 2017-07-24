Test the model using `curl`:

{% term %}
$ curl -d '<JSON input from above>' http://localhost:6444/run
{% endterm %}

You can alternatively run your model with performance stats using:

{% term %}
$ curl -d '<JSON input from above>' http://localhost:6444/run?withstats
{% endterm %}

{% note %}

There is some overhead to collecting stats for a model --- if you need
to minimize the latency of running your model, run without stats.

{% endnote %}

To see the current stats for your model, run:

{% term %}
curl http://localhost:6444/stats
{% endterm %}
