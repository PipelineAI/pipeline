---
layout: docs
title: Flags
description: Guild project flags
group: project-reference
---

Flags serve an important function in Guild operations --- they are
passed as command options to model operations *prepare*, *train*, and
*evaluate*. For example, if the flag `datadir` is defined as `./data`
for a train operation, the train command will be called with the
additional argument:

{% term %}
--datadir=./data
{% endterm %}

In this way flags are used to parameterize Guild operations.

Flags may be defined in two ways: as *project wide flags* or as *named
flags*. Project wide flags are defined in an unnamed `flags`
section. For example, his section defines three flags that apply
throughout a project:

{% code %}
[flags]

rundir  = $RUNDIR
datadir = ./data
epochs  = 200
{% endcode %}

A named flags section is associated with named models and
resources. This sample section redefines the `epochs` flag, associating
it with the name "long-train":

{% code %}
[flags "long-train"]

epochs  = 2000
{% endcode %}

Flags are applied in an order of precedence, each level adding or
redefining flag values as provided:

1. `-F, --flag` command line options
2. Flag sections applied using the `--profile` command line option
3. Named flags corresponding to the model or resource name
4. Project wide flags (i.e. unnamed flags section)

Let's consider a project with two models, *intro* and *expert*, and
two flag sections --- one project wide (unnamed) and another named
"expert":

{% code %}
[model "intro"]

train = train_intro

[model "expert"]

train = train_expert

[flags]

epochs = 100

[flags "expert"]

epochs = 200
{% endcode %}

Using the `--preview` option we can see what command Guild will run
under various scenarios.

{% term %}
$ guild train --preview

  python -m train_intro --epochs=100

$ guild train expert --preview

  python -m train_expert --epochs=200

$ guild train intro --profile expert

  python -m train_intro --epochs=200

$ guild train intro --flag epochs=300

  python -m train_intro --epochs=300
{% endterm %}

{% note %}

Guild includes all applicable flags to an operation command, whether
the command expects or requires the flag or not. For this reason,
command line parsers should use this pattern when parsing arguments:

{% code python %}
parser = argparse.ArgumentParser()
# calls to parser.add_argument
FLAGS, _ = parser.parse_known_args()
{% endcode %}

Note the use of `parse_know_args` rather than `parse_args`. This form
tolerates additional flags that Guild may provide but that aren't
defined for the script.
{% endnote %}

{% insight %}
Flags are a useful construct in Guild --- they are used to define sets
of parameterized inputs to your model operations. This documents the
set of inputs to various operations under different conditions and
reduces the chances of user error when running commands.
{% endinsight %}

{% next /project-reference/views/ %}Next: Project views{% endnext %}
