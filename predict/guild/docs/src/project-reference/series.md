---
layout: docs
title: Series
description: Guild project series
group: project-reference
---

## Contents

* Will be replaced with the ToC
{:toc}

Series are similar to fields --- they are specification for displaying
data collected by Guild during a run. Series attributes specify how
time series are displayed and so have slightly different semantics from
fields.

Like fields, Guild defines named series with predefined
attributes. Each series can be modified by adding a corresponding
named series section.

For examples of Guild's predefined series, refer to:

- [default-series.config](https://github.com/guildai/guild/blob/master/priv/viewdefs/default-series.config) (base series attributes)
- [tensorflow-series.config](https://github.com/guildai/guild/blob/master/priv/viewdefs/tensorflow-series.config) (TensorFlow specific attributes)

## Series attributes

### format

The `format` attribute defined how series values are formatted. Refer
to the [field format attribute](#format) for more information.

### label

The `label` attribute is displayed on the Y axis of series charts.

### source

The `source` attribute of a series refers to a series collected by
Guild during a run. Refer to the [field source attribute](#source) for
more information.

The `source` attribute may be a regular expression that matches
multiple sources. Each source is displayed as a separate series in a
series widget.

Refer to the series examples above for commonly used sources.

### title

The `title` attribute is used as the chart widget title.

{% next /project-reference/resources/ %}Next: Project resources{% endnext %}
