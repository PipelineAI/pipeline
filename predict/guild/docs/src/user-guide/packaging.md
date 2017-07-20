---
layout: docs
title: Packaging
description: How to create and use Guild packages
group: user-guide
---

Guild packages can be used to quickly install pre-trained models,
datasets, and source packages that are used as templates for new Guild
projects.

## Contents

* Will be replaced with the ToC
{:toc}

## Overview

There are three types of packages:

- Datasets
- Models
- Source package

## Dataset packages

Dataset packages contain data that can be used to train
models. Datasets may contain files in any format but it's common to
use TensorFlow's TFRecord format for efficiency. The dataset format
must comply with the format expected by any model trained using the
dataset.

## Models

## Source packages

## Building packages

### Steps

1. Identify a candidate project
2. Name the package
3. Initialize a package definition
4. List references
5. Identify and work with sources
6. Build and package files
7. Test package
8. Register package with a repo

#### Identify a candidate project

Candidate projects may be in early stages, such as a published paper
or blog post about a novel architecture or approach, or they may be
fully developed TensorFlow scripts, such as those in the {% link
https://github.com/tensorflow/models %}TensorFlow Models{% endlink %}.

#### Name the package

Name the package using generally accepted terms that are as short as
possible. If the package must have multiple terms, separate each term
with a dash ('-').

If the package is a source package, it should end with `-src`.

If the package is a pretrained mode, it should end with `+DATASET`
where `DATASET` is the name of the dataset used to train the model.

Examples:

| Name                 | Description |
| -------------------- | ----------- |
| cifar10              | CIFAR10 dataset |
| inception-v1-src     | Inception v1 source package |
| inception-v1+cifar10 | Inception v1 model trained using the cifar10 dataset |

#### Initialize a package project

```
$ guild init --package DIR
```
