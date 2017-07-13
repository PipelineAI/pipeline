---
layout: simple
title: Features
description: Guild AI features
group: features
---

<style>
 figure {
    width: 100%;
 }

 figure img {
     width: 100%;
 }

 .figure-img {
      margin-bottom: 10px;
 }

 .highlight pre {
     margin: 5px 12px;
 }

 .highlight pre code {
     font-size: 0.75rem;
 }
</style>

<div class="bd-featurette guild-features" style="border-top:none">
  <div class="container">
    <div class="row">
      <div class="col-lg-6">

{% markdown %}

### Real time training updates

Guild track your TensorFlow experiments in real time and gives you
up-to-the-second feedback. Guild uses intelligent polling and a high
performance embedded database to minimize the impact on your system.

{% endmarkdown %}

      </div>
      <div class="col-lg-6">
        <figure class="figure">
          <img class="figure-img border" style="border-width:0" src="/assets/img/view-anim-2.gif">
        </figure>
      </div>
    </div>
  </div>
</div>

<div class="bd-featurette guild-features">
  <div class="container">
    <div class="row">
      <div class="col-lg-6">

{% markdown %}

### At-a-glance run comparisons

Guild summarizes important run results and presents them in a
sortable, filterable table for quick access. Results are updated in
real time to keep you updated on your current experiment while
comparing it to previous runs.

{% endmarkdown %}

      </div>
      <div class="col-lg-6">
        <figure class="figure">
          <img class="figure-img border" src="/assets/img/view-7.jpg">
        </figure>
      </div>
    </div>
  </div>
</div>

<div class="bd-featurette guild-features">
  <div class="container">
    <div class="row">
      <div class="col-lg-6">

{% markdown %}

### Compare model training and system statistics


Guild lets you dig deeper into run results by comparing TensorFlow
scalars and system statistics. Guild has integrated TensorBoard's
charting component which lets you compare series data by global step,
relative time, and wall time. It also supports series smoothing and
logarithmic Y axis.

{% endmarkdown %}

      </div>
      <div class="col-lg-6">
        <figure class="figure">
          <img class="figure-img border" src="/assets/img/view-8.jpg">
        </figure>
      </div>
    </div>
  </div>
</div>

<div class="bd-featurette guild-features">
  <div class="container">
    <div class="row">
      <div class="col-lg-6">

{% markdown %}

### Capture more data


In addition to your TensorFlow event logs, which contain your training
statistics and other summaries, Guild captures a range of other
essential build artifacts including script output, command flags,
system attributes and stats such as GPU and CPU utilization. This
information is indispensable for answering certain questions,
particularly those concerning operational performance.

{% endmarkdown %}

      </div>
      <div class="col-lg-6">
        <figure class="figure">
          <img class="figure-img border" src="/assets/img/view-9.jpg">
        </figure>
      </div>
    </div>
  </div>
</div>

<div class="bd-featurette guild-features">
  <div class="container">
    <div class="row">
      <div class="col-lg-6">

{% markdown %}

### TensorBoard integration

When you need to drill into even more detail, Guild seamlessly
integrates TensorBoard into your project view. There's no need to
start a separate TensorBoard process --- Guild handles this in the
background when you run the `view` command. TensorBoard lets you view
event log summaries including scalars, images, audio, variable
distributions and histograms, and an interactive view of the model
graph.

{% endmarkdown %}

      </div>
      <div class="col-lg-6">
        <figure class="figure">
          <img class="figure-img border" src="/assets/img/view-10.jpg">
        </figure>
      </div>
    </div>
  </div>
</div>

<div class="bd-featurette guild-features">
  <div class="container">
    <div class="row">
      <div class="col-lg-6">

{% markdown %}

### Simplified training workflow

Guild workflow consists of simple commands that you run for a project:
`prepare`, `train`, `evaluate`, and `serve`. Guild fills in the
details for each command using information from the Guild project
file, letting you run complex operations typing long, complex
commands.

{% next /command-reference/overview/ %}Guild command reference{% endnext %}

{% endmarkdown %}

      </div>
      <div class="col-lg-6">
        <figure class="figure">
          <img class="figure-img border" style="border-width:2px;border-color:#222" src="/assets/img/view-anim-3.gif">
        </figure>
      </div>
    </div>
  </div>
</div>

<div class="bd-featurette guild-features">
  <div class="container">
    <div class="row">
      <div class="col-lg-6">

{% markdown %}

### Self documenting project structure

Guild projects provide instructions for performing the prepare, train,
and evaluate operations. Project are plain text files that are easy
for humans to read. They are useful not only to Guild for running
commands but as model interface specifications.

{% next /project-reference/guild-project-file/ %}Guild project reference{% endnext %}

{% endmarkdown %}

      </div>
      <div class="col-lg-6">
        <figure class="highlight language-none">
          <pre><code class="language-none" data-lang="none">[project]

name              = MNIST
description       = Guild MNIST example

[model "expert"]

prepare           = expert --prepare
train             = expert
train_requires    = ./data
evaluate          = expert --test

[flags]

datadir           = ./data
rundir            = $RUNDIR
batch_size        = 100
epochs            = 10
train_dir         = $RUNDIR</code></pre>
        </figure>
      </div>
    </div>
  </div>
</div>

<div class="bd-featurette guild-features">
  <div class="container">
    <div class="row">
      <div class="col-lg-6">

{% markdown %}

### Integrated inference server

Guild provides an integrated HTTP server that you can use to test your
trained models before deploying them to TensorFlow serving.

{% next /tutorials/using-guild-to-serve-models/ %}More on Guild serve{% endnext %}

{% endmarkdown %}
      </div>
      <div class="col-lg-6">
        <figure class="figure">
          <icon class="fa-awesome fa-fw fa-icon-upload-alt"></icon>
        </figure>
      </div>
    </div>
  </div>
</div>
