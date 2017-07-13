---
layout: narrow
title: FAQ
description: Frequently asked questions
group: faq
---

## Contents

* Will be replaced with the ToC
{:toc}

## Using Guild AI

### When does it make sense to use Guild AI?

Guild supports collaborative development among data scientists,
software engineers, and systems engineers. If you work with others in
the development and deployment/running of your models, Guild provides
some useful features.

In particular Guild is helpful when you have any of these goals:

- [Standardize](/user-guide/projects/) your TensorFlow projects to
  simplify how you and
  others [work with them](/user-guide/model-operations/)
- Collect and [visualize](/user-guide/guild-view/) detailed data on
  model performance to inform operational and system decisions, as
  well as make model improvements
- Automatically preserve the results of every training experiment
- [Run your trained models](/user-guide/guild-serve/) on your own
  infrastructure, AWS, or other hosted GPU environments

### When would I not use Guild AI?

Guild provides structure to a TensorFlow project. In early stages when
you're working alone, it may not make sense to integrate Guild into
your project. As your project matures and you want to publish it for
others to use and improve, Guild will provide a consistent interface
along with features for easily visualizing, tracking, and running your
models.

You can
typically
[integrate Guild into a project](/tutorials/integrating-guild-with-your-project/) in
under an hour --- so you don't need start your project with Guild
integration.

### Does Guild AI incur additional overhead when training models?

Yes, Guild runs your model and also collects and logs additional data
in the background. However, Guild is designed to minimize the impact
on your system during training.

Logs are buffered and compressed in time series ranges to reduce the
storage requirements and number of indexes created as events are
logged. Guild databases tend to be small --- typically less than 1/10
the size of TensorFlow logs themselves.

Guild collects data in relatively coarse grained increments (usually
every 60 seconds, though this is configurable per project) and incurs
virtually no overhead when it's idle.

Guild CPU usage during training is typically less than 1%. The Guild
process itself requires approximately 40 MB of resident memory.

## Licensing

### How much does Guild AI cost?

Guild AI is 100% free, open source software released under
the
[Apache 2 license](https://raw.githubusercontent.com/guildai/guild/master/LICENSE).

That said, any tool costs time and effort to use. We're working hard
to make Guild AI as simple as possible --- if you have suggestions to
improve Guild AI, please feel free
to
[open an issue on GitHub](https://github.com/guildai/guild/issues/new).

### Can I use Guild AI in a commercial application?

Yes. The
[Apache 2 license](https://raw.githubusercontent.com/guildai/guild/master/LICENSE) allows
you to freely distribute Guild AI source without requiring you to
release your changes under a particular license.

## Design

### What language is Guild AI written in?

Guild tools are actually written in several languages:

- **Python** is the primary interface to TensorFlow
- **bash** is used for system scripting
- **Erlang** provides the control layer for most of the Guild operations

Training operations in deep learning applications are intense systems
operations. They may last for not just minutes or hours, but days and
weeks! This is a somewhat novel problem in software --- very long
running batch operations.

Guild AI's focus on operations further adds to the novelty of this
problem --- it must reliably and efficiently collect a variety of
system statistics in the background while ensuring the smooth
functioning of the training operation.

Erlang is a proven tool for handling concurrent activities with high
levels of uptime and reliability. While Python is the obvious choice
for advanced scripting applications, Guild AI's core control layer
benefits from Erlang's unique approach to managing long running
processes.

Python is used in Guild AI for many important tasks, not the least of
which is interfacing with TensorFlow itself. Python is also used to
implement the system stat collectors. If you're interested in
contributing to Guild AI and don't know Erlang, don't despair! There's
a good chance your Python skillset can be applied directly to Guild
AI's many non-Erlang extension points.

### Why does Guild duplicate TensorFlow scalar summaries in its own database?

As background to this question, Guild tails the event logs during
certain operations (notably *train*) and rewrites scalar summaries in
its own database, effectively duplicating the data.

Guild does this for primarily performance reasons as it can take
several minutes to load large TensorFlow event logs into memory. This
problem is compounded when run results are aggregated, as in the case
of the Guild [compare table](/developers-guide/#compare-table).

While Guild does duplicate some of TensorFlow's event logs, it is
careful to minimize the impact on system performance, both in terms of
disk IO and storage. For more information see the answer to
*[Does Guild AI incur additional overhead when training models](#does-guild-ai-incur-additional-overhead-when-training-models)*
above.
