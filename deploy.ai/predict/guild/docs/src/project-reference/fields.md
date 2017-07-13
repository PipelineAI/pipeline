---
layout: docs
title: Fields
description: Fields used by Guild
group: project-reference
---

## Contents

* Will be replaced with the ToC
{:toc}

Fields are used in Guild View to summarize run results. They're
displayed as values at the top of the **Train** page. For example:

{% screen view-11.jpg %}

They are also used as table values in the **Compare** page.

{% screen view-12.jpg %}

Guild defines a number of fields that may be referenced by name. Each
named field implicitly defines a number of attributes.

For details on Guild's predefined fields, refer to:

- [default-fields.config](https://github.com/guildai/guild/blob/master/priv/viewdefs/default-fields.config) (base field attributes)
- [tensorflow-fields.config](https://github.com/guildai/guild/blob/master/priv/viewdefs/tensorflow-fields.config) (TensorFlow specific attributes)

Field attributes can be defined, either as new fields, or as modified
fields, using field sections. Here's a field definition that defined
attributes for a field "accuracy":

{% code %}
[field "accuracy"]

color   = green-700
icon    = accuracy
label   = Validation Accuracy
source  = series/tf/validation/accuracy
reduce  = last
format  = 0.00%
{% endcode %}

## Field attributes

Field attributes are used to display values in Guild View:

| [source](#source) | reference to an underlying series or other Guild data source |
| [reduce](#reduce) | function that reduces a series to a single value |
| [format](#format) | format for displaying the field value |
| [label](#label)   | label for the field |
| [color](#color)   | field background color, where applicable |
| [icon](#icon)     | field icon, where applicable |

For details, refer to the field attributes below.


### color

A field `color` attribute specifies a value from
Google's
[Material Design Color Pallete](https://material.io/guidelines/style/color.html#).

### icon

The `icon` attribute specifies an icon to use for a field when it's
displayed on the **Train** page.

Guild provides icons from the [Font Awesome](http://fontawesome.io/)
library.

When specifying a font, omit the `fa-` prefix.

### label

A field `label` is used to identify the field value.

### source

A field `source` references the Guild series key containing the source
data.

Guild data sources may include:

| `series/ + SERIES` | a time series logged for a run |
| `flags` | flags associated with a run |
| `attrs` | attributes associated with a run |

The series available for a run can be listed using the `list-series`
command. To list the series available for the latest run, for example,
run:

{% term %}
$ guild list-series --latest-run
{% endterm %}

<a id="series"></a>

Below is a list of series typically collected for each run:

| `op/cpu/util` | operation CPU utilization |
| `op/mem/rss` | operation resident memory |
| `op/mem/vms` | operation virtual memory |
| `sys/cpu/util` | system CPU utilization |
| `sys/cpuN/util` | CPU utilization for core N |
| `sys/devDEV/rkbps` | KBs read per second for device DEV |
| `sys/devDEV/rps` | reads per second for device DEV |
| `sys/devDEV/util` | utilization for device DEV |
| `sys/devDEV/wkbps` | KBs written per second for device DEV |
| `sys/devDEV/wps` | writes per second for device DEV |
| `sys/gpuN/fanspeed` | fanspeed for GPU N |
| `sys/gpuN/gpu/util` | utilization for GPU N |
| `sys/gpuN/mem/free` | free memory for GPU N |
| `sys/gpuN/mem/total` | total memory for GPU N |
| `sys/gpuN/mem/used` | used memory for GPU N |
| `sys/gpuN/mem/util` | memory utilization for GPU N |
| `sys/gpuN/powerdraw` | power drawn in watts from GPU N |
| `sys/gpuN/pstate` | pstate for GPU N |
| `sys/gpuN/temp` | temperature for GPU N |
| `sys/mem/free` | free system memory |
| `sys/mem/total` | total system memory |
| `sys/mem/used` | used system memory |
| `sys/mem/util` | utilized system memory |
| `sys/swap/free` | free swap |
| `sys/swap/total` | total swap |
| `sys/swap/used` | used swap |
| `sys/swap/util` | utilized swap |
| `tf/SCALAR_SUMMARY` | SCALAR_SUMMARY written to TensorFlow event logs |

### reduce

The `reduce` attribute specifies a function that reduces a series of
values to a single value.

Available reduce functions are:

| `average` | average of the series |
| `duration` | duration across the series --- taken by subtracting the timestamp of the first series event from the last event |
| `last` | the last value of the series |
| `last_5_average` | the average of the last five values of the series --- or the average of the series if there are less than five values |
| `steps` | the number of steps across the series --- taken by subtracting the step value of the first series event from the last event |

### format

The `format` attribute indicates how the field value should be formatted.

Guild supports the formatting specification defined
by [Numeral.js](http://numeraljs.com/#format).

{% next /project-reference/series/ %}Next: Project series{% endnext %}
