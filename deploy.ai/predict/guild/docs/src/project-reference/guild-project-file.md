---
layout: docs
title: Guild project file
description: Details on the Guild project file
group: project-reference
---

Guild project files contain information about your project that lets
Guild perform various operations.

A Guild project file is named `Guild` and located in the root of the
project directory. Guild commands that apply to projects will use the
closest Guild file from the current directory, or the directory
provided with the `--project` option.

Guild files are extended {% link
https://en.wikipedia.org/wiki/INI_file%}INI files{% endlink %}. They
are organized in sections, each section having an optional
name. Sections contain attributes, which are named values.

Here's a simple Guild file with a single `model` and `flags` section.

{% code %}
[project]

name = My Sample Project

[model]

runtime = tensorflow
train = train

[flags]

rundir = $RUNDIR
{% endcode %}

A name can be used to further identifies a section. Here's a section
for a model named "expert":

{% code %}
[model "expert"]

runtime = tensorflow
train = train_expert
{% endcode %}

## Project header

Each project may contain at most one `project` section.

### Project attributes

#### name

The project name is used by Guild View whenever identifying the project.

If a project name is not specified, Guild will infer a name from the
project directory.

#### runroot

This value indicates where runs are stored. Paths are relative to the
project root. The default value is `runs`.

{% next /project-reference/models/ %}Next: Project models{% endnext %}
