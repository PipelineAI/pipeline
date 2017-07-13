---
layout: docs
title: Projects
description: Guide for using Guild projects
group: user-guide
---

Guild is enabled for a TensorFlow project by including a simple
`Guild` file, located in the project root.

## Contents

* Will be replaced with the ToC
{:toc}

## Overview

Most [Guild operations](/user-guide/model-operations/) are performed
in the context of a *project*. A project is a directory containing a
`Guild` project file, which describes your project. Project files
provide Guild with information needed to perform operations:

- Models to be trained, evaluated, and served
- Flags to be used for various model operations
- What to display in Guild view

By default, Guild will look for a project file starting from the
current directory and moving up the file system hierarchy. You can
alternatively specify the project location using the `--project`
command line option.

Here's a simple `Guild` file:

{% code %}
[project]

name = My Sample Project

[model]

runtime = tensorflow
train = train

[flags]

rundir = $RUNDIR
{% endcode %}

Guild project files use an extended {% link
https://en.wikipedia.org/wiki/INI_file %}INI file format{% endlink
%}. They are intended as user facing artifacts --- i.e. they are
easily read and edited.

For more information on project files,
see [Project reference](/project-reference/guild-project-file/).

{% insight %}

A good way to learn about a Guild project is to read the `Guild`
project file. If you're using a terminal, view the file using `cat` or
`less`. If you're browsing a project on GitHub, simply click the
`Guild` file in the project root directory.

{% link
https://github.com/guildai/guild-examples/blob/master/mnist/Guild
%}Here's an example{% endlink %}

{% endinsight %}

## Guild examples

Guild AI provides a number of complete projects that illustrate all of
Guild's features. Use these as references for your own projects.

{% next /examples/ %}View Guild examples{% endnext %}

## Initializing a new project

To initialize a new project, change to the top-level directory
containing your project files (or a new directory) and run:

{% term %}
$ guild init --name NAME --train-cmd CMD
{% endterm %}

`NAME` is the name of your project, which is saved in the new project
file under as the `name` attribute in the `project` section. This
value is used in [Guild View](/user-guide/guild-view/) to identify your
project. If you omit the project name argument, the name of the
current directory will be used.

`CMD` is the name of the Python module used to train the default
associated with the project. If you omit this value, Guild will print
a warning message indicating that the project requires a train
command.

{% note %}

You can change project values at any time by modifying the `Guild`
project file.

{% endnote %}

For more information on the `init` command, refer to the {% ref cmd:init %}.

## Project settings

The `project` section in the Guild file defined project level
settings. These include:

| `name`    | project name |
| `runroot` | directory to store runs in |

If you specified the `--name` option when running `init` the project
name will be already configured using this value.

By default, Guild stores runs local to each project. This is similar
to other build tools that create compiled artifacts within a project
directory structure.

{% note %}

To ensure that runs are not stored in revision control systems along
with your other source code, ensure that `runs` (or the value used for
`runroot`) is ignored by your revision control tool (e.g. added to
`.gitignore`).

{% endnote %}

For more information,
see [Project header](/project-reference/guild-project-file/#project-header).

## Models

Guild projects center on training, evaluating, and serving *models*. A
model is a neural network architecture that is implemented as a
TensorFlow graph. In Guild AI, a model represented by a section in a
Guild project file.

Guild projects may define multiple models. Additional models must be
uniquely identified within a project using section names.

For example, here are two named models:

{% code %}
[model "10-layer"]

train = train --layers=10

[model "20-layer"]

train = train --layers=20
{% endcode %}

Use the operation name when performing an operation on a named
model. You may omit a name when performing an operation on a default
model (an unnamed model, or the first model defined in a project, if
all of the models are named).

For example, to train the model named "10-layers" above, run:

{% term %}
$ guild train 10-layers
{% endterm %}

For more information, see [Models](/project-reference/models/) in the
project reference.

## Flags

Flags are used as command line arguments when performing model
operations. You can use different flag sections to define different
sets of values to use for your models.

An unnamed flags section can be used to define flags for all models.

Named flags sections can be used to define flags that are used when
performing operations for the matching model --- or if the user
specifies a `--profile` command line option when running the
operation.

Flag values may contain references to *operation variables*. Operation
variables are resolved by Guild when running commands.

The most commonly used variable is `RUNDIR`, which is the run
directory created for a train operation, or the directory specified by
the user for run-specific operations (e.g. eval, serve, etc.)

For more information,
see [Option variables](/user-guide/model-operations/#operation-variables).

## Views

View sections define the fields and series to display when viewing
project models. Multiple views may be defined, provided each
additional view section is named.

Like named flag sections, named views correspond to models with the
same name.

For more information, see [Views](/project-reference/views/) in the
project reference.

## Next Steps

{% next /user-guide/model-operations/ %}Read about Guild operations{% endnext %}
