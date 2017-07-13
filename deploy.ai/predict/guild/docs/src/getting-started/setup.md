---
layout: docs
title: Setup
description: Setup for Guild AI
group: getting-started
---

Follow the steps below to install and configure Guild AI on your system.

## Contents

* Will be replaced with the ToC
{:toc}

## Requirements

### Python

Guild AI currently requires Python 2.7. As of Guild version
{{site.current_version}} Python 3 is not supported.

### TensorFlow

Guild AI requires TensorFlow version {{site.tensorflow_min}} or
later. Some projects may require later versions of TensorFlow ---
refer to the documentation for each project to confirm you have a
compatible version of TensorFlow installed.

Refer to {% link https://www.tensorflow.org/install/ %}Installing
TensorFlow{% endlink %} for instructions on installing TensorFlow on
your system. If your system does not have a GPU, or you're not sure,
select the non-GPU version of TensorFlow. You can upgrade to the GPU
version later if you need to.

### psutil

Guild uses the Python library {% link
https://github.com/giampaolo/psutil %}psutil{% endlink %} to collect
many system stats. While this library is optional, we recommend
installing it to extend the data Guild collects for your model.

Using `pip` run:

{% term %}
$ pip install --upgrade psutil
{% endterm %}

For alternatives, see the {% link
https://github.com/giampaolo/psutil/blob/master/INSTALL.rst %}psutil
install instructions{% endlink %}.

### nvidia-smi

Guild uses {% link
https://developer.nvidia.com/nvidia-system-management-interface
%}NVIDIA System Management Interface{% endlink %} to collect GPU
statistics. If your system has an NVIDIA GPU, we recommend installing
this utility to extend the data Guild collects for your model.

To install `nvidia-smi` install the latest CUDA toolkit from this location:

{% link https://developer.nvidia.com/cuda-downloads
%}https://developer.nvidia.com/cuda-downloads{% endlink %}

## Installing precompiled binaries

Download the latest release for your system using the applicable link
below.

| **Platform** | **Architecture** | **Download** |
| Linux       | x86 64-bit   | [guild_{{site.current_version}}_linux_x86_64.tar.gz](https://github.com/guildai/guild/releases/download/v{{site.current_version}}/guild_{{site.current_version}}_linux_x86_64.tar.gz)
| Mac OS X    | x86 64-bit   | [guild_{{site.current_version}}_dawrin_x86_64.tar.gz](https://github.com/guildai/guild/releases/download/v{{site.current_version}}/guild_{{site.current_version}}_darwin_x86_64.tar.gz)

If your platform is not listed,
see [Installing from source](#installing-from-source) below.

Earlier versions of Guild AI can be
downloaded [here](https://github.com/guildai/guild/releases).

### Unpack archive

The Guild AI archive can be extracted into a single directory on your
system and run in place. If you want to make Guild available system
wide, unpack the archive to the system location for your platform
(e.g. `/opt/` for Linux, `/usr/local/opt/` for Max OS X).

Here are some commands that install Guild binaries into system
locations.

For **Linux** (installs to `/opt/`) run:

{% term %}
$ sudo tar -C /opt/ -xzf guild_{{site.current_version}}_linux_x86_64.tar.gz
{% endterm %}

For **Max OS X** (installs to `/usr/local/opt`) run:

{% term %}
$ sudo tar -C /usr/local/opt -xzf guild_{{site.current_version}}_darwin_x86_64.tar.gz
{% endterm %}

Change the install directory to suit your environment, bearing in mind
that will change the path used in the commands below.

### Running Guild

You can run Guild directly by executing `bin/guild` from the unpacked
archive location. To run the `guild` command without specifying its
full path each time, you need to modify your environment in one of the
following ways:

1. Create a `guild` symbolic link that points to `bin/guild`
2. Add the `bin` directory to your `PATH` environment

If you're unsure which method to use, select option 1 below.

#### Option 1 - symbolic link

This option creates a {% link
https://en.wikipedia.org/wiki/Symbolic_link %}symbolic link{% endlink
%} in `/usr/local/bin` that resolves to the installed `guild` command.

{% note %} The paths below assume you installed Guild in the
recommended system locations [provided above](#unpack-archive). If you
installed Guild in a different location, modify the locations below
accordingly.{% endnote %}

For **Linux** run:

{% term %}
$ sudo ln -sf /opt/guild_{{site.current_version}}_linux_x86_64/bin/guild /usr/local/bin/guild
{% endterm %}

For **Mac OS X** run:

{% term %}
$ sudo ln -sf /usr/local/opt/guild_{{site.current_version}}_darwin_x86_64/bin/guild /usr/local/bin/guild
{% endterm %}

#### Option 2 - modify PATH

This option modifies your `PATH` environment to include Guild's `bin`
directory. Note again that the paths used below assume you installed
Guild to the recommended system locations --- if you installed
somewhere else, modify the values accordingly.

For **Linux** systems, add the following line to the end of your shell
initialization file:

{% code %}
PATH=$PATH:/opt/guild_{{site.current_version}}_linux_x86_64/bin
{% endcode %}

{% note %}

Linux systems use a variety of shells and shell initialization
schemes. If you're not sure which file to modify for your system,
refer to {% link
https://linux.die.net/Bash-Beginners-Guide/sect_03_01.html %}Shell
initialization files{% endlink %} for help or open an issue with {%
ref github-issues %}.

{% endnote %}

For **Mac OS X** add this to the end of `~/.bash_profile`:

{% code %}
PATH=$PATH:/usr/local/opt/guild_{{site.current_version}}_darwin_x86_64/bin
{% endcode %}

{% note %}

Changes to your PATH will not take effect in any of your current shell
sessions. To confirm your PATH is set correctly, open **a new
terminal** and run:

{% term %}
$ echo $PATH
{% endterm %}
{% endnote %}

## Installing from source

If precompiled binaries are not available for your platform or you
want to run the latest code, you can compile Guild from source.

To compile Guild source, you need the following software:

- Erlang 18 or later
- Python 2.7
- Make

Clone the Guild AI source repository on GitHub:

{% term %}
$ git clone https://github.com/guildai/guild.git
{% endterm %}

Change to the Guild source directory and compile by running:

{% term %}
$ cd guild
$ make
{% endterm %}

This will download the required software libraries and compile Guild.

To run the compiled version of Guild, create a symbolic link to
`scripts/guild-dev` in the Guild source directory.

## Verifying your setup

Verify your Guild setup by running:

{% term %}
$ guild check
{% endterm %}

This command verifies the Guild setup and prints information about the
environment. If the command exits without error messages, you've
successfully installed Guild!

If you see errors, ensure that you completed the steps above. If
you're still unable to resolve the issue, open an issue in {% ref
github-issues %}.

If you see warnings, your setup is okay but will have some limited
functionality.

{% next /getting-started/quick-start/ %}Next: Start using Guild{% endnext %}
