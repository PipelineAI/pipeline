---
layout: docs
title: Views
description: Guild project views
group: project-reference
---

## Contents

* Will be replaced with the ToC
{:toc}

View sections define how Guild View displays training results for
models.

A project must define a `view` section for Guild View to display
something for the project.

View attributes are used for the following:

- Train fields (defined by the `fields` attribute)
- Train series (defined by the `series`, `series-a`, and `series-b` attributes)
- Compare fields (defined by the `compare` attributes)

Here's view defined for the {% ref mnist-example %}.

{% code %}
[view]

fields           = validation-accuracy train-accuracy steps time

series-b         = loss accuracy op-cpu-percent op-memory \
                   gpu-percent gpu-memory gpu-powerdraw

compare          = validation-accuracy train-accuracy batch-size-flag \
                   steps time
{% endcode %}

Each view attribute is a space delimited list of fields or series to
display in the view.

For details about each attribute, refer to the sections below.

## View attributes

### fields

The `fields` attribute is a list of fields that should appear at the
top of the **Train** page in Guild View. Each field name maps to
either a built in field, or a custom field defined for the project.

For more information on fields, refer to [Fields](/project-reference/fields/).

### series

The series attributes define the time series charts that should appear
in the view.

`series` display full width charts at the top of the view.

For more information on series, refer to [Series](/project-reference/series/).

### compare

The `compare` attribute defined a list of fields to use in the Guild
View **Compare** page.

If `compare` is not provided, the value for `fields` (see above) will be used.

For more information on fields, refer to [Fields](/project-reference/fields/).

{% next /project-reference/fields/ %}Next: Project fields{% endnext %}
