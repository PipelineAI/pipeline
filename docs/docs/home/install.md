PipelineIO CLI is a python based command line tool to interact with PipelineIO.

`pio-cli` is available on [pypi](https://pypi.python.org/pypi/pio-cli) and
runs on both Python 2.7 and Python 3.

Use pip to install the cli.

```bash
pip install -U pio-cli
```

If successfully installed you can view the commands supported by the CLI using just `pio`. 

```
pio

...
```

Detailed documentation for the PipelineIO CLI commands are available in the [documentation](../commands/index.md).


## Virtualenv

We highly recommend using [virtualenv](https://virtualenv.pypa.io/en/stable/userguide/) to install `pio-cli`. This will isolate your PipelineIO environment and avoid any existing library conflicts.  This will ensure a smooth installation process.

```bash
sudo pip install virtualenv
```

To create a virtualenv environment, you need pass a directory for your environment.

```bash
virtualenv ~/.pio
```

You can activate the virtualenv by running:

```bash
source ~/.pio/bin/activate
```
__Note:  Everytime you need to use `pio` in a terminal, activate the virtualenv using this command.__

To install `pio-cli` in this virtualenv:

```bash
pip install -U pio-cli
```

## Using `sudo`

If you are not using virtualenv and you are installing `pio` globally you may need to use `sudo`:

```bash
sudo pip install -U pio-cli
```
## Missing dependencies

Not all python environments are installed the same way. So sometimes you may run 
into install issues. If `pip` cannot install dependencies itself, you may see errors like:

```bash
...
Failed building wheel for scandir
...
```

or

```bash
...
No distributions matching the version for backports.tempfile (from pio-cli)
...
```

In such cases, you can install the dependencies directly:

```bash
pip install -U scandir
pip install -U backports.tempfile
```

and then try re-installing `pio-cli`


## Python.h: No such file or directory

If you get this error in a linux environment:

```bash
...
Python.h: No such file or directory
```

you need to install `python-dev` package

```bash
sudo apt-get install python-dev
```

{!contributing.md!}
