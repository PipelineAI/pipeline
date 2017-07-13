# -*- coding: utf-8 -*-

"""setup.py: setuptools control."""

import re
from setuptools import setup

import sys
if not sys.version_info[0] == 3:
#    print("\n \
    sys.exit("\n \
              ****************************************************************\n \
              * The CLI has only been tested with Python 3+ at this time.    *\n \
              * Report any issues with Python 2 by emailing help@pipeline.io *\n \
              ****************************************************************\n")

version = re.search(
    '^__version__\s*=\s*"(.*)"',
    open('pio/__init__.py').read(),
    re.M
    ).group(1)

setup(
    name = "pio-cli",
    packages = ["pio"],
    entry_points = {
        "console_scripts": ['pio = pio.__init__:main']
    },
    version = version,
    description = "PipelineAI CLI",
    long_description = "PipelineAI CLI",
    author = "Chris Fregly",
    author_email = "chris@pipeline.io",
    url = "https://github.com/fluxcapacitor/pipeline/cli",
    install_requires=[
        "kubernetes==3.0.0a1",
        "fire==0.1.1",
        "requests==2.18.1",
        "pyyaml==3.12",
        "tabulate==0.7.7",
        "futures==3.1.1",
    ],
    dependency_links=[
    ]
)
