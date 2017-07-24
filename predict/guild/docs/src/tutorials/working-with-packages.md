---
layout: docs
title: Working with packages
description: Using Guild packages to accelerate TensorFlow development.
group: tutorials
---

Guild packages are a convenient way to quickly bootstrap you
TensorFlow model development. Packages let you find and install
predefined models, including those that have been trained on various
datasets. Source packages may be used to generate new Guild projects
that you customize or use to fine tune trained models with your own
data.

## Contents

* Will be replaced with the ToC
{:toc}

## Package overview

Guild packages are compressed archives that contain source code and
binary files associated with TensorFlow model training and use. In
general, a package can contain any of these artifacts:

- Datasets
- Untrained models
- Trained models
- Project source code
- Guild project templates

Packages follow a consistent naming convention to help identify what
they contain:

    NAME[:DATASET]-VERSION.pkg.tar.xz

`NAME` is either a model or dataset name. If a model has been trained,
`DATASET` indicates the dataset used for training. `VERSION` is used
to indicate when a package has been updated and is either a date
timestamp or an incrementing integer.

## Finding packages

To find a package, use the `search` command:

```
$ guild search TERM
```

`TERM` may be a part of a package name (e.g. `inception`) or a regular
exception. Use an asterisk (`*`) to view all packages.

## Installing and uninstalling packages

To install a package, use the `install` command:

```
$ guild install PACKAGE
```

`PACKAGE` may be the package name with or without a version number. If
the version number is omitted, Guild will install the latest version.

Packages are installed per user in `$HOME/.guild/packages`.

You may uninstall a package using the `uninstall` command:

```
$ guild uninstall PACKAGE
```

To uninstall a paricular version, specify the package version
number. To uninstall all versions of a package, omit the version
number.

## Referencing package content

Packages that include datasets or saved models may be reference by
their file system path. To reference binary artifacts provided by a
package, you may use `$HOME/.guild/packages/PACKAGE-VERSION`. For
example, to reference the `mnist-1` package in a script:

```
$ python train.py --dataset $HOME/.guild/packages/mnist-1
```

Use Guild's `list-packages` command to list installed packages.

Note that when referencing a package this way, you must specify the
full package name along with its version.

To reference a package within a Guild project file, you may use
`$PACKAGES/PACKAGE[-VERSION]`. The version number may be provided to
use a specific installed version, or omitted to use the latest
version.

Here's a sample Guild project snippet that references the latest
`mnist` dataset:

```
[flags]

dataset = $PACKAGES/mnist
```

## Using a package to generate a project

Packages may also provide project templates, which can be used to
generate projects. To generate a project using an installed package,
use the `init` command along with `--template=PACKAGE` and any
variables required by the template. For example, to generate a new
project using the latest installed `inception_v3` package, run:

```
$ guild init --template inception_v3
```

This will generate a project in the current directory that can be used
to train an *Inception v3* model.

Once you've generated a project from a package template, you may
modify the Guild project file to provide training details, including
the dataset use for training. This is typically a matter of making a
few changes to the {% ref guild-project-file %} and then running:

```
$ guild train
```
