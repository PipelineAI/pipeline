PipelineIO CLI is a python based command line tool to interact with PipelineIO.

`pio-cli` is available on [pypi](https://pypi.python.org/pypi/pio-cli) and
runs on both Python 2.7 and Python 3.

Use `pip` to install the cli.

```bash
pip install pio-cli
```

You can view the commands supported by the CLI using just `pio`. 

```
pio

...
```

Detailed documentation for the PipelineIO CLI commands are available in the [documentation](../commands/index.md).

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
